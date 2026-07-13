package com.footballstats.performance.mongo;

import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.RealTeam;
import com.footballstats.service.PlayerGenerator;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequiredArgsConstructor
@Profile("mongodb")
public class MongoPerformanceTestController {

    private static final String SEASON = "2025/2026";
    private static final int TRIALS = 10;

    private static final String C_PERFORMANCE = "performance_test";
    private static final String C_COMPETITIONS = "competitions";
    private static final String C_REAL_TEAMS = "realTeam";
    private static final String C_REFEREES = "referee";
    private static final String C_GENERATED_PLAYERS = "generatedPlayer";
    private static final String C_MATCHES = "match";
    private static final String C_MATCH_EVENTS = "matchEvent";
    private static final String C_EUROPEAN_MATCHES = "europeanMatch";
    private static final String C_EUROPEAN_MATCH_EVENTS = "europeanMatchEvent";
    private static final String C_EUROPEAN_KNOCKOUT = "europeanKnockoutMatch";

    private static final String[] LEAGUES = {
            "england", "spain", "germany", "italy", "france",
            "portugal", "netherlands", "belgium", "scotland", "turkiye",
            "poland", "austria", "croatia", "czech-republic", "denmark",
            "greece", "norway", "sweden", "switzerland", "serbia",
            "romania", "bulgaria", "ukraine", "russia", "israel",
            "hungary", "moldova", "republic-of-ireland"
    };

    private static final String[] EUROPEAN_COMPETITIONS = {
            "champions-league", "europa-league", "conference-league"
    };

    private static final Set<String> LEAGUE_LOGO_SLUGS = Set.of(
            "premierleague",
            "laliga",
            "bundesliga",
            "seriea",
            "ligue1",
            "ligaportugal",
            "eredivisie",
            "proleague",
            "scottishpremiership",
            "superlig",
            "pkobpekstraklasa",
            "austriabundesliga",
            "supersporthnl",
            "chanceliga",
            "superliga",
            "superleague1",
            "eliteserien",
            "allsvenskan",
            "superleague",
            "superligaserbije",
            "romaniasuperliga",
            "efbetliga",
            "premierliga",
            "ligathaai",
            "russiapremierliga",
            "nemzetibanjnoksag",
            "irelandpremierdivision"
    );

    private final MongoPerformanceTestRepository performanceTestRepository;
    private final MongoTemplate mongoTemplate;

    private final PlayerGenerator playerGenerator = new PlayerGenerator();
    private final Random random = new Random();

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
        response.append("MONGODB CRUD PERFORMANCE TESTS COMPLETED\n");
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
        response.append("MONGODB ALL CRUD TESTS COMPLETED\n\n");

        for (int size : sizes) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("STARTING MONGODB CRUD TEST FOR SIZE = " + size);
            System.out.println("========================================");

            performanceTestRepository.deleteAll();

            response.append("========================================\n");
            response.append("MONGODB CRUD TEST SIZE = ").append(size).append("\n");
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

    @GetMapping("/admin/performance/app-flow-mongodb")
    public String runMongoApplicationFlowPerformanceTests() {
        ensureCompetitions();
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
                runSelectSampleEuropeanMatchEvents(),
                runQueryPaginationMatches(),
                runQueryJoinMatchesEventsPlayers(),
                runQuerySortFilterPlayersByValue()
        );

        StringBuilder response = new StringBuilder();
        response.append("MONGODB FULL APP FLOW PERFORMANCE TESTS COMPLETED\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
    }

    @GetMapping("/admin/performance/setup-mongodb")
    public String setupMongoPerformanceData() {
        ensureCompetitions();
        ensureReferees();
        ensureRealTeams();

        return """
                MONGODB SETUP PERFORMANCE DATA COMPLETED

                Prepared:
                - competitions
                - referees
                - real teams

                Main endpoint:
                /admin/performance/app-flow-mongodb

                Individual endpoints:
                /admin/performance/generate-players-mongodb
                /admin/performance/generate-leagues-mongodb
                /admin/performance/calculate-league-tables-mongodb
                /admin/performance/select-league-matches-mongodb
                /admin/performance/select-match-events-mongodb
                /admin/performance/select-players-mongodb
                /admin/performance/generate-europe-mongodb
                /admin/performance/calculate-europe-tables-mongodb
                /admin/performance/build-europe-rounds-mongodb
                /admin/performance/generate-europe-knockout-mongodb
                /admin/performance/select-europe-events-mongodb
                /admin/performance/query-pagination-mongodb
                /admin/performance/query-join-mongodb
                /admin/performance/query-sort-filter-mongodb
                """;
    }

    @GetMapping("/admin/performance/reset-mongodb")
    public String resetMongoPerformanceData() {
        resetApplicationCollections();

        return """
                MONGODB PERFORMANCE DATA RESET COMPLETED

                Removed collections:
                - generatedPlayer
                - match
                - matchEvent
                - europeanMatch
                - europeanMatchEvent
                - europeanKnockoutMatch
                """;
    }

