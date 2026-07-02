package com.footballstats.performance.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class RedisPerformanceTestController {

    private static final int TRIALS = 10;
    private static final String KEY_PREFIX = "performance:test:";

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/admin/performance/crud")
    public String runCrudTests(@RequestParam(defaultValue = "1000") int size) {
        clearKeys();

        runInsertTest(size);
        runSelectTest(size);
        runUpdateTest(size);
        runDeleteTest(size);

        return "REDIS CRUD performance tests completed";
    }

    @GetMapping("/admin/performance/crud-all")
    public String runAllCrudTests() {
        int[] sizes = {1000, 10000, 100000};

        for (int size : sizes) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("STARTING REDIS CRUD TEST FOR SIZE = " + size);
            System.out.println("========================================");

            clearKeys();

            runInsertTest(size);
            runSelectTest(size);
            runUpdateTest(size);
            runDeleteTest(size);
        }

        return "ALL REDIS CRUD TESTS COMPLETED";
    }

    private void runInsertTest(int size) {
        for (int trial = 1; trial <= TRIALS; trial++) {
            clearKeys();

            Map<String, Object> rows = new HashMap<>();

            for (int i = 1; i <= size; i++) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("testName", "TEST_" + trial + "_" + i);
                entity.put("testValue", i);

                rows.put(KEY_PREFIX + i, entity);
            }

            long start = System.nanoTime();
            redisTemplate.opsForValue().multiSet(rows);
            long end = System.nanoTime();

            System.out.println("PERFORMANCE_TEST | REDIS_CRUD_INSERT_" + size
                    + " | trial=" + trial
                    + " | count=" + size
                    + " | durationMs=" + ((end - start) / 1_000_000));
        }
    }

    private void runSelectTest(int size) {
        for (int trial = 1; trial <= TRIALS; trial++) {
            List<String> keys = buildKeys(size);

            long start = System.nanoTime();
            List<Object> rows = redisTemplate.opsForValue().multiGet(keys);
            long end = System.nanoTime();

            int count = rows == null ? 0 : rows.size();

            System.out.println("PERFORMANCE_TEST | REDIS_CRUD_SELECT_" + size
                    + " | trial=" + trial
                    + " | count=" + count
                    + " | durationMs=" + ((end - start) / 1_000_000));
        }
    }

    @SuppressWarnings("unchecked")
    private void runUpdateTest(int size) {
        for (int trial = 1; trial <= TRIALS; trial++) {
            List<String> keys = buildKeys(size);
            List<Object> rows = redisTemplate.opsForValue().multiGet(keys);

            long start = System.nanoTime();

            Map<String, Object> updatedRows = new HashMap<>();

            if (rows != null) {
                for (int i = 0; i < rows.size(); i++) {
                    Object value = rows.get(i);

                    if (value instanceof Map<?, ?> rawMap) {
                        Map<String, Object> row = new HashMap<>((Map<String, Object>) rawMap);
                        Object testValue = row.get("testValue");

                        if (testValue instanceof Integer integerValue) {
                            row.put("testValue", integerValue + 1);
                        } else if (testValue instanceof Number numberValue) {
                            row.put("testValue", numberValue.intValue() + 1);
                        }

                        updatedRows.put(KEY_PREFIX + (i + 1), row);
                    }
                }
            }

            if (!updatedRows.isEmpty()) {
                redisTemplate.opsForValue().multiSet(updatedRows);
            }

            long end = System.nanoTime();

            System.out.println("PERFORMANCE_TEST | REDIS_CRUD_UPDATE_" + size
                    + " | trial=" + trial
                    + " | count=" + updatedRows.size()
                    + " | durationMs=" + ((end - start) / 1_000_000));
        }
    }

    private void runDeleteTest(int size) {
        for (int trial = 1; trial <= TRIALS; trial++) {
            prepareRowsForDelete(size);
            List<String> keys = buildKeys(size);

            long start = System.nanoTime();
            redisTemplate.delete(keys);
            long end = System.nanoTime();

            System.out.println("PERFORMANCE_TEST | REDIS_CRUD_DELETE_" + size
                    + " | trial=" + trial
                    + " | count=" + size
                    + " | durationMs=" + ((end - start) / 1_000_000));
        }
    }

    private void prepareRowsForDelete(int size) {
        clearKeys();

        Map<String, Object> rows = new HashMap<>();

        for (int i = 1; i <= size; i++) {
            Map<String, Object> entity = new HashMap<>();
            entity.put("testName", "DELETE_TEST_" + i);
            entity.put("testValue", i);

            rows.put(KEY_PREFIX + i, entity);
        }

        redisTemplate.opsForValue().multiSet(rows);
    }

    private List<String> buildKeys(int size) {
        List<String> keys = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            keys.add(KEY_PREFIX + i);
        }

        return keys;
    }

    private void clearKeys() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
