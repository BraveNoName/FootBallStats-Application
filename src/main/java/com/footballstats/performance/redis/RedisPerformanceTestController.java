package com.footballstats.performance.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.RealTeam;
import com.footballstats.service.PlayerGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequiredArgsConstructor
@Profile("redis")
public class RedisPerformanceTestController {

    private static final String SEASON = "2025/2026";
    private static final int TRIALS = 10;

    private static final String K_COMPETITIONS = "redis:competitions:";
    private static final String K_REAL_TEAMS = "redis:realTeam:";
    private static final String K_REFEREES = "redis:referee:";
    private static final String K_GENERATED_PLAYERS = "redis:generatedPlayer:";
    private static final String K_MATCHES = "redis:match:";
    private static final String K_MATCH_EVENTS = "redis:matchEvent:";
    private static final String K_EUROPEAN_MATCHES = "redis:europeanMatch:";
    private static final String K_EUROPEAN_MATCH_EVENTS = "redis:europeanMatchEvent:";
    private static final String K_EUROPEAN_KNOCKOUT = "redis:europeanKnockoutMatch:";

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
            "nemzetibajnoksag",
            "irelandpremierdivision"
    );

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final PlayerGenerator playerGenerator = new PlayerGenerator();
    private final Random random = new Random();

    @GetMapping("/admin/performance/app-flow-redis")
    public String runRedisApplicationFlowPerformanceTests() {
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
        response.append("REDIS FULL APP FLOW PERFORMANCE TESTS COMPLETED\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
    }

    @GetMapping("/admin/performance/setup-redis")
    public String setupRedisPerformanceData() {
        ensureCompetitions();
        ensureReferees();
        ensureRealTeams();

        return """
                REDIS SETUP PERFORMANCE DATA COMPLETED

                Prepared:
                - competitions
                - referees
                - real teams

                Main endpoint:
                /admin/performance/app-flow-redis
                """;
    }

    @GetMapping("/admin/performance/generate-players-redis")
    public String generatePlayersRedisPerformance() {
        ensureRealTeams();
        return buildSummary(runGeneratePlayersForAllTeams());
    }

    @GetMapping("/admin/performance/generate-leagues-redis")
    public String generateLeaguesRedisPerformance() {
        ensureRealTeams();
        ensureReferees();
        return buildSummary(runGenerateAllLeagueSeasons());
    }

    @GetMapping("/admin/performance/select-players-redis")
    public String selectPlayersRedisPerformance() {
        return buildSummary(runSelectAllGeneratedPlayers());
    }

    @GetMapping("/admin/performance/generate-europe-knockout-redis")
    public String generateEuropeKnockoutRedisPerformance() {
        return buildSummary(runGenerateAllEuropeanKnockouts());
    }

    @GetMapping("/admin/performance/select-europe-events-redis")
    public String selectEuropeEventsRedisPerformance() {
        return buildSummary(runSelectSampleEuropeanMatchEvents());
    }

    @GetMapping("/admin/performance/query-pagination-redis")
    public String queryPaginationRedisPerformance() {
        return buildSummary(runQueryPaginationMatches());
    }

    @GetMapping("/admin/performance/query-join-redis")
    public String queryJoinRedisPerformance() {
        return buildSummary(runQueryJoinMatchesEventsPlayers());
    }

    @GetMapping("/admin/performance/query-sort-filter-redis")
    public String querySortFilterRedisPerformance() {
        return buildSummary(runQuerySortFilterPlayersByValue());
    }

    private PerformanceResult runGeneratePlayersForAllTeams() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_PLAYERS_ALL", () -> {
            deleteByPrefix(K_GENERATED_PLAYERS);

            AtomicLong playerId = new AtomicLong(1);
            long insertedPlayers = 0;

            List<Map<String, Object>> teams = findAll(K_REAL_TEAMS);

            for (Map<String, Object> teamDoc : teams) {
                RealTeam team = toRealTeam(teamDoc);
                List<GeneratedPlayer> squad = playerGenerator.generateSquadForTeam(team);

                Map<String, String> rows = new LinkedHashMap<>();

                for (GeneratedPlayer player : squad) {
                    long id = playerId.getAndIncrement();
                    rows.put(K_GENERATED_PLAYERS + id, toJson(toPlayerMap(id, player)));
                    insertedPlayers++;
                }

                if (!rows.isEmpty()) {
                    redisTemplate.opsForValue().multiSet(rows);
                }
            }

            return insertedPlayers;
        });
    }

    private PerformanceResult runGenerateAllLeagueSeasons() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_ALL_LEAGUE_SEASONS", () -> {
            deleteByPrefix(K_MATCHES);
            deleteByPrefix(K_MATCH_EVENTS);

            AtomicLong matchId = new AtomicLong(1);
            AtomicLong eventId = new AtomicLong(1);
            long totalMatches = 0;

            for (String league : LEAGUES) {
                List<Map<String, Object>> teams = findTeamsByLeague(league);
                int round = 1;

                for (int i = 0; i < teams.size(); i++) {
                    for (int j = i + 1; j < teams.size(); j++) {
                        Map<String, Object> home = teams.get(i);
                        Map<String, Object> away = teams.get(j);

                        long firstMatchId = matchId.getAndIncrement();
                        Map<String, Object> firstMatch = createLeagueMatch(firstMatchId, league, home, away, round++);
                        save(K_MATCHES + firstMatchId, firstMatch);
                        insertMatchEvents(eventId, firstMatchId, firstMatch, false);
                        totalMatches++;

                        long secondMatchId = matchId.getAndIncrement();
                        Map<String, Object> secondMatch = createLeagueMatch(secondMatchId, league, away, home, round++);
                        save(K_MATCHES + secondMatchId, secondMatch);
                        insertMatchEvents(eventId, secondMatchId, secondMatch, false);
                        totalMatches++;
                    }
                }
            }

            return totalMatches;
        });
    }

    private PerformanceResult runCalculateAllLeagueTables() {
        return runMeasuredTest("REDIS_APP_FLOW_CALCULATE_ALL_LEAGUE_TABLES", () -> {
            long totalRows = 0;

            for (String league : LEAGUES) {
                final String currentLeague = league;

                List<Map<String, Object>> teams = findTeamsByLeague(currentLeague);
                Map<String, TableRow> table = new LinkedHashMap<>();

                for (Map<String, Object> team : teams) {
                    table.put(asString(team.get("clubSlug")), new TableRow());
                }

                List<Map<String, Object>> matches = findAll(K_MATCHES).stream()
                        .filter(match -> currentLeague.equals(asString(match.get("league"))))
                        .toList();

                for (Map<String, Object> match : matches) {
                    String homeSlug = asString(match.get("homeClubSlug"));
                    String awaySlug = asString(match.get("awayClubSlug"));
                    int homeGoals = asInt(match.get("homeGoals"));
                    int awayGoals = asInt(match.get("awayGoals"));

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
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_ALL_LEAGUE_MATCHES", () -> {
            long totalMatches = 0;

            for (String league : LEAGUES) {
                final String currentLeague = league;

                List<Map<String, Object>> matches = findAll(K_MATCHES).stream()
                        .filter(match -> currentLeague.equals(asString(match.get("league"))))
                        .sorted(Comparator
                                .comparingInt((Map<String, Object> match) -> asInt(match.get("roundNumber")))
                                .thenComparing(match -> asString(match.get("matchDate"))))
                        .toList();

                totalMatches += matches.size();
            }

            return totalMatches;
        });
    }

    private PerformanceResult runSelectSampleMatchEvents() {

        return runMeasuredTest("REDIS_APP_FLOW_SELECT_SAMPLE_MATCH_EVENTS", () -> {

            Map<String, Object> match = findAll(K_MATCHES)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                return 0;
            }

            long matchId = asLong(match.get("_id"));

            return findAll(K_MATCH_EVENTS)
                    .stream()
                    .filter(event -> asLong(event.get("matchId")) == matchId)
                    .sorted(Comparator.comparingInt(event -> asInt(event.get("eventMinute"))))
                    .count();
        });
    }

    private PerformanceResult runSelectAllGeneratedPlayers() {
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_ALL_GENERATED_PLAYERS", () ->
                findAll(K_GENERATED_PLAYERS).size()
        );
    }

    private PerformanceResult runGenerateAllEuropeanCompetitions() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_ALL_EUROPEAN_COMPETITIONS", () -> {
            deleteByPrefix(K_EUROPEAN_MATCHES);
            deleteByPrefix(K_EUROPEAN_MATCH_EVENTS);

            AtomicLong matchId = new AtomicLong(1);
            AtomicLong eventId = new AtomicLong(1);
            long totalMatches = 0;

            List<Map<String, Object>> allTeams = findAll(K_REAL_TEAMS);

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<Map<String, Object>> participants = allTeams.stream().limit(36).toList();

                for (int round = 1; round <= 8; round++) {
                    for (int i = 0; i < participants.size(); i += 2) {
                        Map<String, Object> home = participants.get((i + round - 1) % participants.size());
                        Map<String, Object> away = participants.get((i + round) % participants.size());

                        long id = matchId.getAndIncrement();
                        Map<String, Object> match = createEuropeanMatch(id, competition, home, away, round);
                        save(K_EUROPEAN_MATCHES + id, match);
                        insertMatchEvents(eventId, id, match, true);
                        totalMatches++;
                    }
                }
            }

            return totalMatches;
        });
    }

    private PerformanceResult runCalculateAllEuropeanTables() {
        return runMeasuredTest("REDIS_APP_FLOW_CALCULATE_ALL_EUROPEAN_TABLES", () -> {
            long totalRows = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                final String currentCompetition = competition;

                List<Map<String, Object>> matches = findAll(K_EUROPEAN_MATCHES).stream()
                        .filter(match -> currentCompetition.equals(asString(match.get("competitionSlug"))))
                        .toList();

                Map<String, TableRow> table = new LinkedHashMap<>();

                for (Map<String, Object> match : matches) {
                    table.putIfAbsent(asString(match.get("homeClubSlug")), new TableRow());
                    table.putIfAbsent(asString(match.get("awayClubSlug")), new TableRow());

                    TableRow home = table.get(asString(match.get("homeClubSlug")));
                    TableRow away = table.get(asString(match.get("awayClubSlug")));

                    int homeGoals = asInt(match.get("homeGoals"));
                    int awayGoals = asInt(match.get("awayGoals"));

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
        return runMeasuredTest("REDIS_APP_FLOW_BUILD_ALL_EUROPEAN_ROUND_VIEWS", () -> {
            long totalRounds = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                final String currentCompetition = competition;

                for (int round = 1; round <= 8; round++) {
                    final int currentRound = round;

                    findAll(K_EUROPEAN_MATCHES).stream()
                            .filter(match -> currentCompetition.equals(asString(match.get("competitionSlug"))))
                            .filter(match -> asInt(match.get("roundNumber")) == currentRound)
                            .sorted(Comparator.comparing(match -> asString(match.get("matchDate"))))
                            .toList();

                    totalRounds++;
                }
            }

            return totalRounds;
        });
    }

    private PerformanceResult runGenerateAllEuropeanKnockouts() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_ALL_EUROPEAN_KNOCKOUTS", () -> {
            deleteByPrefix(K_EUROPEAN_KNOCKOUT);

            AtomicLong knockoutId = new AtomicLong(1);
            long totalKnockoutMatches = 0;

            for (String competition : EUROPEAN_COMPETITIONS) {
                List<Map<String, Object>> teams = findAll(K_REAL_TEAMS).stream()
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
            List<Map<String, Object>> teams,
            int startIndex,
            int awayStartIndex,
            int matchesCount
    ) {
        long created = 0;

        for (int i = 0; i < matchesCount; i++) {
            Map<String, Object> home = teams.get((startIndex + i) % teams.size());
            Map<String, Object> away = teams.get((awayStartIndex - 1 - i + teams.size()) % teams.size());

            long id = knockoutId.getAndIncrement();

            Map<String, Object> match = new LinkedHashMap<>();
            match.put("_id", id);
            match.put("season", SEASON);
            match.put("competitionSlug", competition);
            match.put("round", round);
            match.put("roundOrder", roundOrder);
            match.put("bracketOrder", i + 1);
            match.put("homeTeam", asString(home.get("apiTeamName")));
            match.put("awayTeam", asString(away.get("apiTeamName")));
            match.put("homeClubSlug", asString(home.get("clubSlug")));
            match.put("awayClubSlug", asString(away.get("clubSlug")));
            match.put("homeLeagueSlug", asString(home.get("leagueSlug")));
            match.put("awayLeagueSlug", asString(away.get("leagueSlug")));
            match.put("homeGoals", random.nextInt(5));
            match.put("awayGoals", random.nextInt(5));
            match.put("matchDate", LocalDateTime.now().plusDays(roundOrder * 7L + i).toString());
            match.put("status", "FINISHED");

            save(K_EUROPEAN_KNOCKOUT + id, match);
            created++;
        }

        return created;
    }

    private PerformanceResult runSelectSampleEuropeanMatchEvents() {
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_SAMPLE_EUROPEAN_MATCH_EVENTS", () -> {
            Map<String, Object> match = findAll(K_EUROPEAN_MATCHES).stream().findFirst().orElse(null);

            if (match == null) {
                return 0;
            }

            long matchId = asLong(match.get("_id"));

            return findAll(K_EUROPEAN_MATCH_EVENTS).stream()
                    .filter(event -> asLong(event.get("matchId")) == matchId)
                    .sorted(Comparator.comparingInt(event -> asInt(event.get("eventMinute"))))
                    .count();
        });
    }

    private PerformanceResult runQueryPaginationMatches() {
        return runMeasuredTest("REDIS_QUERY_PAGINATION_MATCHES", () ->
                findAll(K_MATCHES).stream()
                        .sorted(Comparator
                                .comparingInt((Map<String, Object> match) -> asInt(match.get("roundNumber")))
                                .thenComparing(match -> asString(match.get("matchDate"))))
                        .skip(1000)
                        .limit(100)
                        .count()
        );
    }

    private PerformanceResult runQueryJoinMatchesEventsPlayers() {
        return runMeasuredTest("REDIS_QUERY_COMPLEX_JOIN_MATCHES_EVENTS_PLAYERS", () -> {
            List<Map<String, Object>> events = findAll(K_MATCH_EVENTS).stream().limit(1000).toList();
            List<Map<String, Object>> matches = findAll(K_MATCHES);
            List<Map<String, Object>> players = findAll(K_GENERATED_PLAYERS);

            Map<Long, Map<String, Object>> matchesById = new LinkedHashMap<>();
            for (Map<String, Object> match : matches) {
                matchesById.put(asLong(match.get("_id")), match);
            }

            Map<Long, Map<String, Object>> playersById = new LinkedHashMap<>();
            for (Map<String, Object> player : players) {
                playersById.put(asLong(player.get("_id")), player);
            }

            long rows = 0;

            for (Map<String, Object> event : events) {
                long matchId = asLong(event.get("matchId"));
                long playerId = asLong(event.get("playerId"));

                if (matchesById.containsKey(matchId) && playersById.containsKey(playerId)) {
                    rows++;
                }
            }

            return rows;
        });
    }

    private PerformanceResult runQuerySortFilterPlayersByValue() {
        return runMeasuredTest("REDIS_QUERY_SORT_FILTER_PLAYERS_BY_VALUE", () ->
                findAll(K_GENERATED_PLAYERS).stream()
                        .filter(player -> "england".equals(asString(player.get("leagueSlug"))))
                        .filter(player -> asLong(player.get("marketValue")) > 0)
                        .sorted(Comparator.comparingLong((Map<String, Object> player) -> asLong(player.get("marketValue"))).reversed())
                        .limit(100)
                        .count()
        );
    }

    private Map<String, Object> createLeagueMatch(long id, String league, Map<String, Object> home, Map<String, Object> away, int roundNumber) {
        Map<String, Object> match = new LinkedHashMap<>();
        match.put("_id", id);
        match.put("season", SEASON);
        match.put("league", league);
        match.put("homeTeam", asString(home.get("apiTeamName")));
        match.put("awayTeam", asString(away.get("apiTeamName")));
        match.put("homeClubSlug", asString(home.get("clubSlug")));
        match.put("awayClubSlug", asString(away.get("clubSlug")));
        match.put("homeGoals", random.nextInt(5));
        match.put("awayGoals", random.nextInt(5));
        match.put("roundNumber", roundNumber);
        match.put("matchDate", LocalDateTime.now().plusDays(roundNumber).toString());
        match.put("status", "FINISHED");
        return match;
    }

    private Map<String, Object> createEuropeanMatch(long id, String competition, Map<String, Object> home, Map<String, Object> away, int roundNumber) {
        Map<String, Object> match = new LinkedHashMap<>();
        match.put("_id", id);
        match.put("season", SEASON);
        match.put("competitionSlug", competition);
        match.put("homeTeam", asString(home.get("apiTeamName")));
        match.put("awayTeam", asString(away.get("apiTeamName")));
        match.put("homeClubSlug", asString(home.get("clubSlug")));
        match.put("awayClubSlug", asString(away.get("clubSlug")));
        match.put("homeLeagueSlug", asString(home.get("leagueSlug")));
        match.put("awayLeagueSlug", asString(away.get("leagueSlug")));
        match.put("homeGoals", random.nextInt(5));
        match.put("awayGoals", random.nextInt(5));
        match.put("roundNumber", roundNumber);
        match.put("matchDate", LocalDateTime.now().plusDays(roundNumber).toString());
        match.put("status", "FINISHED");
        return match;
    }

    private void insertMatchEvents(AtomicLong eventId, long matchId, Map<String, Object> match, boolean european) {
        String keyPrefix = european ? K_EUROPEAN_MATCH_EVENTS : K_MATCH_EVENTS;
        int homeGoals = asInt(match.get("homeGoals"));
        int awayGoals = asInt(match.get("awayGoals"));
        int goals = homeGoals + awayGoals;

        String homeSlug = asString(match.get("homeClubSlug"));
        String awaySlug = asString(match.get("awayClubSlug"));

        List<Map<String, Object>> players = findAll(K_GENERATED_PLAYERS).stream()
                .filter(player -> homeSlug.equals(asString(player.get("clubSlug"))) || awaySlug.equals(asString(player.get("clubSlug"))))
                .limit(40)
                .toList();

        if (players.isEmpty()) {
            return;
        }

        Map<String, String> rows = new LinkedHashMap<>();

        for (int i = 0; i < goals; i++) {
            Map<String, Object> player = players.get(random.nextInt(players.size()));
            long id = eventId.getAndIncrement();

            Map<String, Object> event = createEvent(id, matchId, player, "GOAL");
            rows.put(keyPrefix + id, toJson(event));
        }

        int cards = random.nextInt(4);
        for (int i = 0; i < cards; i++) {
            Map<String, Object> player = players.get(random.nextInt(players.size()));
            long id = eventId.getAndIncrement();

            Map<String, Object> event = createEvent(id, matchId, player, "YELLOW_CARD");
            rows.put(keyPrefix + id, toJson(event));
        }

        if (!rows.isEmpty()) {
            redisTemplate.opsForValue().multiSet(rows);
        }
    }

    private Map<String, Object> createEvent(long id, long matchId, Map<String, Object> player, String eventType) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("_id", id);
        event.put("matchId", matchId);
        event.put("playerId", asLong(player.get("_id")));
        event.put("playerName", asString(player.get("firstName")) + " " + asString(player.get("lastName")));
        event.put("clubSlug", asString(player.get("clubSlug")));
        event.put("eventType", eventType);
        event.put("eventMinute", random.nextInt(1, 91));
        return event;
    }

    private void ensureCompetitions() {
        if (existsAny(K_COMPETITIONS)) {
            return;
        }

        deleteByPrefix(K_COMPETITIONS);

        List<Map<String, Object>> competitions = List.of(
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
                competition(26, "Champions League", "champions-league", "EUROPEAN_CUP"),
                competition(27, "Europa League", "europa-league", "EUROPEAN_CUP"),
                competition(28, "Conference League", "conference-league", "EUROPEAN_CUP")
        );

        Map<String, String> rows = new LinkedHashMap<>();

        for (Map<String, Object> competition : competitions) {
            rows.put(K_COMPETITIONS + competition.get("_id"), toJson(competition));
        }

        redisTemplate.opsForValue().multiSet(rows);
        System.out.println("PERFORMANCE_TEST | REDIS_SETUP_COMPETITIONS | count=" + competitions.size());
    }

    private Map<String, Object> competition(long id, String name, String slug, String type) {
        Map<String, Object> competition = new LinkedHashMap<>();
        competition.put("_id", id);
        competition.put("name", name);
        competition.put("slug", slug);
        competition.put("season", SEASON);
        competition.put("type", type);
        return competition;
    }

    private void ensureRealTeams() {
        if (existsAny(K_REAL_TEAMS)) {
            return;
        }

        deleteByPrefix(K_REAL_TEAMS);

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

        Map<String, String> rows = new LinkedHashMap<>();
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

                boolean exists = rows.values().stream()
                        .map(this::fromJson)
                        .anyMatch(team -> leagueSlug.equals(asString(team.get("leagueSlug"))) && clubSlug.equals(asString(team.get("clubSlug"))));

                if (exists) {
                    continue;
                }

                long teamId = id.getAndIncrement();

                Map<String, Object> team = new LinkedHashMap<>();
                team.put("_id", teamId);
                team.put("leagueSlug", leagueSlug);
                team.put("clubSlug", clubSlug);
                team.put("apiTeamId", Math.abs((leagueSlug + "-" + clubSlug).hashCode()));
                team.put("apiTeamName", clubName);
                team.put("apiLogo", "/Images/leagues/" + folder + "/" + fileName);

                rows.put(K_REAL_TEAMS + teamId, toJson(team));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load league logo resources for RealTeam seed.", e);
        }

        if (!rows.isEmpty()) {
            redisTemplate.opsForValue().multiSet(rows);
        }

        System.out.println("PERFORMANCE_TEST | REDIS_SETUP_REAL_TEAMS | count=" + rows.size());
    }

    private void ensureReferees() {
        if (existsAny(K_REFEREES)) {
            return;
        }

        deleteByPrefix(K_REFEREES);

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

        Map<String, String> rows = new LinkedHashMap<>();

        for (int i = 0; i < names.length; i++) {
            long id = i + 1L;

            Map<String, Object> referee = new LinkedHashMap<>();
            referee.put("_id", id);
            referee.put("name", names[i]);
            referee.put("countryCode", "UEFA");
            referee.put("active", true);

            rows.put(K_REFEREES + id, toJson(referee));
        }

        redisTemplate.opsForValue().multiSet(rows);
        System.out.println("PERFORMANCE_TEST | REDIS_SETUP_REFEREES | count=" + names.length);
    }

    private List<Map<String, Object>> findTeamsByLeague(String league) {
        return findAll(K_REAL_TEAMS).stream()
                .filter(team -> league.equals(asString(team.get("leagueSlug"))))
                .sorted(Comparator.comparing(team -> asString(team.get("apiTeamName"))))
                .toList();
    }

    private RealTeam toRealTeam(Map<String, Object> document) {
        RealTeam team = new RealTeam();
        team.setLeagueSlug(asString(document.get("leagueSlug")));
        team.setClubSlug(asString(document.get("clubSlug")));
        team.setApiTeamId(asInt(document.get("apiTeamId")));
        team.setApiTeamName(asString(document.get("apiTeamName")));
        team.setApiLogo(asString(document.get("apiLogo")));
        return team;
    }

    private Map<String, Object> toPlayerMap(long id, GeneratedPlayer player) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_id", id);
        map.put("firstName", player.getFirstName());
        map.put("lastName", player.getLastName());
        map.put("birthDate", player.getBirthDate() == null ? null : player.getBirthDate().toString());
        map.put("age", player.getAge());
        map.put("height", player.getHeight());
        map.put("nationality", player.getNationality());
        map.put("countryCode", player.getCountryCode());
        map.put("position", player.getPosition());
        map.put("preferredFoot", player.getPreferredFoot());
        map.put("shirtNumber", player.getShirtNumber());
        map.put("currentClub", player.getCurrentClub());
        map.put("clubSlug", player.getClubSlug());
        map.put("leagueSlug", player.getLeagueSlug());
        map.put("teamId", player.getTeamId());
        map.put("joinedClubDate", player.getJoinedClubDate() == null ? null : player.getJoinedClubDate().toString());
        map.put("contractUntil", player.getContractUntil() == null ? null : player.getContractUntil().toString());
        map.put("lastContractExtension", player.getLastContractExtension() == null ? null : player.getLastContractExtension().toString());
        map.put("marketValue", toLong(player.getMarketValue()));
        map.put("active", player.isActive());
        map.put("imageUrl", player.getImageUrl());
        map.put("injuredMatches", player.getInjuredMatches());
        map.put("suspendedMatches", player.getSuspendedMatches());
        return map;
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

    private boolean existsAny(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys != null && !keys.isEmpty();
    }

    private void save(String key, Map<String, Object> value) {
        redisTemplate.opsForValue().set(key, toJson(value));
    }

    private List<Map<String, Object>> findAll(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");

        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        List<String> sortedKeys = keys.stream().sorted().toList();
        List<String> values = redisTemplate.opsForValue().multiGet(sortedKeys);

        if (values == null) {
            return List.of();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (String value : values) {
            if (value != null) {
                result.add(fromJson(value));
            }
        }

        return result;
    }

    private void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize Redis value", e);
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot deserialize Redis value", e);
        }
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value == null) {
            return 0;
        }

        return Integer.parseInt(String.valueOf(value));
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value == null) {
            return 0L;
        }

        return Long.parseLong(String.valueOf(value));
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
    @GetMapping("/admin/performance/debug-redis-teams")
    public String debugRedisTeams() {
        StringBuilder response = new StringBuilder();
        long totalTeams = 0;
        long expectedMatches = 0;

        for (String league : LEAGUES) {
            long teams = findTeamsByLeague(league).size();
            long matches = teams * (teams - 1);

            totalTeams += teams;
            expectedMatches += matches;

            response.append(league)
                    .append(" | teams=")
                    .append(teams)
                    .append(" | matches=")
                    .append(matches)
                    .append("\n");
        }

        response.append("\nTOTAL_TEAMS=").append(totalTeams);
        response.append("\nEXPECTED_MATCHES=").append(expectedMatches);

        return response.toString();
    }
    @GetMapping("/admin/performance/debug-redis-folders")
    public String debugRedisFolders() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/static/Images/leagues/*/*.png");

        Map<String, Long> folders = new LinkedHashMap<>();

        for (Resource resource : resources) {
            String folder = extractLeagueFolder(resource);
            if (folder == null) {
                folder = "NULL_FOLDER";
            }

            folders.put(folder, folders.getOrDefault(folder, 0L) + 1);
        }

        StringBuilder response = new StringBuilder();

        folders.forEach((folder, count) ->
                response.append(folder)
                        .append(" | files=")
                        .append(count)
                        .append("\n")
        );

        return response.toString();
    }

    @GetMapping("/admin/performance/calculate-league-tables-redis")
    public String calculateLeagueTablesRedisPerformance() {
        return buildSummary(runCalculateAllLeagueTables());
    }

    @GetMapping("/admin/performance/select-league-matches-redis")
    public String selectLeagueMatchesRedisPerformance() {
        return buildSummary(runSelectAllLeagueMatches());
    }

    @GetMapping("/admin/performance/select-match-events-redis")
    public String selectMatchEventsRedisPerformance() {
        return buildSummary(runSelectSampleMatchEvents());
    }

    @GetMapping("/admin/performance/generate-europe-redis")
    public String generateEuropeRedisPerformance() {
        return buildSummary(runGenerateAllEuropeanCompetitions());
    }

    @GetMapping("/admin/performance/calculate-europe-tables-redis")
    public String calculateEuropeTablesRedisPerformance() {
        return buildSummary(runCalculateAllEuropeanTables());
    }

    @GetMapping("/admin/performance/build-europe-rounds-redis")
    public String buildEuropeRoundsRedisPerformance() {
        return buildSummary(runBuildAllEuropeanRoundViews());
    }

    @GetMapping("/admin/performance/query-advanced-redis")
    public String queryAdvancedRedisPerformance() {
        List<PerformanceResult> results = List.of(
                runQueryPaginationMatches(),
                runQueryJoinMatchesEventsPlayers(),
                runQuerySortFilterPlayersByValue()
        );

        StringBuilder response = new StringBuilder();
        response.append("REDIS ADVANCED QUERY PERFORMANCE TESTS COMPLETED\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
    }
}
