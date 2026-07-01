package com.footballstats.controller;
import org.springframework.transaction.annotation.Transactional;
import com.footballstats.dto.EuropeanLeaguePhaseRoundView;
import com.footballstats.dto.EuropeanLeagueTableRow;
import com.footballstats.dto.LeagueTableRow;
import com.footballstats.model.*;
import com.footballstats.repository.*;
import com.footballstats.service.LeagueTableService;
import com.footballstats.service.MatchGeneratorService;
import com.footballstats.service.PlayerGenerator;
import com.footballstats.service.europe.EuropeanCompetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PerformanceTestController {

    private static final String SEASON = "2025/2026";
    private static final int TRIALS = 10;

    private static final String[] LEAGUES = {
            "england", "spain", "germany", "italy", "france",
            "portugal", "netherlands", "belgium", "scotland", "turkiye",
            "poland", "austria", "croatia", "czech-republic", "denmark",
            "greece", "norway", "sweden", "switzerland", "serbia",
            "romania", "bulgaria", "ukraine", "russia", "israel"
    };

    private static final String[] EUROPEAN_COMPETITIONS = {
            "champions-league",
            "europa-league",
            "conference-league"
    };

    private final MatchGeneratorService matchGeneratorService;
    private final LeagueTableService leagueTableService;
    private final EuropeanCompetitionService europeanCompetitionService;

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final RealTeamRepository realTeamRepository;
    private final EuropeanMatchRepository europeanMatchRepository;
    private final EuropeanMatchEventRepository europeanMatchEventRepository;
    private final EuropeanKnockoutMatchRepository europeanKnockoutMatchRepository;
    private final RefereeRepository refereeRepository;

    private final PlayerGenerator playerGenerator = new PlayerGenerator();

    @GetMapping("/admin/performance/app-flow")
    public String runApplicationFlowPerformanceTests() {
        ensureReferees();
        ensureRealTeams();

        List<PerformanceResult> results = List.of(
                runGeneratePlayersForAllTeams(),
                runGenerateAllLeagueSeasons(),
                runCalculateAllLeagueTables(),
                runSelectAllLeagueMatches(),
                runSelectSampleMatchEvents(),
                runSelectAllGeneratedPlayers(),
                runGenerateAllEuropeanCompetitions(),
                runCalculateAllEuropeanTables(),
                runBuildAllEuropeanRoundViews(),
                runGenerateAllEuropeanKnockouts(),
                runSelectSampleEuropeanMatchEvents()
        );

        StringBuilder response = new StringBuilder();
        response.append("FULL APP FLOW PERFORMANCE TESTS COMPLETED\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
    }

    @GetMapping("/admin/performance/setup")
    public String setupPerformanceData() {
        ensureReferees();
        ensureRealTeams();

        return """
                SETUP PERFORMANCE DATA COMPLETED

                Prepared:
                - referees
                - real teams

                Next endpoints:
                /admin/performance/generate-players
                /admin/performance/generate-leagues
                /admin/performance/calculate-league-tables
                /admin/performance/select-league-matches
                /admin/performance/select-match-events
                /admin/performance/select-players
                /admin/performance/generate-europe
                /admin/performance/calculate-europe-tables
                /admin/performance/build-europe-rounds
                /admin/performance/generate-europe-knockout
                /admin/performance/select-europe-events
                """;
    }

    @GetMapping("/admin/performance/generate-players")
    public String generatePlayersPerformance() {
        return buildSummary(runGeneratePlayersForAllTeams());
    }

    @GetMapping("/admin/performance/generate-leagues")
    public String generateLeaguesPerformance() {
        return buildSummary(runGenerateAllLeagueSeasons());
    }

    @GetMapping("/admin/performance/calculate-league-tables")
    public String calculateLeagueTablesPerformance() {
        return buildSummary(runCalculateAllLeagueTables());
    }

    @GetMapping("/admin/performance/select-league-matches")
    public String selectLeagueMatchesPerformance() {
        return buildSummary(runSelectAllLeagueMatches());
    }

    @GetMapping("/admin/performance/select-match-events")
    public String selectMatchEventsPerformance() {
        return buildSummary(runSelectSampleMatchEvents());
    }

    @GetMapping("/admin/performance/select-players")
    public String selectPlayersPerformance() {
        return buildSummary(runSelectAllGeneratedPlayers());
    }

    @GetMapping("/admin/performance/generate-europe")
    public String generateEuropePerformance() {
        return buildSummary(runGenerateAllEuropeanCompetitions());
    }

    @GetMapping("/admin/performance/calculate-europe-tables")
    public String calculateEuropeTablesPerformance() {
        return buildSummary(runCalculateAllEuropeanTables());
    }

    @GetMapping("/admin/performance/build-europe-rounds")
    public String buildEuropeRoundsPerformance() {
        return buildSummary(runBuildAllEuropeanRoundViews());
    }

    @GetMapping("/admin/performance/generate-europe-knockout")
    public String generateEuropeKnockoutPerformance() {
        return buildSummary(runGenerateAllEuropeanKnockouts());
    }

    @GetMapping("/admin/performance/select-europe-events")
    public String selectEuropeEventsPerformance() {
        return buildSummary(runSelectSampleEuropeanMatchEvents());
    }

    private PerformanceResult runGeneratePlayersForAllTeams() {
        return runMeasuredTest("APP_FLOW_GENERATE_PLAYERS_ALL", () -> {
            generatedPlayerRepository.deleteAll();

            long insertedPlayers = 0;
            List<RealTeam> teams = realTeamRepository.findAll();

            for (RealTeam team : teams) {
                List<GeneratedPlayer> squad = playerGenerator.generateSquadForTeam(team);
                generatedPlayerRepository.saveAll(squad);
                insertedPlayers += squad.size();
            }

            return insertedPlayers;
        });
    }

    private PerformanceResult runGenerateAllLeagueSeasons() {
        return runMeasuredTest("APP_FLOW_GENERATE_ALL_LEAGUE_SEASONS", () -> {
            for (String league : LEAGUES) {
                matchGeneratorService.generateLeagueSeason(league);
            }

            return matchRepository.count();
        });
    }

    private PerformanceResult runCalculateAllLeagueTables() {
        return runMeasuredTest("APP_FLOW_CALCULATE_ALL_LEAGUE_TABLES", () -> {
            long totalRows = 0;

            for (String league : LEAGUES) {
                List<Match> matches = matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(league);
                List<LeagueTableRow> table = leagueTableService.calculateTable(matches, league);
                totalRows += table.size();
            }

            return totalRows;
        });
    }

    private PerformanceResult runSelectAllLeagueMatches() {
        return runMeasuredTest("APP_FLOW_SELECT_ALL_LEAGUE_MATCHES", () -> {
            long totalMatches = 0;

            for (String league : LEAGUES) {
                totalMatches += matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(league).size();
            }

            return totalMatches;
        });
    }

    private PerformanceResult runSelectSampleMatchEvents() {
        return runMeasuredTest("APP_FLOW_SELECT_SAMPLE_MATCH_EVENTS", () -> {
            Long matchId = getFirstLeagueMatchId();

            if (matchId == null || matchId < 0) {
                return 0;
            }

            List<MatchEvent> events = matchEventRepository.findByMatchIdOrderByEventMinuteAsc(matchId);
            return events.size();
        });
    }

    private PerformanceResult runSelectAllGeneratedPlayers() {
        return runMeasuredTest("APP_FLOW_SELECT_ALL_GENERATED_PLAYERS", () -> {
            List<GeneratedPlayer> players = generatedPlayerRepository.findAll();
            return players.size();
        });
    }

    private PerformanceResult runGenerateAllEuropeanCompetitions() {
        return runMeasuredTest("APP_FLOW_GENERATE_ALL_EUROPEAN_COMPETITIONS", () -> {
            for (String competition : EUROPEAN_COMPETITIONS) {
                europeanCompetitionService.generateOrRegenerateEuropeanCompetition(SEASON, competition);
            }

            return europeanMatchRepository.count();
        });
    }

    private PerformanceResult runCalculateAllEuropeanTables() {
        return runMeasuredTest("APP_FLOW_CALCULATE_ALL_EUROPEAN_TABLES", () -> {
            long totalRows = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<EuropeanLeagueTableRow> table =
                        europeanCompetitionService.calculateLeaguePhaseTable(SEASON, competition);
                totalRows += table.size();
            }

            return totalRows;
        });
    }

    private PerformanceResult runBuildAllEuropeanRoundViews() {
        return runMeasuredTest("APP_FLOW_BUILD_ALL_EUROPEAN_ROUND_VIEWS", () -> {
            long totalRounds = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<EuropeanLeaguePhaseRoundView> rounds =
                        europeanCompetitionService.buildLeaguePhaseRoundViews(SEASON, competition);
                totalRounds += rounds.size();
            }

            return totalRounds;
        });
    }

    @Transactional
    private PerformanceResult runGenerateAllEuropeanKnockouts() {
        return runMeasuredTest("APP_FLOW_GENERATE_ALL_EUROPEAN_KNOCKOUTS", () -> {
            long totalKnockoutMatches = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                deleteEuropeanKnockoutData(SEASON, competition);

                europeanCompetitionService.generateKnockoutStage(SEASON, competition);

                List<EuropeanKnockoutMatch> knockoutMatches =
                        europeanCompetitionService.getKnockoutMatches(SEASON, competition);

                totalKnockoutMatches += knockoutMatches.size();
            }

            return totalKnockoutMatches;
        });
    }

    private PerformanceResult runSelectSampleEuropeanMatchEvents() {
        return runMeasuredTest("APP_FLOW_SELECT_SAMPLE_EUROPEAN_MATCH_EVENTS", () -> {
            Long europeanMatchId = getFirstEuropeanMatchId();

            if (europeanMatchId == null || europeanMatchId < 0) {
                return 0;
            }

            List<EuropeanMatchEvent> events =
                    europeanMatchEventRepository.findByMatchIdOrderByEventMinuteAsc(europeanMatchId);

            return events.size();
        });
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

    private void deleteEuropeanKnockoutData(String season, String competitionSlug) {
        List<Long> knockoutMatchIds = europeanKnockoutMatchRepository
                .findBySeasonAndCompetitionSlugOrderByRoundOrderAscBracketOrderAsc(season, competitionSlug)
                .stream()
                .map(EuropeanKnockoutMatch::getId)
                .toList();

        if (!knockoutMatchIds.isEmpty()) {
            europeanMatchEventRepository.deleteByKnockoutMatchIdIn(knockoutMatchIds);
        }

        europeanKnockoutMatchRepository.deleteBySeasonAndCompetitionSlug(season, competitionSlug);
    }

    private Long getFirstLeagueMatchId() {
        return matchRepository.findAll()
                .stream()
                .findFirst()
                .map(Match::getId)
                .orElse(-1L);
    }

    private Long getFirstEuropeanMatchId() {
        return europeanMatchRepository.findAll()
                .stream()
                .findFirst()
                .map(EuropeanMatch::getId)
                .orElse(-1L);
    }

    private void ensureRealTeams() {
        if (realTeamRepository.count() > 0) {
            return;
        }

        Map<String, String> leagueFolders = new LinkedHashMap<>();
        leagueFolders.put("England - Premier League", "england");
        leagueFolders.put("Spain - LaLiga", "spain");
        leagueFolders.put("Germany - Bundesliga", "germany");
        leagueFolders.put("Italy - Serie A", "italy");
        leagueFolders.put("France - Ligue 1", "france");
        leagueFolders.put("Portugal - Liga Portugal", "portugal");
        leagueFolders.put("Netherlands - Eredivisie", "netherlands");
        leagueFolders.put("Belgium - Jupiler Pro League", "belgium");
        leagueFolders.put("Scotland - Scottish Premiership", "scotland");
        leagueFolders.put("Turkey - Super Lig", "turkiye");
        leagueFolders.put("Poland - Ekstraklasa", "poland");
        leagueFolders.put("Austria - Bundesliga", "austria");
        leagueFolders.put("Croatia - SuperSport HNL", "croatia");
        leagueFolders.put("Czech Republic - Chance Liga", "czech-republic");
        leagueFolders.put("Denmark - Superliga", "denmark");
        leagueFolders.put("Greece - Super League Greece", "greece");
        leagueFolders.put("Norway - Eliteserien", "norway");
        leagueFolders.put("Sweden - Allsvenskan", "sweden");
        leagueFolders.put("Switzerland - Super League", "switzerland");
        leagueFolders.put("Serbia - Super Liga Srbije", "serbia");
        leagueFolders.put("Romania - SuperLiga", "romania");
        leagueFolders.put("Bulgaria - efbet Liga", "bulgaria");
        leagueFolders.put("Ukraine - Premier Liga", "ukraine");
        leagueFolders.put("Russia - Premier Liga", "russia");
        leagueFolders.put("Israel - Ligat HaAl", "israel");

        int insertedTeams = 0;

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/static/Images/leagues/*/*.png");

            for (Resource resource : resources) {
                String folder = extractLeagueFolder(resource);
                if (folder == null || !leagueFolders.containsKey(folder)) {
                    continue;
                }

                String leagueSlug = leagueFolders.get(folder);
                String fileName = resource.getFilename();

                if (fileName == null || !fileName.endsWith(".png")) {
                    continue;
                }

                String clubName = fileName.substring(0, fileName.length() - 4);
                String clubSlug = slugify(clubName);

                if (realTeamRepository.existsByLeagueSlugAndClubSlug(leagueSlug, clubSlug)) {
                    continue;
                }

                RealTeam team = new RealTeam();
                team.setLeagueSlug(leagueSlug);
                team.setClubSlug(clubSlug);
                team.setApiTeamName(clubName);
                team.setApiTeamId(Math.abs((leagueSlug + "-" + clubSlug).hashCode()));
                team.setApiLogo("/Images/leagues/" + folder + "/" + fileName);

                realTeamRepository.save(team);
                insertedTeams++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load league logo resources for RealTeam seed.", e);
        }

        System.out.println("PERFORMANCE_TEST | SETUP_REAL_TEAMS | count=" + insertedTeams);
    }

    private String extractLeagueFolder(Resource resource) throws IOException {
        String url = URLDecoder.decode(resource.getURL().toString(), StandardCharsets.UTF_8);
        url = url.replace("\\", "/");

        String marker = "/static/Images/leagues/";
        int markerIndex = url.indexOf(marker);

        if (markerIndex < 0) {
            return null;
        }

        String rest = url.substring(markerIndex + marker.length());
        int slashIndex = rest.indexOf("/");

        if (slashIndex < 0) {
            return null;
        }

        return rest.substring(0, slashIndex);
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .toLowerCase(Locale.ROOT)
                .replace("&", "and")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
    }

    private void ensureReferees() {
        if (refereeRepository.count() > 0) {
            return;
        }

        String[] names = {
                "Szymon Marciniak",
                "François Letexier",
                "Clément Turpin",
                "Benoît Bastien",
                "Michael Oliver",
                "Anthony Taylor",
                "Chris Kavanagh",
                "Felix Zwayer",
                "Daniel Siebert",
                "Tobias Stieler",
                "Sven Jablonski",
                "Danny Makkelie",
                "Serdar Gözübüyük",
                "Jesús Gil Manzano",
                "José María Sánchez Martínez",
                "Alejandro Hernández Hernández",
                "Davide Massa",
                "Maurizio Mariani",
                "Marco Guida",
                "Slavko Vinčić",
                "Rade Obrenović",
                "Glenn Nyberg",
                "Sandro Schärer",
                "István Kovács",
                "Ivan Kružliak",
                "Espen Eskås",
                "Irfan Peljto",
                "João Pinheiro",
                "Halil Umut Meler",
                "Orel Grinfeld",
                "Erik Lambrechts",
                "Donatas Rumšas",
                "Nick Walsh",
                "Damian Sylwestrzak",
                "Paweł Raczkowski",
                "Stéphanie Frappart",
                "Jérôme Brisard",
                "Ricardo de Burgos Bengoetxea",
                "Juan Martínez Munuera",
                "Simone Sozza"
        };

        for (String name : names) {
            Referee referee = new Referee();
            referee.setName(name);
            referee.setCountryCode("UEFA");
            referee.setActive(true);
            refereeRepository.save(referee);
        }

        System.out.println("PERFORMANCE_TEST | SETUP_REFEREES | count=" + names.length);
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