    @GetMapping("/admin/performance/counts-mongodb")
    public String mongoPerformanceCounts() {
        return """
                MONGODB PERFORMANCE DATA COUNTS

                competitions: %d
                realTeams: %d
                referees: %d
                generatedPlayers: %d
                matches: %d
                matchEvents: %d
                europeanMatches: %d
                europeanMatchEvents: %d
                europeanKnockoutMatches: %d
                """
                .formatted(
                        countCollection(C_COMPETITIONS),
                        countCollection(C_REAL_TEAMS),
                        countCollection(C_REFEREES),
                        countCollection(C_GENERATED_PLAYERS),
                        countCollection(C_MATCHES),
                        countCollection(C_MATCH_EVENTS),
                        countCollection(C_EUROPEAN_MATCHES),
                        countCollection(C_EUROPEAN_MATCH_EVENTS),
                        countCollection(C_EUROPEAN_KNOCKOUT)
                );
    }

    @GetMapping("/admin/performance/app-flow-clean-mongodb")
    public String runMongoApplicationFlowCleanPerformanceTests() {
        resetApplicationCollections();
        ensureCompetitions();
        ensureReferees();
        ensureRealTeams();

        return runMongoApplicationFlowPerformanceTests();
    }

    @GetMapping("/admin/performance/generate-players-mongodb")
    public String generatePlayersMongoPerformance() {
        ensureRealTeams();
        return buildSummary(runGeneratePlayersForAllTeams());
    }

    @GetMapping("/admin/performance/generate-leagues-mongodb")
    public String generateLeaguesMongoPerformance() {
        ensureRealTeams();
        ensureReferees();
        return buildSummary(runGenerateAllLeagueSeasons());
    }

    @GetMapping("/admin/performance/calculate-league-tables-mongodb")
    public String calculateLeagueTablesMongoPerformance() {
        return buildSummary(runCalculateAllLeagueTables());
    }

    @GetMapping("/admin/performance/select-league-matches-mongodb")
    public String selectLeagueMatchesMongoPerformance() {
        return buildSummary(runSelectAllLeagueMatches());
    }

    @GetMapping("/admin/performance/select-match-events-mongodb")
    public String selectMatchEventsMongoPerformance() {
        return buildSummary(runSelectSampleMatchEvents());
    }

    @GetMapping("/admin/performance/select-players-mongodb")
    public String selectPlayersMongoPerformance() {
        return buildSummary(runSelectAllGeneratedPlayers());
    }

    @GetMapping("/admin/performance/generate-europe-mongodb")
    public String generateEuropeMongoPerformance() {
        return buildSummary(runGenerateAllEuropeanCompetitions());
    }

    @GetMapping("/admin/performance/calculate-europe-tables-mongodb")
    public String calculateEuropeTablesMongoPerformance() {
        return buildSummary(runCalculateAllEuropeanTables());
    }

    @GetMapping("/admin/performance/build-europe-rounds-mongodb")
    public String buildEuropeRoundsMongoPerformance() {
        return buildSummary(runBuildAllEuropeanRoundViews());
    }

    @GetMapping("/admin/performance/generate-europe-knockout-mongodb")
    public String generateEuropeKnockoutMongoPerformance() {
        return buildSummary(runGenerateAllEuropeanKnockouts());
    }

    @GetMapping("/admin/performance/select-europe-events-mongodb")
    public String selectEuropeEventsMongoPerformance() {
        return buildSummary(runSelectSampleEuropeanMatchEvents());
    }

    @GetMapping("/admin/performance/query-pagination-mongodb")
    public String queryPaginationMongoPerformance() {
        return buildSummary(runQueryPaginationMatches());
    }

    @GetMapping("/admin/performance/query-join-mongodb")
    public String queryJoinMongoPerformance() {
        return buildSummary(runQueryJoinMatchesEventsPlayers());
    }

    @GetMapping("/admin/performance/query-sort-filter-mongodb")
    public String querySortFilterMongoPerformance() {
        return buildSummary(runQuerySortFilterPlayersByValue());
    }

