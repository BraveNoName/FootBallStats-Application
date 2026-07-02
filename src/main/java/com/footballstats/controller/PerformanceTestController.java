package com.footballstats.controller;

import com.footballstats.model.PerformanceTestEntity;
import com.footballstats.repository.CompetitionRepository;
import com.footballstats.repository.PerformanceTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@Profile("!mongodb")
public class PerformanceTestController {

    private static final int TRIALS = 10;

    private final CompetitionRepository competitionRepository;
    private final PerformanceTestRepository performanceTestRepository;

    @GetMapping("/admin/performance/run")
    public String runSelectTests() {
        PerformanceResult result = runMeasuredTest("SELECT_COMPETITIONS", () -> competitionRepository.findAll().size());
        return buildSummary(result);
    }

    @GetMapping("/admin/performance/crud")
    public String runCrudTests(@RequestParam(defaultValue = "1000") int size) {
        performanceTestRepository.deleteAll();

        List<PerformanceResult> results = List.of(
                runInsertTest(size),
                runSelectTest(size),
                runUpdateTest(size),
                runDeleteTest(size)
        );

        StringBuilder response = new StringBuilder();
        response.append("CRUD PERFORMANCE TESTS COMPLETED\n");
        response.append("Size: ").append(size).append("\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n");
        }

        return response.toString();
    }

    @GetMapping("/admin/performance/crud-all")
    public String runAllCrudTests() {
        int[] sizes = {1000, 10000, 100000};

        StringBuilder response = new StringBuilder();
        response.append("ALL CRUD TESTS COMPLETED\n\n");

        for (int size : sizes) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("STARTING CRUD TEST FOR SIZE = " + size);
            System.out.println("========================================");

            performanceTestRepository.deleteAll();

            response.append("========================================\n");
            response.append("CRUD TEST SIZE = ").append(size).append("\n");
            response.append("========================================\n\n");

            List<PerformanceResult> results = List.of(
                    runInsertTest(size),
                    runSelectTest(size),
                    runUpdateTest(size),
                    runDeleteTest(size)
            );

            for (PerformanceResult result : results) {
                response.append(buildSummary(result)).append("\n");
            }
        }

        return response.toString();
    }

    private PerformanceResult runInsertTest(int size) {
        return runMeasuredTest("CRUD_INSERT_" + size, () -> {
            performanceTestRepository.deleteAll();

            List<PerformanceTestEntity> rows = new ArrayList<>();

            for (int i = 1; i <= size; i++) {
                PerformanceTestEntity entity = new PerformanceTestEntity();
                entity.setTestName("TEST_" + i);
                entity.setTestValue(i);
                rows.add(entity);
            }

            performanceTestRepository.saveAll(rows);
            return size;
        });
    }

    private PerformanceResult runSelectTest(int size) {
        return runMeasuredTest("CRUD_SELECT_" + size, () -> performanceTestRepository.findAll().size());
    }

    private PerformanceResult runUpdateTest(int size) {
        return runMeasuredTest("CRUD_UPDATE_" + size, () -> {
            List<PerformanceTestEntity> rows = performanceTestRepository.findAll();

            for (PerformanceTestEntity row : rows) {
                row.setTestValue(row.getTestValue() + 1);
            }

            performanceTestRepository.saveAll(rows);
            return rows.size();
        });
    }

    private PerformanceResult runDeleteTest(int size) {
        return runMeasuredTest("CRUD_DELETE_" + size, () -> {
            prepareRowsForDelete(size);
            performanceTestRepository.deleteAll();
            return size;
        });
    }

    private void prepareRowsForDelete(int size) {
        performanceTestRepository.deleteAll();

        List<PerformanceTestEntity> rows = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            PerformanceTestEntity entity = new PerformanceTestEntity();
            entity.setTestName("DELETE_TEST_" + i);
            entity.setTestValue(i);
            rows.add(entity);
        }

        performanceTestRepository.saveAll(rows);
    }

    private PerformanceResult runMeasuredTest(String testName, MeasuredOperation operation) {
        List<Long> durationsMs = new ArrayList<>();
        List<Long> results = new ArrayList<>();

        System.out.println();
        System.out.println("========================================");
        System.out.println("STARTING PERFORMANCE TEST: " + testName);
        System.out.println("trials=" + TRIALS);
        System.out.println("========================================");

        for (int trial = 1; trial <= TRIALS; trial++) {
            long start = System.nanoTime();
            long result = operation.execute();
            long end = System.nanoTime();

            long durationMs = (end - start) / 1_000_000;

            durationsMs.add(durationMs);
            results.add(result);

            log(testName, trial, result, durationMs);
        }

        PerformanceResult performanceResult = new PerformanceResult(testName, durationsMs, results);
        System.out.println(buildOneLineSummary(performanceResult));

        return performanceResult;
    }

    private String buildSummary(PerformanceResult result) {
        long minDuration = result.durationsMs().stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);

        long maxDuration = result.durationsMs().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        double avgDuration = result.durationsMs().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long lastResult = result.results().isEmpty()
                ? 0
                : result.results().get(result.results().size() - 1);

        double avgResult = result.results().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        double throughput = avgDuration > 0
                ? avgResult / (avgDuration / 1000.0)
                : 0;

        StringBuilder trials = new StringBuilder();

        for (int i = 0; i < result.durationsMs().size(); i++) {
            trials.append("trial=")
                    .append(i + 1)
                    .append(" | result=")
                    .append(result.results().get(i))
                    .append(" | durationMs=")
                    .append(result.durationsMs().get(i))
                    .append("\n");
        }

        return """
                PERFORMANCE TEST SUMMARY
                Test name: %s
                Trials: %d

                Last result: %d
                Average result: %.2f

                Average duration: %.2f ms
                Min duration: %d ms
                Max duration: %d ms
                Average throughput: %.2f records/s

                Detailed trials:
                %s
                """
                .formatted(
                        result.testName(),
                        result.durationsMs().size(),
                        lastResult,
                        avgResult,
                        avgDuration,
                        minDuration,
                        maxDuration,
                        throughput,
                        trials.toString()
                );
    }

    private String buildOneLineSummary(PerformanceResult result) {
        double avgDuration = result.durationsMs().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long minDuration = result.durationsMs().stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);

        long maxDuration = result.durationsMs().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        return "PERFORMANCE_TEST_SUMMARY | "
                + result.testName()
                + " | trials=" + result.durationsMs().size()
                + " | avgDurationMs=" + String.format(Locale.US, "%.2f", avgDuration)
                + " | minDurationMs=" + minDuration
                + " | maxDurationMs=" + maxDuration;
    }

    private void log(String testName, int trial, long result, long durationMs) {
        System.out.println(
                "PERFORMANCE_TEST | " + testName
                        + " | trial=" + trial
                        + " | result=" + result
                        + " | durationMs=" + durationMs
        );
    }

    @FunctionalInterface
    private interface MeasuredOperation {
        long execute();
    }

    private record PerformanceResult(
            String testName,
            List<Long> durationsMs,
            List<Long> results
    ) {
    }
}
