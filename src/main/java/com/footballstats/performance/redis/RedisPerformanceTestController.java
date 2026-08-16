package com.footballstats.performance.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.DeserializationFeature;


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
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RestController
@RequiredArgsConstructor
@Profile("redis")
public class RedisPerformanceTestController {

    private static final String SEASON = "2025/2026";
    private static final int TRIALS = 1;

    private static final String K_COMPETITIONS = "redis:competitions:";
    private static final String K_REAL_TEAMS = "redis:realTeam:";
    private static final String K_REFEREES = "redis:referee:";
    private static final String K_GENERATED_PLAYERS = "redis:generatedPlayer:";
    private static final String K_MATCHES = "redis:match:";
    private static final String K_MATCH_EVENTS = "redis:matchEvent:";
    private static final String K_EUROPEAN_MATCHES = "redis:europeanMatch:";
    private static final String K_EUROPEAN_MATCH_EVENTS = "redis:europeanMatchEvent:";
    private static final String K_EUROPEAN_KNOCKOUT = "redis:europeanKnockoutMatch:";
    private static final String K_PARTICIPANTS = "redis:europeanCompetitionParticipant:";
    private static final String K_SEQUENCE = "redis:sequence:";
    private static final String K_INDEX = "redis:index:";

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

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final PlayerGenerator playerGenerator = new PlayerGenerator();
    private final LeagueTableService leagueTableService = new LeagueTableService();
    private final Random random = new Random();

    private volatile MatchRepository appMatchRepository;
    private volatile MatchEventRepository appMatchEventRepository;
    private volatile GeneratedPlayerRepository appGeneratedPlayerRepository;
    private volatile EuropeanCompetitionParticipantRepository appParticipantRepository;
    private volatile EuropeanMatchRepository appEuropeanMatchRepository;
    private volatile EuropeanMatchEventRepository appEuropeanMatchEventRepository;
    private volatile EuropeanKnockoutMatchRepository appEuropeanKnockoutRepository;
    private volatile RefereeRepository appRefereeRepository;
    private volatile MatchGeneratorService appMatchGeneratorService;
    private volatile EuropeanCompetitionService appEuropeanCompetitionService;

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

    @GetMapping("/admin/performance/app-flow-clean-redis")
    public String runCleanRedisApplicationFlowPerformanceTests() {
        resetRedisPerformanceData();
        setupRedisPerformanceData();
        return runRedisApplicationFlowPerformanceTests();
    }

    @GetMapping("/admin/performance/reset-redis")
    public String resetRedisPerformanceData() {
        deleteByPrefix(K_GENERATED_PLAYERS);
        deleteByPrefix(K_MATCHES);
        deleteByPrefix(K_MATCH_EVENTS);
        deleteByPrefix(K_EUROPEAN_MATCHES);
        deleteByPrefix(K_EUROPEAN_MATCH_EVENTS);
        deleteByPrefix(K_EUROPEAN_KNOCKOUT);
        deleteByPrefix(K_PARTICIPANTS);
        deleteByPrefix(K_SEQUENCE);
        deleteByPrefix(K_INDEX);

        return """
                REDIS PERFORMANCE DATA RESET COMPLETED

                Deleted generated data only:
                - generated players
                - league matches
                - league match events
                - european participants
                - european matches
                - european match events
                - european knockout matches

                Kept setup data:
                - competitions
                - real teams
                - referees
                """;
    }