    @GetMapping("/admin/performance/query-advanced-mongodb")
    public String queryAdvancedMongoPerformance() {
        List<PerformanceResult> results = List.of(
                runQueryPaginationMatches(),
                runQueryJoinMatchesEventsPlayers(),
                runQuerySortFilterPlayersByValue()
        );

        StringBuilder response = new StringBuilder();
        response.append("MONGODB ADVANCED QUERY PERFORMANCE TESTS COMPLETED\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
    }

    private PerformanceResult runInsertTest(int size) {
        return runMeasuredTest("MONGODB_CRUD_INSERT_" + size, () -> {
            performanceTestRepository.deleteAll();

            List<PerformanceTestMongoEntity> rows = new ArrayList<>();

            for (int i = 1; i <= size; i++) {
                PerformanceTestMongoEntity entity = new PerformanceTestMongoEntity();
                entity.setTestName("TEST_" + i);
                entity.setTestValue(i);
                rows.add(entity);
            }

            performanceTestRepository.saveAll(rows);
            return size;
        });
    }

    private PerformanceResult runSelectTest(int size) {
        return runMeasuredTest("MONGODB_CRUD_SELECT_" + size, () -> performanceTestRepository.findAll().size());
    }

    private PerformanceResult runUpdateTest(int size) {
        return runMeasuredTest("MONGODB_CRUD_UPDATE_" + size, () -> {
            List<PerformanceTestMongoEntity> rows = performanceTestRepository.findAll();

            for (PerformanceTestMongoEntity row : rows) {
                row.setTestValue(row.getTestValue() + 1);
            }

            performanceTestRepository.saveAll(rows);
            return rows.size();
        });
    }

    private PerformanceResult runDeleteTest(int size) {
        return runMeasuredTest("MONGODB_CRUD_DELETE_" + size, () -> {
            prepareRowsForDelete(size);
            performanceTestRepository.deleteAll();
            return size;
        });
    }

    private void prepareRowsForDelete(int size) {
        performanceTestRepository.deleteAll();

        List<PerformanceTestMongoEntity> rows = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            PerformanceTestMongoEntity entity = new PerformanceTestMongoEntity();
            entity.setTestName("DELETE_TEST_" + i);
            entity.setTestValue(i);
            rows.add(entity);
        }

        performanceTestRepository.saveAll(rows);
    }

    private PerformanceResult runGeneratePlayersForAllTeams() {
        return runMeasuredTest("MONGODB_APP_FLOW_GENERATE_PLAYERS_ALL", () -> {
            mongoTemplate.dropCollection(C_GENERATED_PLAYERS);

            AtomicLong playerId = new AtomicLong(1);
            long insertedPlayers = 0;

            List<Document> teams = mongoTemplate.findAll(Document.class, C_REAL_TEAMS);

            for (Document teamDoc : teams) {
                RealTeam team = toRealTeam(teamDoc);
                List<GeneratedPlayer> squad = playerGenerator.generateSquadForTeam(team);

                List<Document> playerDocs = squad.stream()
                        .map(player -> toPlayerDocument(playerId.getAndIncrement(), player))
                        .toList();

                if (!playerDocs.isEmpty()) {
                    mongoTemplate.insert(playerDocs, C_GENERATED_PLAYERS);
                    insertedPlayers += playerDocs.size();
                }
            }

            return insertedPlayers;
        });
    }

    private PerformanceResult runGenerateAllLeagueSeasons() {
        return runMeasuredTest("MONGODB_APP_FLOW_GENERATE_ALL_LEAGUE_SEASONS", () -> {
            mongoTemplate.dropCollection(C_MATCHES);
            mongoTemplate.dropCollection(C_MATCH_EVENTS);

            AtomicLong matchId = new AtomicLong(1);
            AtomicLong eventId = new AtomicLong(1);
            long totalMatches = 0;

            for (String league : LEAGUES) {
                List<Document> teams = findTeamsByLeague(league);
                int round = 1;

                for (int i = 0; i < teams.size(); i++) {
                    for (int j = i + 1; j < teams.size(); j++) {
                        Document home = teams.get(i);
                        Document away = teams.get(j);

                        long firstMatchId = matchId.getAndIncrement();
                        Document firstMatch = createLeagueMatch(firstMatchId, league, home, away, round++);
                        mongoTemplate.insert(firstMatch, C_MATCHES);
                        insertMatchEvents(eventId, firstMatchId, firstMatch, false);
                        totalMatches++;

                        long secondMatchId = matchId.getAndIncrement();
                        Document secondMatch = createLeagueMatch(secondMatchId, league, away, home, round++);
                        mongoTemplate.insert(secondMatch, C_MATCHES);
                        insertMatchEvents(eventId, secondMatchId, secondMatch, false);
                        totalMatches++;
                    }
                }
            }

            return totalMatches;
        });
    }

    private PerformanceResult runCalculateAllLeagueTables() {
        return runMeasuredTest("MONGODB_APP_FLOW_CALCULATE_ALL_LEAGUE_TABLES", () -> {
            long totalRows = 0;

            for (String league : LEAGUES) {
                List<Document> teams = findTeamsByLeague(league);
                Map<String, TableRow> table = new LinkedHashMap<>();

                for (Document team : teams) {
                    table.put(team.getString("clubSlug"), new TableRow());
                }

                List<Document> matches = mongoTemplate.find(
                        Query.query(Criteria.where("league").is(league)),
                        Document.class,
                        C_MATCHES
                );

                for (Document match : matches) {
                    String homeSlug = match.getString("homeClubSlug");
                    String awaySlug = match.getString("awayClubSlug");
                    int homeGoals = match.getInteger("homeGoals", 0);
                    int awayGoals = match.getInteger("awayGoals", 0);

                    TableRow home = table.get(homeSlug);
                    TableRow away = table.get(awaySlug);

                    if (home == null || away == null) {
                        continue;
                    }

                    home.played++;
                    away.played++;
                    home.goalsFor += homeGoals;
                    home.goalsAgainst += awayGoals;
                    away.goalsFor += awayGoals;
                    away.goalsAgainst += homeGoals;

                    if (homeGoals > awayGoals) {
                        home.points += 3;
                        home.wins++;
                        away.losses++;
                    } else if (homeGoals < awayGoals) {
                        away.points += 3;
                        away.wins++;
                        home.losses++;
                    } else {
                        home.points++;
                        away.points++;
                        home.draws++;
                        away.draws++;
                    }
                }

                totalRows += table.size();
            }

            return totalRows;
        });
    }

    private PerformanceResult runSelectAllLeagueMatches() {
        return runMeasuredTest("MONGODB_APP_FLOW_SELECT_ALL_LEAGUE_MATCHES", () -> {
            long totalMatches = 0;

            for (String league : LEAGUES) {
                Query query = Query.query(Criteria.where("league").is(league))
                        .with(Sort.by(Sort.Direction.ASC, "roundNumber", "matchDate"));
                totalMatches += mongoTemplate.find(query, Document.class, C_MATCHES).size();
            }

            return totalMatches;
        });
    }

    private PerformanceResult runSelectSampleMatchEvents() {
        return runMeasuredTest("MONGODB_APP_FLOW_SELECT_SAMPLE_MATCH_EVENTS", () -> {
            Document match = mongoTemplate.findOne(new Query().limit(1), Document.class, C_MATCHES);

            if (match == null) {
                return 0;
            }

            Query query = Query.query(Criteria.where("matchId").is(match.getLong("_id")))
                    .with(Sort.by(Sort.Direction.ASC, "eventMinute"));

            return mongoTemplate.find(query, Document.class, C_MATCH_EVENTS).size();
        });
    }

    private PerformanceResult runSelectAllGeneratedPlayers() {
        return runMeasuredTest("MONGODB_APP_FLOW_SELECT_ALL_GENERATED_PLAYERS", () ->
                mongoTemplate.findAll(Document.class, C_GENERATED_PLAYERS).size()
        );
    }