    @GetMapping("/admin/performance/counts-redis")
    public String countsRedisPerformanceData() {
        return """
                REDIS PERFORMANCE DATA COUNTS

                competitions=%d
                realTeams=%d
                referees=%d
                generatedPlayers=%d
                leagueMatches=%d
                leagueMatchEvents=%d
                europeanMatches=%d
                europeanMatchEvents=%d
                europeanKnockoutMatches=%d
                totalKeys=%d
                """
                .formatted(
                        countByPrefix(K_COMPETITIONS),
                        countByPrefix(K_REAL_TEAMS),
                        countByPrefix(K_REFEREES),
                        countByPrefix(K_GENERATED_PLAYERS),
                        countByPrefix(K_MATCHES),
                        countByPrefix(K_MATCH_EVENTS),
                        countByPrefix(K_EUROPEAN_MATCHES),
                        countByPrefix(K_EUROPEAN_MATCH_EVENTS),
                        countByPrefix(K_EUROPEAN_KNOCKOUT),
                        countByPrefix("redis:")
                );
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
            deleteByPrefix(K_SEQUENCE + "GeneratedPlayer");
            deleteByPrefix(K_INDEX + "players:");

            long insertedPlayers = 0;
            List<RealTeam> teams =
                    readAllRedis(K_REAL_TEAMS, RealTeam.class);

            for (RealTeam team : teams) {
                List<GeneratedPlayer> squad =
                        playerGenerator.generateSquadForTeam(team);

                generatedPlayerRepositoryAdapter().saveAll(squad);

                insertedPlayers += squad.size();
            }

            return insertedPlayers;
        });
    }

    private PerformanceResult runGenerateAllLeagueSeasons() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_ALL_LEAGUE_SEASONS", () -> {
            synchronized (this) {
                long totalMatches = 0;

                for (String league : LEAGUES) {
                    matchGeneratorServiceAdapter().generateLeagueSeason(league);

                    totalMatches += matchRepositoryAdapter()
                            .findByLeagueOrderByRoundNumberAscMatchDateAsc(league)
                            .size();
                }

                return totalMatches;
            }
        });
    }

    private PerformanceResult runCalculateAllLeagueTables() {
        return runMeasuredTest("REDIS_APP_FLOW_CALCULATE_ALL_LEAGUE_TABLES", () -> {
            long totalRows = 0;
            for (String league : LEAGUES) {
                List<Match> matches = matchRepositoryAdapter()
                        .findByLeagueOrderByRoundNumberAscMatchDateAsc(league);
                List<LeagueTableRow> table = leagueTableService.calculateTable(matches, league);
                totalRows += table.size();
            }
            return totalRows;
        });
    }

    private PerformanceResult runSelectAllLeagueMatches() {
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_ALL_LEAGUE_MATCHES", () -> {
            long total = 0;
            for (String league : LEAGUES) {
                total += matchRepositoryAdapter()
                        .findByLeagueOrderByRoundNumberAscMatchDateAsc(league)
                        .size();
            }
            return total;
        });
    }

    private PerformanceResult runSelectSampleMatchEvents() {
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_SAMPLE_MATCH_EVENTS", () -> {
            List<Match> england = matchRepositoryAdapter()
                    .findByLeagueOrderByRoundNumberAscMatchDateAsc("england");
            if (england.isEmpty()) return 0;
            return matchEventRepositoryAdapter()
                    .findByMatchIdOrderByEventMinuteAsc(england.get(0).getId())
                    .size();
        });
    }

    private PerformanceResult runSelectAllGeneratedPlayers() {
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_ALL_GENERATED_PLAYERS", () ->
                readAllRedis(K_GENERATED_PLAYERS, GeneratedPlayer.class).size()
        );
    }

    private PerformanceResult runGenerateAllEuropeanCompetitions() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_ALL_EUROPEAN_COMPETITIONS", () -> {
            synchronized (this) {
                long total = 0;

                for (String competition : EUROPEAN_COMPETITIONS) {
                    europeanCompetitionServiceAdapter()
                            .generateOrRegenerateEuropeanCompetition(SEASON, competition);

                    total += europeanMatchRepositoryAdapter()
                            .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(SEASON, competition)
                            .size();
                }

                return total;
            }
        });
    }

    private PerformanceResult runCalculateAllEuropeanTables() {
        return runMeasuredTest("REDIS_APP_FLOW_CALCULATE_ALL_EUROPEAN_TABLES", () -> {
            long total = 0;
            for (String competition : EUROPEAN_COMPETITIONS) {
                List<EuropeanLeagueTableRow> table = europeanCompetitionServiceAdapter()
                        .calculateLeaguePhaseTable(SEASON, competition);
                total += table.size();
            }
            return total;
        });
    }

    private PerformanceResult runBuildAllEuropeanRoundViews() {
        return runMeasuredTest("REDIS_APP_FLOW_BUILD_ALL_EUROPEAN_ROUND_VIEWS", () -> {
            long total = 0;
            for (String competition : EUROPEAN_COMPETITIONS) {
                List<EuropeanLeaguePhaseRoundView> rounds = europeanCompetitionServiceAdapter()
                        .buildLeaguePhaseRoundViews(SEASON, competition);
                total += rounds.size();
            }
            return total;
        });
    }

    private PerformanceResult runGenerateAllEuropeanKnockouts() {
        return runMeasuredTest("REDIS_APP_FLOW_GENERATE_ALL_EUROPEAN_KNOCKOUTS", () -> {
            long total = 0;
            for (String competition : EUROPEAN_COMPETITIONS) {
                if (!europeanCompetitionServiceAdapter().isKnockoutGenerated(SEASON, competition)) {
                    europeanCompetitionServiceAdapter().generateKnockoutStage(SEASON, competition);
                }
                total += europeanCompetitionServiceAdapter()
                        .getKnockoutMatches(SEASON, competition)
                        .size();
            }
            return total;
        });
    }

    private PerformanceResult runSelectSampleEuropeanMatchEvents() {
        return runMeasuredTest("REDIS_APP_FLOW_SELECT_SAMPLE_EUROPEAN_MATCH_EVENTS", () -> {
            List<EuropeanMatch> matches = europeanMatchRepositoryAdapter()
                    .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(SEASON, "champions-league");
            if (matches.isEmpty()) return 0;
            return europeanMatchEventRepositoryAdapter()
                    .findByMatchIdOrderByEventMinuteAsc(matches.get(0).getId())
                    .size();
        });
    }

    private PerformanceResult runQueryPaginationMatches() {
        return runMeasuredTest("REDIS_QUERY_PAGINATION_MATCHES", () -> {
            List<Match> all = readAllRedis(K_MATCHES, Match.class).stream()
                    .sorted(Comparator.comparingInt(Match::getRoundNumber)
                            .thenComparing(Match::getMatchDate))
                    .toList();
            int from = Math.min(1000, all.size());
            int to = Math.min(from + 100, all.size());
            return all.subList(from, to).size();
        });
    }

    private PerformanceResult runQueryJoinMatchesEventsPlayers() {
        return runMeasuredTest("REDIS_QUERY_COMPLEX_JOIN_MATCHES_EVENTS_PLAYERS", () -> {
            List<MatchEvent> events = readAllRedis(K_MATCH_EVENTS, MatchEvent.class);
            long rows = 0;
            int checked = 0;
            for (MatchEvent event : events) {
                if (checked++ >= 1000) break;
                Match match = readRedis(K_MATCHES + event.getMatchId(), Match.class);
                GeneratedPlayer player = event.getPlayerId() == null
                        ? null
                        : readRedis(K_GENERATED_PLAYERS + event.getPlayerId(), GeneratedPlayer.class);
                if (match != null && player != null) rows++;
            }
            return rows;
        });
    }

    private PerformanceResult runQuerySortFilterPlayersByValue() {
        return runMeasuredTest("REDIS_QUERY_SORT_FILTER_PLAYERS_BY_VALUE", () ->
                readAllRedis(K_GENERATED_PLAYERS, GeneratedPlayer.class).stream()
                        .filter(p -> "england".equals(p.getLeagueSlug()))
                        .filter(p -> p.getMarketValue() != null && p.getMarketValue().signum() > 0)
                        .sorted(Comparator.comparing(GeneratedPlayer::getMarketValue).reversed())
                        .limit(100)
                        .count()
        );
    }

    // -------------------------------------------------------------------------
    // Adaptery repozytoriów do Redis. Dzięki nim testy używają dokładnie
    // MatchGeneratorService i EuropeanCompetitionService z normalnej aplikacji.
    // -------------------------------------------------------------------------

    private MatchGeneratorService matchGeneratorServiceAdapter() {
        if (appMatchGeneratorService == null) {
            synchronized (this) {
                if (appMatchGeneratorService == null) {
                    appMatchGeneratorService = new MatchGeneratorService(
                            matchRepositoryAdapter(),
                            matchEventRepositoryAdapter(),
                            generatedPlayerRepositoryAdapter()
                    );
                }
            }
        }
        return appMatchGeneratorService;
    }

    private EuropeanCompetitionService europeanCompetitionServiceAdapter() {
        if (appEuropeanCompetitionService == null) {
            synchronized (this) {
                if (appEuropeanCompetitionService == null) {
                    appEuropeanCompetitionService = new EuropeanCompetitionService(
                            participantRepositoryAdapter(),
                            europeanMatchRepositoryAdapter(),
                            europeanKnockoutRepositoryAdapter(),
                            matchRepositoryAdapter(),
                            leagueTableService,
                            generatedPlayerRepositoryAdapter(),
                            europeanMatchEventRepositoryAdapter(),
                            matchGeneratorServiceAdapter(),
                            refereeRepositoryAdapter()
                    );
                }
            }
        }
        return appEuropeanCompetitionService;
    }

    private MatchRepository matchRepositoryAdapter() {
        if (appMatchRepository == null) {
            appMatchRepository = proxyRepository(MatchRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findByLeagueOrderByRoundNumberAscMatchDateAsc" -> indexedRedis(
                        indexLeagueMatches((String) args[0]), Match.class).stream()
                        .sorted(Comparator.comparingInt(Match::getRoundNumber).thenComparing(Match::getMatchDate))
                        .toList();
                case "existsByLeague" -> !members(indexLeagueMatches((String) args[0])).isEmpty();
                case "deleteByLeague" -> { deleteIndexed(indexLeagueMatches((String) args[0])); yield null; }
                case "saveAll" -> saveAllRedis((Iterable<?>) args[0]);
                default -> unsupported(method);
            });
        }
        return appMatchRepository;
    }

    private MatchEventRepository matchEventRepositoryAdapter() {
        if (appMatchEventRepository == null) {
            appMatchEventRepository = proxyRepository(MatchEventRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findByMatchIdOrderByEventMinuteAsc" -> indexedRedis(
                        indexMatchEvents((Long) args[0]), MatchEvent.class).stream()
                        .sorted(Comparator.comparing(MatchEvent::getEventMinute))
                        .toList();
                case "deleteByMatchIdIn" -> {
                    for (Object id : (Collection<?>) args[0]) deleteIndexed(indexMatchEvents(((Number) id).longValue()));
                    yield null;
                }
                case "save" -> saveRedis(args[0]);
                default -> unsupported(method);
            });
        }
        return appMatchEventRepository;
    }

    private GeneratedPlayerRepository generatedPlayerRepositoryAdapter() {
        if (appGeneratedPlayerRepository == null) {
            appGeneratedPlayerRepository = proxyRepository(GeneratedPlayerRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findByLeagueSlugAndClubSlug" -> indexedRedis(
                        indexPlayersLeagueClub((String) args[0], (String) args[1]), GeneratedPlayer.class);
                case "findByClubSlug" -> indexedRedis(indexPlayersClub((String) args[0]), GeneratedPlayer.class);
                case "existsByLeagueSlugAndClubSlug" -> !members(
                        indexPlayersLeagueClub((String) args[0], (String) args[1])).isEmpty();
                case "save" -> saveRedis(args[0]);
                case "saveAll" -> saveAllRedis((Iterable<?>) args[0]);
                default -> unsupported(method);
            });
        }
        return appGeneratedPlayerRepository;
    }

    private EuropeanCompetitionParticipantRepository participantRepositoryAdapter() {
        if (appParticipantRepository == null) {
            appParticipantRepository = proxyRepository(EuropeanCompetitionParticipantRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findBySeasonAndCompetitionSlugOrderByPotNumberAscUefaRankingPointsDesc" -> indexedRedis(
                        indexParticipants((String) args[0], (String) args[1]), EuropeanCompetitionParticipant.class).stream()
                        .sorted(Comparator.comparingInt(EuropeanCompetitionParticipant::getPotNumber)
                                .thenComparing(Comparator.comparingInt(EuropeanCompetitionParticipant::getUefaRankingPoints).reversed()))
                        .toList();
                case "existsBySeasonAndCompetitionSlug" -> !members(indexParticipants((String) args[0], (String) args[1])).isEmpty();
                case "deleteBySeason" -> { deleteParticipantSeason((String) args[0]); yield null; }
                case "saveAll" -> saveAllRedis((Iterable<?>) args[0]);
                default -> unsupported(method);
            });
        }
        return appParticipantRepository;
    }

    private EuropeanMatchRepository europeanMatchRepositoryAdapter() {
        if (appEuropeanMatchRepository == null) {
            appEuropeanMatchRepository = proxyRepository(EuropeanMatchRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "existsBySeasonAndCompetitionSlug" -> !members(indexEuropeanMatches((String) args[0], (String) args[1])).isEmpty();
                case "findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc" -> indexedRedis(
                        indexEuropeanMatches((String) args[0], (String) args[1]), EuropeanMatch.class).stream()
                        .sorted(Comparator.comparingInt(EuropeanMatch::getRoundNumber).thenComparing(EuropeanMatch::getMatchDate))
                        .toList();
                case "deleteBySeasonAndCompetitionSlug" -> { deleteIndexed(indexEuropeanMatches((String) args[0], (String) args[1])); yield null; }
                case "saveAll" -> saveAllRedis((Iterable<?>) args[0]);
                default -> unsupported(method);
            });
        }
        return appEuropeanMatchRepository;
    }

    private EuropeanMatchEventRepository europeanMatchEventRepositoryAdapter() {
        if (appEuropeanMatchEventRepository == null) {
            appEuropeanMatchEventRepository = proxyRepository(EuropeanMatchEventRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findByMatchIdOrderByEventMinuteAsc" -> indexedRedis(
                        indexEuropeanEventsMatch((Long) args[0]), EuropeanMatchEvent.class).stream()
                        .sorted(Comparator.comparing(EuropeanMatchEvent::getEventMinute))
                        .toList();
                case "findByKnockoutMatchIdAndKnockoutLegOrderByEventMinuteAsc" -> indexedRedis(
                        indexEuropeanEventsKnockoutLeg((Long) args[0], (String) args[1]), EuropeanMatchEvent.class).stream()
                        .sorted(Comparator.comparing(EuropeanMatchEvent::getEventMinute))
                        .toList();
                case "deleteByMatchIdIn" -> {
                    for (Object id : (Collection<?>) args[0]) deleteIndexed(indexEuropeanEventsMatch(((Number) id).longValue()));
                    yield null;
                }
                case "deleteByKnockoutMatchIdIn" -> {
                    for (Object id : (Collection<?>) args[0]) deleteEuropeanKnockoutEvents(((Number) id).longValue());
                    yield null;
                }
                case "saveAll" -> saveAllRedis((Iterable<?>) args[0]);
                default -> unsupported(method);
            });
        }
        return appEuropeanMatchEventRepository;
    }

    private EuropeanKnockoutMatchRepository europeanKnockoutRepositoryAdapter() {
        if (appEuropeanKnockoutRepository == null) {
            appEuropeanKnockoutRepository = proxyRepository(EuropeanKnockoutMatchRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "existsBySeasonAndCompetitionSlug" -> !members(indexKnockout((String) args[0], (String) args[1])).isEmpty();
                case "findBySeasonAndCompetitionSlugOrderByRoundOrderAscBracketOrderAsc" -> indexedRedis(
                        indexKnockout((String) args[0], (String) args[1]), EuropeanKnockoutMatch.class).stream()
                        .sorted(Comparator.comparingInt(EuropeanKnockoutMatch::getRoundOrder)
                                .thenComparingInt(EuropeanKnockoutMatch::getBracketOrder))
                        .toList();
                case "deleteBySeasonAndCompetitionSlug" -> { deleteIndexed(indexKnockout((String) args[0], (String) args[1])); yield null; }
                case "saveAll" -> saveAllRedis((Iterable<?>) args[0]);
                default -> unsupported(method);
            });
        }
        return appEuropeanKnockoutRepository;
    }

    private RefereeRepository refereeRepositoryAdapter() {
        if (appRefereeRepository == null) {
            appRefereeRepository = proxyRepository(RefereeRepository.class, (proxy, method, args) -> switch (method.getName()) {
                case "findAll" -> readAllRedis(K_REFEREES, Referee.class);
                default -> unsupported(method);
            });
        }
        return appRefereeRepository;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxyRepository(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private Object unsupported(Method method) {
        throw new UnsupportedOperationException("Redis repository adapter does not implement: " + method.getName());
    }

    private List<Object> saveAllRedis(Iterable<?> values) {
        List<Object> saved = new ArrayList<>();

        for (Object value : values) {
            saved.add(value);
        }

        if (saved.isEmpty()) {
            return saved;
        }

        long missingIds = saved.stream()
                .filter(value -> getId(value) == null)
                .count();

        if (missingIds > 0) {
            String sequenceKey =
                    K_SEQUENCE + saved.get(0).getClass().getSimpleName();

            Long lastId = redisTemplate.opsForValue()
                    .increment(sequenceKey, missingIds);

            long nextId = (lastId == null ? missingIds : lastId)
                    - missingIds + 1;

            for (Object value : saved) {
                if (getId(value) == null) {
                    setId(value, nextId++);
                }
            }
        }

        Map<String, String> data = new LinkedHashMap<>();

        try {
            objectMapper.registerModule(new JavaTimeModule());

            for (Object value : saved) {
                String key = entityPrefix(value) + getId(value);
                data.put(key, objectMapper.writeValueAsString(value));
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Cannot serialize Redis entities",
                    e
            );
        }

        redisTemplate.opsForValue().multiSet(data);

        for (Object value : saved) {
            String key = entityPrefix(value) + getId(value);
            indexEntity(key, value);
        }

        return saved;
    }

    private Object saveRedis(Object value) {
        Long id = getId(value);
        if (id == null) {
            id = redisTemplate.opsForValue().increment(K_SEQUENCE + value.getClass().getSimpleName());
            setId(value, id == null ? 1L : id);
        }

        String key = entityPrefix(value) + getId(value);
        writeJson(key, value);
        indexEntity(key, value);
        return value;
    }

    private String entityPrefix(Object value) {
        if (value instanceof GeneratedPlayer) return K_GENERATED_PLAYERS;
        if (value instanceof Match) return K_MATCHES;
        if (value instanceof MatchEvent) return K_MATCH_EVENTS;
        if (value instanceof EuropeanCompetitionParticipant) return K_PARTICIPANTS;
        if (value instanceof EuropeanMatch) return K_EUROPEAN_MATCHES;
        if (value instanceof EuropeanMatchEvent) return K_EUROPEAN_MATCH_EVENTS;
        if (value instanceof EuropeanKnockoutMatch) return K_EUROPEAN_KNOCKOUT;
        throw new IllegalArgumentException("Unsupported Redis entity: " + value.getClass().getName());
    }

    private void indexEntity(String key, Object value) {
        if (value instanceof GeneratedPlayer p) {
            addIndex(indexPlayersLeagueClub(p.getLeagueSlug(), p.getClubSlug()), key);
            addIndex(indexPlayersClub(p.getClubSlug()), key);
        } else if (value instanceof Match m) {
            addIndex(indexLeagueMatches(m.getLeague()), key);
        } else if (value instanceof MatchEvent e) {
            addIndex(indexMatchEvents(e.getMatchId()), key);
        } else if (value instanceof EuropeanCompetitionParticipant p) {
            addIndex(indexParticipants(p.getSeason(), p.getCompetitionSlug()), key);
        } else if (value instanceof EuropeanMatch m) {
            addIndex(indexEuropeanMatches(m.getSeason(), m.getCompetitionSlug()), key);
        } else if (value instanceof EuropeanMatchEvent e) {
            if (e.getMatchId() != null) addIndex(indexEuropeanEventsMatch(e.getMatchId()), key);
            if (e.getKnockoutMatchId() != null && e.getKnockoutLeg() != null) {
                addIndex(indexEuropeanEventsKnockoutLeg(e.getKnockoutMatchId(), e.getKnockoutLeg()), key);
            }
        } else if (value instanceof EuropeanKnockoutMatch m) {
            addIndex(indexKnockout(m.getSeason(), m.getCompetitionSlug()), key);
        }
    }

    private void addIndex(String index, String key) {
        redisTemplate.opsForSet().add(index, key);
    }

    private Set<String> members(String index) {
        Set<String> result = redisTemplate.opsForSet().members(index);
        return result == null ? Set.of() : result;
    }

    private <T> List<T> indexedRedis(String index, Class<T> type) {
        Set<String> keySet = members(index);

        if (keySet.isEmpty()) {
            return List.of();
        }

        List<String> keys = new ArrayList<>(keySet);
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<T> result = new ArrayList<>();

        for (String json : values) {
            if (json == null) {
                continue;
            }

            try {
                JsonNode node = objectMapper.readTree(json);

                if (node.isObject() && node.has("_id") && !node.has("id")) {
                    ObjectNode objectNode = (ObjectNode) node;
                    objectNode.set("id", objectNode.get("_id"));
                    objectNode.remove("_id");
                }

                result.add(objectMapper.treeToValue(node, type));

            } catch (Exception e) {
                throw new IllegalStateException(
                        "Cannot deserialize Redis indexed values",
                        e
                );
            }
        }

        return result;
    }

    private void deleteIndexed(String index) {
        Set<String> keys = new HashSet<>(members(index));
        if (!keys.isEmpty()) redisTemplate.delete(keys);
        redisTemplate.delete(index);
    }

    private void deleteParticipantSeason(String season) {
        for (String competition : EUROPEAN_COMPETITIONS) {
            deleteIndexed(indexParticipants(season, competition));
        }
    }

    private void deleteEuropeanKnockoutEvents(long knockoutId) {
        deleteIndexed(indexEuropeanEventsKnockoutLeg(knockoutId, "FIRST_LEG"));
        deleteIndexed(indexEuropeanEventsKnockoutLeg(knockoutId, "SECOND_LEG"));
        deleteIndexed(indexEuropeanEventsKnockoutLeg(knockoutId, "FINAL"));
    }

    private String indexPlayersLeagueClub(String league, String club) {
        return K_INDEX + "players:leagueClub:" + league + ":" + club;
    }
    private String indexPlayersClub(String club) { return K_INDEX + "players:club:" + club; }
    private String indexLeagueMatches(String league) { return K_INDEX + "matches:league:" + league; }
    private String indexMatchEvents(Long matchId) { return K_INDEX + "matchEvents:match:" + matchId; }
    private String indexParticipants(String season, String competition) { return K_INDEX + "participants:" + season + ":" + competition; }
    private String indexEuropeanMatches(String season, String competition) { return K_INDEX + "europeanMatches:" + season + ":" + competition; }
    private String indexEuropeanEventsMatch(Long matchId) { return K_INDEX + "europeanEvents:match:" + matchId; }
    private String indexEuropeanEventsKnockoutLeg(Long id, String leg) { return K_INDEX + "europeanEvents:knockout:" + id + ":" + leg; }
    private String indexKnockout(String season, String competition) { return K_INDEX + "knockout:" + season + ":" + competition; }

    private <T> List<T> readAllRedis(String prefix, Class<T> type) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) return List.of();
        List<T> result = new ArrayList<>();
        for (String key : keys) {
            T value = readRedis(key, type);
            if (value != null) result.add(value);
        }
        return result;
    }

    private <T> T readRedis(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            return null;
        }

        try {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );

            JsonNode node = objectMapper.readTree(json);

            if (node.isObject() && node.has("_id") && !node.has("id")) {
                ObjectNode objectNode = (ObjectNode) node;
                objectNode.set("id", objectNode.get("_id"));
                objectNode.remove("_id");
            }

            return objectMapper.treeToValue(node, type);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot read Redis JSON: " + key,
                    e
            );
        }
    }

    private void writeJson(String key, Object value) {
        try {
            objectMapper.registerModule(new JavaTimeModule());


            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(value)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Cannot write Redis JSON: " + key,
                    e
            );
        }
    }

    private Long getId(Object value) {
        try {
            Field field = findField(value.getClass(), "id");
            field.setAccessible(true);
            return (Long) field.get(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read Redis id", e);
        }
    }

    private void setId(Object value, Long id) {
        try {
            Field field = findField(value.getClass(), "id");
            field.setAccessible(true);
            field.set(value, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot assign Redis id", e);
        }
    }

    private Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try { return current.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) { current = current.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
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
                competition(26, "Nemzeti Bajnoksag", "hungary", "DOMESTIC_LEAGUE"),
                competition(27, "Moldova Superliga", "moldova", "DOMESTIC_LEAGUE"),
                competition(28, "Ireland Premier Division", "republic-of-ireland", "DOMESTIC_LEAGUE"),
                competition(29, "Champions League", "champions-league", "EUROPEAN_CUP"),
                competition(30, "Europa League", "europa-league", "EUROPEAN_CUP"),
                competition(31, "Conference League", "conference-league", "EUROPEAN_CUP")
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

    private long countByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys == null ? 0L : keys.size();
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