    private PerformanceResult runGenerateAllEuropeanCompetitions() {
        return runMeasuredTest("MONGODB_APP_FLOW_GENERATE_ALL_EUROPEAN_COMPETITIONS", () -> {
            mongoTemplate.dropCollection(C_EUROPEAN_MATCHES);
            mongoTemplate.dropCollection(C_EUROPEAN_MATCH_EVENTS);

            AtomicLong matchId = new AtomicLong(1);
            AtomicLong eventId = new AtomicLong(1);
            long totalMatches = 0;

            List<Document> allTeams = mongoTemplate.findAll(Document.class, C_REAL_TEAMS);

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<Document> participants = allTeams.stream().limit(36).toList();

                for (int round = 1; round <= 8; round++) {
                    for (int i = 0; i < participants.size(); i += 2) {
                        Document home = participants.get((i + round - 1) % participants.size());
                        Document away = participants.get((i + round) % participants.size());

                        long id = matchId.getAndIncrement();
                        Document match = createEuropeanMatch(id, competition, home, away, round);
                        mongoTemplate.insert(match, C_EUROPEAN_MATCHES);
                        insertMatchEvents(eventId, id, match, true);
                        totalMatches++;
                    }
                }
            }

            return totalMatches;
        });
    }

    private PerformanceResult runCalculateAllEuropeanTables() {
        return runMeasuredTest("MONGODB_APP_FLOW_CALCULATE_ALL_EUROPEAN_TABLES", () -> {
            long totalRows = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<Document> matches = mongoTemplate.find(
                        Query.query(Criteria.where("competitionSlug").is(competition)),
                        Document.class,
                        C_EUROPEAN_MATCHES
                );

                Map<String, TableRow> table = new LinkedHashMap<>();

                for (Document match : matches) {
                    table.putIfAbsent(match.getString("homeClubSlug"), new TableRow());
                    table.putIfAbsent(match.getString("awayClubSlug"), new TableRow());

                    TableRow home = table.get(match.getString("homeClubSlug"));
                    TableRow away = table.get(match.getString("awayClubSlug"));

                    int homeGoals = match.getInteger("homeGoals", 0);
                    int awayGoals = match.getInteger("awayGoals", 0);

                    home.played++;
                    away.played++;
                    home.goalsFor += homeGoals;
                    home.goalsAgainst += awayGoals;
                    away.goalsFor += awayGoals;
                    away.goalsAgainst += homeGoals;

                    if (homeGoals > awayGoals) {
                        home.points += 3;
                        home.wins++;
                        away.losses++;
                    } else if (homeGoals < awayGoals) {
                        away.points += 3;
                        away.wins++;
                        home.losses++;
                    } else {
                        home.points++;
                        away.points++;
                        home.draws++;
                        away.draws++;
                    }
                }

                totalRows += table.size();
            }

            return totalRows;
        });
    }

    private PerformanceResult runBuildAllEuropeanRoundViews() {
        return runMeasuredTest("MONGODB_APP_FLOW_BUILD_ALL_EUROPEAN_ROUND_VIEWS", () -> {
            long totalRounds = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                for (int round = 1; round <= 8; round++) {
                    Query query = Query.query(
                                    Criteria.where("competitionSlug").is(competition)
                                            .and("roundNumber").is(round)
                            )
                            .with(Sort.by(Sort.Direction.ASC, "matchDate"));
                    mongoTemplate.find(query, Document.class, C_EUROPEAN_MATCHES);
                    totalRounds++;
                }
            }

            return totalRounds;
        });
    }

    private PerformanceResult runGenerateAllEuropeanKnockouts() {
        return runMeasuredTest("MONGODB_APP_FLOW_GENERATE_ALL_EUROPEAN_KNOCKOUTS", () -> {
            mongoTemplate.dropCollection(C_EUROPEAN_KNOCKOUT);

            AtomicLong knockoutId = new AtomicLong(1);
            long totalKnockoutMatches = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<Document> teams = mongoTemplate.findAll(Document.class, C_REAL_TEAMS)
                        .stream()
                        .limit(36)
                        .toList();

                if (teams.size() < 24) {
                    continue;
                }

                totalKnockoutMatches += createKnockoutRound(knockoutId, competition, "PLAY-OFF", 1, teams, 8, 24, 8);
                totalKnockoutMatches += createKnockoutRound(knockoutId, competition, "ROUND-OF-16", 2, teams, 0, 8, 8);
                totalKnockoutMatches += createKnockoutRound(knockoutId, competition, "QUARTER-FINAL", 3, teams, 0, 4, 4);
                totalKnockoutMatches += createKnockoutRound(knockoutId, competition, "SEMI-FINAL", 4, teams, 0, 2, 2);
                totalKnockoutMatches += createKnockoutRound(knockoutId, competition, "FINAL", 5, teams, 0, 1, 1);
            }

            return totalKnockoutMatches;
        });
    }

    private long createKnockoutRound(
            AtomicLong knockoutId,
            String competition,
            String round,
            int roundOrder,
            List<Document> teams,
            int startIndex,
            int awayStartIndex,
            int matchesCount
    ) {
        long created = 0;

        for (int i = 0; i < matchesCount; i++) {
            Document home = teams.get((startIndex + i) % teams.size());
            Document away = teams.get((awayStartIndex - 1 - i + teams.size()) % teams.size());

            Document match = new Document("_id", knockoutId.getAndIncrement())
                    .append("season", SEASON)
                    .append("competitionSlug", competition)
                    .append("round", round)
                    .append("roundOrder", roundOrder)
                    .append("bracketOrder", i + 1)
                    .append("homeTeam", home.getString("apiTeamName"))
                    .append("awayTeam", away.getString("apiTeamName"))
                    .append("homeClubSlug", home.getString("clubSlug"))
                    .append("awayClubSlug", away.getString("clubSlug"))
                    .append("homeLeagueSlug", home.getString("leagueSlug"))
                    .append("awayLeagueSlug", away.getString("leagueSlug"))
                    .append("homeGoals", random.nextInt(5))
                    .append("awayGoals", random.nextInt(5))
                    .append("matchDate", LocalDateTime.now().plusDays(roundOrder * 7L + i))
                    .append("status", "FINISHED");

            mongoTemplate.insert(match, C_EUROPEAN_KNOCKOUT);
            created++;
        }

        return created;
    }

    private PerformanceResult runSelectSampleEuropeanMatchEvents() {
        return runMeasuredTest("MONGODB_APP_FLOW_SELECT_SAMPLE_EUROPEAN_MATCH_EVENTS", () -> {
            Document match = mongoTemplate.findOne(new Query().limit(1), Document.class, C_EUROPEAN_MATCHES);

            if (match == null) {
                return 0;
            }

            Query query = Query.query(Criteria.where("matchId").is(match.getLong("_id")))
                    .with(Sort.by(Sort.Direction.ASC, "eventMinute"));

            return mongoTemplate.find(query, Document.class, C_EUROPEAN_MATCH_EVENTS).size();
        });
    }

    private PerformanceResult runQueryPaginationMatches() {
        return runMeasuredTest("MONGODB_QUERY_PAGINATION_MATCHES", () -> {
            Query query = new Query()
                    .skip(1000)
                    .limit(100)
                    .with(Sort.by(Sort.Direction.ASC, "roundNumber", "matchDate"));

            return mongoTemplate.find(query, Document.class, C_MATCHES).size();
        });
    }

    private PerformanceResult runQueryJoinMatchesEventsPlayers() {
        return runMeasuredTest("MONGODB_QUERY_COMPLEX_JOIN_MATCHES_EVENTS_PLAYERS", () -> {
            List<Document> events = mongoTemplate.find(new Query().limit(1000), Document.class, C_MATCH_EVENTS);
            long rows = 0;

            for (Document event : events) {
                Long matchId = event.getLong("matchId");
                Long playerId = event.getLong("playerId");

                Document match = mongoTemplate.findById(matchId, Document.class, C_MATCHES);
                Document player = mongoTemplate.findById(playerId, Document.class, C_GENERATED_PLAYERS);

                if (match != null && player != null) {
                    rows++;
                }
            }

            return rows;
        });
    }

    private PerformanceResult runQuerySortFilterPlayersByValue() {
        return runMeasuredTest("MONGODB_QUERY_SORT_FILTER_PLAYERS_BY_VALUE", () -> {
            Query query = Query.query(
                            Criteria.where("marketValue").ne(null)
                                    .and("leagueSlug").is("england")
                    )
                    .with(Sort.by(Sort.Direction.DESC, "marketValue"))
                    .limit(100);

            return mongoTemplate.find(query, Document.class, C_GENERATED_PLAYERS).size();
        });
    }

    private Document createLeagueMatch(long id, String league, Document home, Document away, int roundNumber) {
        return new Document("_id", id)
                .append("season", SEASON)
                .append("league", league)
                .append("homeTeam", home.getString("apiTeamName"))
                .append("awayTeam", away.getString("apiTeamName"))
                .append("homeClubSlug", home.getString("clubSlug"))
                .append("awayClubSlug", away.getString("clubSlug"))
                .append("homeGoals", random.nextInt(5))
                .append("awayGoals", random.nextInt(5))
                .append("roundNumber", roundNumber)
                .append("matchDate", LocalDateTime.now().plusDays(roundNumber))
                .append("status", "FINISHED");
    }

    private Document createEuropeanMatch(long id, String competition, Document home, Document away, int roundNumber) {
        return new Document("_id", id)
                .append("season", SEASON)
                .append("competitionSlug", competition)
                .append("homeTeam", home.getString("apiTeamName"))
                .append("awayTeam", away.getString("apiTeamName"))
                .append("homeClubSlug", home.getString("clubSlug"))
                .append("awayClubSlug", away.getString("clubSlug"))
                .append("homeLeagueSlug", home.getString("leagueSlug"))
                .append("awayLeagueSlug", away.getString("leagueSlug"))
                .append("homeGoals", random.nextInt(5))
                .append("awayGoals", random.nextInt(5))
                .append("roundNumber", roundNumber)
                .append("matchDate", LocalDateTime.now().plusDays(roundNumber))
                .append("status", "FINISHED");
    }

    private void insertMatchEvents(AtomicLong eventId, long matchId, Document match, boolean european) {
        String collection = european ? C_EUROPEAN_MATCH_EVENTS : C_MATCH_EVENTS;
        int homeGoals = match.getInteger("homeGoals", 0);
        int awayGoals = match.getInteger("awayGoals", 0);
        int goals = homeGoals + awayGoals;

        List<Document> players = mongoTemplate.find(
                Query.query(Criteria.where("clubSlug").in(match.getString("homeClubSlug"), match.getString("awayClubSlug"))).limit(40),
                Document.class,
                C_GENERATED_PLAYERS
        );

        if (players.isEmpty()) {
            return;
        }

        List<Document> events = new ArrayList<>();

        for (int i = 0; i < goals; i++) {
            Document player = players.get(random.nextInt(players.size()));

            events.add(new Document("_id", eventId.getAndIncrement())
                    .append("matchId", matchId)
                    .append("playerId", player.getLong("_id"))
                    .append("playerName", player.getString("firstName") + " " + player.getString("lastName"))
                    .append("clubSlug", player.getString("clubSlug"))
                    .append("eventType", "GOAL")
                    .append("eventMinute", random.nextInt(1, 91)));
        }

        int cards = random.nextInt(0, 4);
        for (int i = 0; i < cards; i++) {
            Document player = players.get(random.nextInt(players.size()));

            events.add(new Document("_id", eventId.getAndIncrement())
                    .append("matchId", matchId)
                    .append("playerId", player.getLong("_id"))
                    .append("playerName", player.getString("firstName") + " " + player.getString("lastName"))
                    .append("clubSlug", player.getString("clubSlug"))
                    .append("eventType", "YELLOW_CARD")
                    .append("eventMinute", random.nextInt(1, 91)));
        }

        if (!events.isEmpty()) {
            mongoTemplate.insert(events, collection);
        }
    }

    private void ensureCompetitions() {
        if (mongoTemplate.collectionExists(C_COMPETITIONS) && mongoTemplate.count(new Query(), C_COMPETITIONS) > 0) {
            return;
        }

        mongoTemplate.dropCollection(C_COMPETITIONS);

        List<Document> competitions = List.of(
                competition(1, "Premier League", "england", "DOMESTIC_LEAGUE"),
                competition(2, "LaLiga", "spain", "DOMESTIC_LEAGUE"),
                competition(3, "Bundesliga", "germany", "DOMESTIC_LEAGUE"),
                competition(4, "Serie A", "italy", "DOMESTIC_LEAGUE"),
                competition(5, "Ligue 1", "france", "DOMESTIC_LEAGUE"),
                competition(6, "Liga Portugal", "portugal", "DOMESTIC_LEAGUE"),
                competition(7, "Eredivisie", "netherlands", "DOMESTIC_LEAGUE"),
                competition(8, "Jupiler Pro League", "belgium", "DOMESTIC_LEAGUE"),
                competition(9, "Scottish Premiership", "scotland", "DOMESTIC_LEAGUE"),
                competition(10, "Super Lig", "turkiye", "DOMESTIC_LEAGUE"),
                competition(11, "Ekstraklasa", "poland", "DOMESTIC_LEAGUE"),
                competition(12, "Austria Bundesliga", "austria", "DOMESTIC_LEAGUE"),
                competition(13, "SuperSport HNL", "croatia", "DOMESTIC_LEAGUE"),
                competition(14, "Chance Liga", "czech-republic", "DOMESTIC_LEAGUE"),
                competition(15, "Superliga", "denmark", "DOMESTIC_LEAGUE"),
                competition(16, "Super League Greece", "greece", "DOMESTIC_LEAGUE"),
                competition(17, "Eliteserien", "norway", "DOMESTIC_LEAGUE"),
                competition(18, "Allsvenskan", "sweden", "DOMESTIC_LEAGUE"),
                competition(19, "Swiss Super League", "switzerland", "DOMESTIC_LEAGUE"),
                competition(20, "Super Liga Srbije", "serbia", "DOMESTIC_LEAGUE"),
                competition(21, "SuperLiga Romania", "romania", "DOMESTIC_LEAGUE"),
                competition(22, "efbet Liga", "bulgaria", "DOMESTIC_LEAGUE"),
                competition(23, "Premier Liga Ukraine", "ukraine", "DOMESTIC_LEAGUE"),
                competition(24, "Premier Liga Russia", "russia", "DOMESTIC_LEAGUE"),
                competition(25, "Ligat ha Al", "israel", "DOMESTIC_LEAGUE"),
                competition(26, "Nemzeti Bajnokság", "hungary", "DOMESTIC_LEAGUE"),
                competition(27, "Moldovan Super Liga", "moldova", "DOMESTIC_LEAGUE"),
                competition(28, "Ireland Premier Division", "republic-of-ireland", "DOMESTIC_LEAGUE"),
                competition(29, "Champions League", "champions-league", "EUROPEAN_CUP"),
                competition(30, "Europa League", "europa-league", "EUROPEAN_CUP"),
                competition(31, "Conference League", "conference-league", "EUROPEAN_CUP")
        );

        mongoTemplate.insert(competitions, C_COMPETITIONS);
        System.out.println("PERFORMANCE_TEST | MONGODB_SETUP_COMPETITIONS | count=" + competitions.size());
    }

    private Document competition(long id, String name, String slug, String type) {
        return new Document("_id", id)
                .append("name", name)
                .append("slug", slug)
                .append("season", SEASON)
                .append("type", type);
    }

    private void ensureRealTeams() {
        if (mongoTemplate.collectionExists(C_REAL_TEAMS) && mongoTemplate.count(new Query(), C_REAL_TEAMS) > 0) {
            return;
        }

        mongoTemplate.dropCollection(C_REAL_TEAMS);

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
        leagueFolders.put("Turkiye - Super Lig", "turkiye");
        leagueFolders.put("Poland - PKO BP Ekstraklasa", "poland");
        leagueFolders.put("Austria - Bundesliga", "austria");
        leagueFolders.put("Croatia - SuperSport HNL", "croatia");
        leagueFolders.put("Czech Republic - Chance Liga", "czech-republic");
        leagueFolders.put("Denmark - Superliga", "denmark");
        leagueFolders.put("Greece - Super League 1", "greece");
        leagueFolders.put("Norway - Eliteserien", "norway");
        leagueFolders.put("Sweden - Allsvenskan", "sweden");
        leagueFolders.put("Switzerland - Super League", "switzerland");
        leagueFolders.put("Serbia - Super liga Srbije", "serbia");
        leagueFolders.put("Romania - SuperLiga", "romania");
        leagueFolders.put("Bulgaria - efbet Liga", "bulgaria");
        leagueFolders.put("Ukraine - Premier Liga", "ukraine");
        leagueFolders.put("Russia - Premier Liga", "russia");
        leagueFolders.put("Israel - Ligat ha Al", "israel");
        leagueFolders.put("Hungary - Nemezeti Bajnoksag", "hungary");
        leagueFolders.put("Moldova - Superliga", "moldova");
        leagueFolders.put("Republic of Ireland - Ireland Premier Division", "republic-of-ireland");

        List<Document> teams = new ArrayList<>();
        AtomicLong id = new AtomicLong(1);

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

                if (LEAGUE_LOGO_SLUGS.contains(clubSlug)) {
                    continue;
                }

                boolean exists = teams.stream().anyMatch(team ->
                        leagueSlug.equals(team.getString("leagueSlug")) && clubSlug.equals(team.getString("clubSlug"))
                );

                if (exists) {
                    continue;
                }

                teams.add(new Document("_id", id.getAndIncrement())
                        .append("leagueSlug", leagueSlug)
                        .append("clubSlug", clubSlug)
                        .append("apiTeamId", Math.abs((leagueSlug + "-" + clubSlug).hashCode()))
                        .append("apiTeamName", clubName)
                        .append("apiLogo", "/Images/leagues/" + folder + "/" + fileName));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load league logo resources for RealTeam seed.", e);
        }

        if (!teams.isEmpty()) {
            mongoTemplate.insert(teams, C_REAL_TEAMS);
        }

        System.out.println("PERFORMANCE_TEST | MONGODB_SETUP_REAL_TEAMS | count=" + teams.size());
    }

    private void ensureReferees() {
        if (mongoTemplate.collectionExists(C_REFEREES) && mongoTemplate.count(new Query(), C_REFEREES) > 0) {
            return;
        }

        mongoTemplate.dropCollection(C_REFEREES);

        String[] names = {
                "Szymon Marciniak", "François Letexier", "Clément Turpin", "Benoît Bastien",
                "Michael Oliver", "Anthony Taylor", "Chris Kavanagh", "Felix Zwayer",
                "Daniel Siebert", "Tobias Stieler", "Sven Jablonski", "Danny Makkelie",
                "Serdar Gözübüyük", "Jesús Gil Manzano", "José María Sánchez Martínez",
                "Alejandro Hernández Hernández", "Davide Massa", "Maurizio Mariani", "Marco Guida",
                "Slavko Vinčić", "Rade Obrenović", "Glenn Nyberg", "Sandro Schärer",
                "István Kovács", "Ivan Kružliak", "Espen Eskås", "Irfan Peljto",
                "João Pinheiro", "Halil Umut Meler", "Orel Grinfeld", "Erik Lambrechts",
                "Donatas Rumšas", "Nick Walsh", "Damian Sylwestrzak", "Paweł Raczkowski",
                "Stéphanie Frappart", "Jérôme Brisard", "Ricardo de Burgos Bengoetxea",
                "Juan Martínez Munuera", "Simone Sozza"
        };

        List<Document> referees = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            referees.add(new Document("_id", (long) i + 1)
                    .append("name", names[i])
                    .append("countryCode", "UEFA")
                    .append("active", true));
        }

        mongoTemplate.insert(referees, C_REFEREES);
        System.out.println("PERFORMANCE_TEST | MONGODB_SETUP_REFEREES | count=" + names.length);
    }

    private void resetApplicationCollections() {
        mongoTemplate.dropCollection(C_COMPETITIONS);
        mongoTemplate.dropCollection(C_REAL_TEAMS);
        mongoTemplate.dropCollection(C_REFEREES);
        mongoTemplate.dropCollection(C_GENERATED_PLAYERS);
        mongoTemplate.dropCollection(C_MATCHES);
        mongoTemplate.dropCollection(C_MATCH_EVENTS);
        mongoTemplate.dropCollection(C_EUROPEAN_MATCHES);
        mongoTemplate.dropCollection(C_EUROPEAN_MATCH_EVENTS);
        mongoTemplate.dropCollection(C_EUROPEAN_KNOCKOUT);
    }

    private long countCollection(String collectionName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            return 0;
        }

        return mongoTemplate.count(new Query(), collectionName);
    }

    private List<Document> findTeamsByLeague(String league) {
        return mongoTemplate.find(
                Query.query(Criteria.where("leagueSlug").is(league))
                        .with(Sort.by(Sort.Direction.ASC, "apiTeamName")),
                Document.class,
                C_REAL_TEAMS
        );
    }

    private RealTeam toRealTeam(Document document) {
        RealTeam team = new RealTeam();
        team.setLeagueSlug(document.getString("leagueSlug"));
        team.setClubSlug(document.getString("clubSlug"));
        team.setApiTeamId(document.getInteger("apiTeamId"));
        team.setApiTeamName(document.getString("apiTeamName"));
        team.setApiLogo(document.getString("apiLogo"));
        return team;
    }

    private Document toPlayerDocument(long id, GeneratedPlayer player) {
        return new Document("_id", id)
                .append("firstName", player.getFirstName())
                .append("lastName", player.getLastName())
                .append("birthDate", player.getBirthDate())
                .append("age", player.getAge())
                .append("height", player.getHeight())
                .append("nationality", player.getNationality())
                .append("countryCode", player.getCountryCode())
                .append("position", player.getPosition())
                .append("preferredFoot", player.getPreferredFoot())
                .append("shirtNumber", player.getShirtNumber())
                .append("currentClub", player.getCurrentClub())
                .append("clubSlug", player.getClubSlug())
                .append("leagueSlug", player.getLeagueSlug())
                .append("teamId", player.getTeamId())
                .append("joinedClubDate", player.getJoinedClubDate())
                .append("contractUntil", player.getContractUntil())
                .append("lastContractExtension", player.getLastContractExtension())
                .append("marketValue", toLong(player.getMarketValue()))
                .append("active", player.isActive())
                .append("imageUrl", player.getImageUrl())
                .append("injuredMatches", player.getInjuredMatches())
                .append("suspendedMatches", player.getSuspendedMatches());
    }

    private long toLong(BigDecimal value) {
        return value == null ? 0L : value.longValue();
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

    private static class TableRow {
        int played;
        int wins;
        int draws;
        int losses;
        int goalsFor;
        int goalsAgainst;
        int points;
    }
}
