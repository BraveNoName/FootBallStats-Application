package com.footballstats.controller;

import com.footballstats.dto.EuropeanLeaguePhaseRoundView;
import com.footballstats.dto.EuropeanLeagueTableRow;
import com.footballstats.dto.LeagueTableRow;
import com.footballstats.model.*;
import com.footballstats.repository.*;
import com.footballstats.service.LeagueTableService;
import com.footballstats.service.PlayerGenerator;
import com.footballstats.service.europe.EuropeanCompetitionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

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
            "romania", "bulgaria", "ukraine", "russia", "israel",
            "hungary", "moldova", "republic-of-ireland"
    };

    private static final String[] EUROPEAN_COMPETITIONS = {
            "champions-league",
            "europa-league",
            "conference-league"
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
    private final Random random = new Random();

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Pełny test aplikacyjny od czystego stanu danych pomiarowych.
     * Ten endpoint jest najbezpieczniejszy do porównań między bazami.
     */
    @GetMapping("/admin/performance/app-flow-clean")
    @Transactional
    public String runCleanApplicationFlowPerformanceTests() {
        resetPerformanceDataInternal();
        ensureReferees();
        ensureRealTeams();
        return runApplicationFlowPerformanceTestsInternal("FULL CLEAN APP FLOW PERFORMANCE TESTS COMPLETED");
    }

    /**
     * Pełny test aplikacyjny bez automatycznego resetu.
     * Zostawiony, żeby nie psuć dotychczasowych skryptów.
     */
    @GetMapping("/admin/performance/app-flow")
    @Transactional
    public String runApplicationFlowPerformanceTests() {
        ensureReferees();
        ensureRealTeams();
        return runApplicationFlowPerformanceTestsInternal("FULL APP FLOW PERFORMANCE TESTS COMPLETED");
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

                Recommended clean endpoint:
                /admin/performance/app-flow-clean

                Manual endpoints:
                /admin/performance/reset
                /admin/performance/counts
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
                /admin/performance/query-pagination
                /admin/performance/query-join
                /admin/performance/query-sort-filter
                /admin/performance/query-advanced
                """;
    }

    /**
     * Czyści tylko dane generowane podczas testów.
     * Nie usuwa słowników bazowych: RealTeam i Referee.
     */
    @GetMapping("/admin/performance/reset")
    @Transactional
    public String resetPerformanceData() {
        resetPerformanceDataInternal();
        return """
                PERFORMANCE DATA RESET COMPLETED

                Deleted generated data:
                - domestic match events
                - domestic matches
                - generated players
                - European match events
                - European knockout matches
                - European league phase matches
                - European competition participants

                Kept setup data:
                - real teams
                - referees
                """;
    }

    @GetMapping("/admin/performance/counts")
    public String performanceDataCounts() {
        return """
                PERFORMANCE DATA COUNTS

                Real teams: %d
                Referees: %d
                Generated players: %d
                Domestic matches: %d
                Domestic match events: %d
                European matches: %d
                European knockout matches: %d
                European match events: %d
                European participants: %d
                """.formatted(
                realTeamRepository.count(),
                refereeRepository.count(),
                generatedPlayerRepository.count(),
                matchRepository.count(),
                matchEventRepository.count(),
                europeanMatchRepository.count(),
                europeanKnockoutMatchRepository.count(),
                europeanMatchEventRepository.count(),
                countEuropeanParticipants()
        );
    }

    @GetMapping("/admin/performance/generate-players")
    public String generatePlayersPerformance() {
        ensureRealTeams();
        return buildSummary(runGeneratePlayersForAllTeams());
    }

    @GetMapping("/admin/performance/generate-leagues")
    public String generateLeaguesPerformance() {
        ensureReferees();
        ensureRealTeams();
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
    @Transactional
    public String generateEuropePerformance() {
        ensureReferees();
        ensureRealTeams();
        ensureGeneratedPlayersExist();
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
    @Transactional
    public String generateEuropeKnockoutPerformance() {
        return buildSummary(runGenerateAllEuropeanKnockouts());
    }

    @GetMapping("/admin/performance/select-europe-events")
    public String selectEuropeEventsPerformance() {
        return buildSummary(runSelectSampleEuropeanMatchEvents());
    }

    @GetMapping("/admin/performance/query-pagination")
    public String queryPaginationPerformance() {
        return buildSummary(runQueryPaginationMatches());
    }

    @GetMapping("/admin/performance/query-join")
    public String queryJoinPerformance() {
        return buildSummary(runQueryJoinMatchesEventsPlayers());
    }

    @GetMapping("/admin/performance/query-sort-filter")
    public String querySortFilterPerformance() {
        return buildSummary(runQuerySortFilterPlayersByValue());
    }

    @GetMapping("/admin/performance/query-advanced")
    public String queryAdvancedPerformance() {
        List<PerformanceResult> results = List.of(
                runQueryPaginationMatches(),
                runQueryJoinMatchesEventsPlayers(),
                runQuerySortFilterPlayersByValue()
        );

        StringBuilder response = new StringBuilder();
        response.append("ADVANCED QUERY PERFORMANCE TESTS COMPLETED\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
    }

    private String runApplicationFlowPerformanceTestsInternal(String title) {
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
        response.append(title).append("\n\n");

        for (PerformanceResult result : results) {
            response.append(buildSummary(result)).append("\n\n");
        }

        return response.toString();
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
            deleteDomesticMatchData();

            long totalMatches = 0;

            for (String league : LEAGUES) {
                List<RealTeam> teams = realTeamRepository.findAll().stream()
                        .filter(team -> league.equals(team.getLeagueSlug()))
                        .sorted(Comparator.comparing(RealTeam::getApiTeamName))
                        .toList();

                int round = 1;

                for (int i = 0; i < teams.size(); i++) {
                    for (int j = i + 1; j < teams.size(); j++) {
                        RealTeam firstTeam = teams.get(i);
                        RealTeam secondTeam = teams.get(j);

                        Match firstMatch = createLeagueMatch(
                                league,
                                firstTeam,
                                secondTeam,
                                round++
                        );
                        Match savedFirstMatch = matchRepository.save(firstMatch);
                        insertLeagueMatchEvents(savedFirstMatch);
                        totalMatches++;

                        Match secondMatch = createLeagueMatch(
                                league,
                                secondTeam,
                                firstTeam,
                                round++
                        );
                        Match savedSecondMatch = matchRepository.save(secondMatch);
                        insertLeagueMatchEvents(savedSecondMatch);
                        totalMatches++;
                    }
                }
            }

            return totalMatches;
        });
    }

    private Match createLeagueMatch(
            String league,
            RealTeam home,
            RealTeam away,
            int roundNumber
    ) {
        Match match = new Match();
        match.setLeague(league);
        match.setRoundNumber(roundNumber);
        match.setMatchDate(LocalDateTime.now().plusDays(roundNumber));
        match.setHomeTeam(home.getApiTeamName());
        match.setAwayTeam(away.getApiTeamName());
        match.setHomeTeamLogo(home.getApiLogo());
        match.setAwayTeamLogo(away.getApiLogo());
        match.setHomeClubSlug(home.getClubSlug());
        match.setAwayClubSlug(away.getClubSlug());
        match.setHomeLeagueSlug(home.getLeagueSlug());
        match.setAwayLeagueSlug(away.getLeagueSlug());
        match.setHomeGoals(random.nextInt(5));
        match.setAwayGoals(random.nextInt(5));
        match.setHalfTimeHomeGoals(0);
        match.setHalfTimeAwayGoals(0);
        match.setStatus("FINISHED");
        return match;
    }

    private void insertLeagueMatchEvents(Match match) {
        if (generatedPlayerRepository.count() == 0) {
            return;
        }

        List<GeneratedPlayer> players = new ArrayList<>();
        players.addAll(generatedPlayerRepository.findByClubSlug(match.getHomeClubSlug()));
        players.addAll(generatedPlayerRepository.findByClubSlug(match.getAwayClubSlug()));

        if (players.isEmpty()) {
            return;
        }

        int goals = valueOrZero(match.getHomeGoals()) + valueOrZero(match.getAwayGoals());

        for (int i = 0; i < goals; i++) {
            GeneratedPlayer player = players.get(random.nextInt(players.size()));
            matchEventRepository.save(createLeagueEvent(match, player, "GOAL"));
        }

        int cards = random.nextInt(4);
        for (int i = 0; i < cards; i++) {
            GeneratedPlayer player = players.get(random.nextInt(players.size()));
            matchEventRepository.save(createLeagueEvent(match, player, "YELLOW_CARD"));
        }
    }

    private MatchEvent createLeagueEvent(
            Match match,
            GeneratedPlayer player,
            String eventType
    ) {
        MatchEvent event = new MatchEvent();
        event.setMatchId(match.getId());
        event.setPlayerId(player.getId());
        event.setPlayerName(player.getFirstName() + " " + player.getLastName());
        event.setEventType(eventType);
        event.setEventMinute(random.nextInt(1, 91));
        event.setDescription(eventType);
        event.setTeamSide(
                player.getClubSlug().equals(match.getHomeClubSlug()) ? "HOME" : "AWAY"
        );
        return event;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
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
            deleteAllEuropeanCompetitionData();

            for (String competition : EUROPEAN_COMPETITIONS) {
                europeanCompetitionService.generateOrRegenerateEuropeanCompetition(SEASON, competition);
                repairDuplicateEuropeanParticipantsAndMatches(SEASON, competition);
            }

            return europeanMatchRepository.count();
        });
    }


    /**
     * Naprawa wykonywana wyłącznie na potrzeby testów wydajnościowych.
     * Usuwa sytuację, w której ten sam klub trafia do jednej konkurencji
     * jednocześnie jako obrońca tytułu i poprzez kwalifikację ligową.
     *
     * Nie modyfikuje EuropeanCompetitionService.
     */
    private void repairDuplicateEuropeanParticipantsAndMatches(
            String season,
            String competitionSlug
    ) {
        List<EuropeanCompetitionParticipant> participants = entityManager.createQuery(
                        "SELECT p FROM EuropeanCompetitionParticipant p " +
                                "WHERE p.season = :season " +
                                "AND p.competitionSlug = :competition " +
                                "ORDER BY p.id",
                        EuropeanCompetitionParticipant.class
                )
                .setParameter("season", season)
                .setParameter("competition", competitionSlug)
                .getResultList();

        Map<String, List<EuropeanCompetitionParticipant>> byClubSlug = new LinkedHashMap<>();

        for (EuropeanCompetitionParticipant participant : participants) {
            byClubSlug.computeIfAbsent(
                    participant.getClubSlug(),
                    ignored -> new ArrayList<>()
            ).add(participant);
        }

        Set<String> currentCompetitionSlugs = new HashSet<>();
        for (EuropeanCompetitionParticipant participant : participants) {
            currentCompetitionSlugs.add(participant.getClubSlug());
        }

        Set<String> allEuropeanSlugs = new HashSet<>(
                entityManager.createQuery(
                                "SELECT DISTINCT p.clubSlug " +
                                        "FROM EuropeanCompetitionParticipant p " +
                                        "WHERE p.season = :season",
                                String.class
                        )
                        .setParameter("season", season)
                        .getResultList()
        );

        Map<String, Long> teamsPerLeague = new HashMap<>();
        for (EuropeanCompetitionParticipant participant : participants) {
            teamsPerLeague.merge(participant.getLeagueSlug(), 1L, Long::sum);
        }

        List<RealTeam> replacementPool = realTeamRepository.findAll().stream()
                .sorted(Comparator.comparing(RealTeam::getApiTeamName))
                .toList();

        for (Map.Entry<String, List<EuropeanCompetitionParticipant>> entry : byClubSlug.entrySet()) {
            List<EuropeanCompetitionParticipant> duplicates = entry.getValue();

            if (duplicates.size() < 2) {
                continue;
            }

            // Zachowujemy rekord obrońcy tytułu, a podmieniamy zwykłego uczestnika.
            EuropeanCompetitionParticipant duplicateToReplace = duplicates.stream()
                    .filter(participant -> !participant.isTitleHolder())
                    .findFirst()
                    .orElse(duplicates.get(duplicates.size() - 1));

            RealTeam replacement = replacementPool.stream()
                    .filter(team -> !currentCompetitionSlugs.contains(team.getClubSlug()))
                    .filter(team -> !allEuropeanSlugs.contains(team.getClubSlug()))
                    .filter(team ->
                            teamsPerLeague.getOrDefault(team.getLeagueSlug(), 0L)
                                    < getEuropeanLeagueLimit(competitionSlug)
                    )
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Cannot find a unique replacement team for "
                                    + competitionSlug
                                    + ", duplicated club="
                                    + entry.getKey()
                    ));

            String duplicatedClubSlug = duplicateToReplace.getClubSlug();

            duplicateToReplace.setClubName(replacement.getApiTeamName());
            duplicateToReplace.setClubSlug(replacement.getClubSlug());
            duplicateToReplace.setLeagueSlug(replacement.getLeagueSlug());
            duplicateToReplace.setCountryCode(resolveCountryCode(replacement.getLeagueSlug()));
            duplicateToReplace.setTitleHolder(false);
            duplicateToReplace.setUefaRankingPoints(
                    Math.abs(replacement.getApiTeamId())
            );

            currentCompetitionSlugs.add(replacement.getClubSlug());
            allEuropeanSlugs.add(replacement.getClubSlug());
            teamsPerLeague.merge(replacement.getLeagueSlug(), 1L, Long::sum);

            replaceDuplicateTeamInEightLeaguePhaseMatches(
                    season,
                    competitionSlug,
                    duplicatedClubSlug,
                    replacement
            );

            System.out.println(
                    "PERFORMANCE_TEST | EUROPE_DUPLICATE_REPAIRED"
                            + " | competition=" + competitionSlug
                            + " | duplicatedClub=" + duplicatedClubSlug
                            + " | replacementClub=" + replacement.getClubSlug()
            );
        }
    }

    private void replaceDuplicateTeamInEightLeaguePhaseMatches(
            String season,
            String competitionSlug,
            String duplicatedClubSlug,
            RealTeam replacement
    ) {
        List<EuropeanMatch> matchesWithDuplicate = europeanMatchRepository
                .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(
                        season,
                        competitionSlug
                )
                .stream()
                .filter(match ->
                        duplicatedClubSlug.equals(match.getHomeClubSlug())
                                || duplicatedClubSlug.equals(match.getAwayClubSlug())
                )
                .toList();

        int matchesToReplace = Math.min(8, matchesWithDuplicate.size());

        for (int i = matchesWithDuplicate.size() - matchesToReplace;
             i < matchesWithDuplicate.size();
             i++) {

            EuropeanMatch match = matchesWithDuplicate.get(i);

            if (duplicatedClubSlug.equals(match.getHomeClubSlug())) {
                match.setHomeTeam(replacement.getApiTeamName());
                match.setHomeClubSlug(replacement.getClubSlug());
                match.setHomeLeagueSlug(replacement.getLeagueSlug());
            } else {
                match.setAwayTeam(replacement.getApiTeamName());
                match.setAwayClubSlug(replacement.getClubSlug());
                match.setAwayLeagueSlug(replacement.getLeagueSlug());
            }
        }

        europeanMatchRepository.saveAll(
                matchesWithDuplicate.subList(
                        matchesWithDuplicate.size() - matchesToReplace,
                        matchesWithDuplicate.size()
                )
        );
    }

    private int getEuropeanLeagueLimit(String competitionSlug) {
        return switch (competitionSlug) {
            case "champions-league" -> 5;
            case "europa-league" -> 4;
            case "conference-league" -> 3;
            default -> 3;
        };
    }

    private String resolveCountryCode(String leagueSlug) {
        return switch (leagueSlug) {
            case "england" -> "gb-eng";
            case "spain" -> "es";
            case "germany" -> "de";
            case "italy" -> "it";
            case "france" -> "fr";
            case "portugal" -> "pt";
            case "netherlands" -> "nl";
            case "belgium" -> "be";
            case "scotland" -> "gb-sct";
            case "turkiye" -> "tr";
            case "poland" -> "pl";
            case "austria" -> "at";
            case "croatia" -> "hr";
            case "czech-republic" -> "cz";
            case "denmark" -> "dk";
            case "greece" -> "gr";
            case "norway" -> "no";
            case "sweden" -> "se";
            case "switzerland" -> "ch";
            case "serbia" -> "rs";
            case "romania" -> "ro";
            case "bulgaria" -> "bg";
            case "ukraine" -> "ua";
            case "russia" -> "ru";
            case "israel" -> "il";
            case "hungary" -> "hu";
            case "moldova" -> "md";
            case "republic-of-ireland" -> "ie";
            default -> "";
        };
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

    private PerformanceResult runQueryPaginationMatches() {
        return runMeasuredTest("QUERY_PAGINATION_MATCHES", () -> {
            Page<Match> page = matchRepository.findAll(PageRequest.of(10, 100));
            return page.getContent().size();
        });
    }

    private PerformanceResult runQueryJoinMatchesEventsPlayers() {
        return runMeasuredTest("QUERY_COMPLEX_JOIN_MATCHES_EVENTS_PLAYERS", () -> {
            List<Object[]> rows = entityManager.createQuery(
                            "SELECT m.id, e.id, p.id " +
                                    "FROM Match m " +
                                    "JOIN MatchEvent e ON e.matchId = m.id " +
                                    "JOIN GeneratedPlayer p ON e.playerId = p.id",
                            Object[].class
                    )
                    .setMaxResults(1000)
                    .getResultList();

            return rows.size();
        });
    }

    private PerformanceResult runQuerySortFilterPlayersByValue() {
        return runMeasuredTest("QUERY_SORT_FILTER_PLAYERS_BY_VALUE", () -> {
            List<GeneratedPlayer> players = entityManager.createQuery(
                            "SELECT p FROM GeneratedPlayer p " +
                                    "WHERE p.marketValue IS NOT NULL " +
                                    "AND p.leagueSlug = :league " +
                                    "ORDER BY p.marketValue DESC",
                            GeneratedPlayer.class
                    )
                    .setParameter("league", "england")
                    .setMaxResults(100)
                    .getResultList();

            return players.size();
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

    private void resetPerformanceDataInternal() {
        deleteDomesticMatchData();
        generatedPlayerRepository.deleteAll();
        deleteAllEuropeanCompetitionData();
    }

    private void deleteDomesticMatchData() {
        matchEventRepository.deleteAll();
        matchRepository.deleteAll();
    }

    private void deleteAllEuropeanCompetitionData() {
        europeanMatchEventRepository.deleteAll();
        europeanKnockoutMatchRepository.deleteAll();
        europeanMatchRepository.deleteAll();
        deleteEuropeanParticipantsBySeason(SEASON);
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

    private void deleteEuropeanParticipantsBySeason(String season) {
        try {
            entityManager.createQuery(
                            "DELETE FROM EuropeanCompetitionParticipant p WHERE p.season = :season"
                    )
                    .setParameter("season", season)
                    .executeUpdate();
        } catch (RuntimeException ignored) {
            System.out.println("PERFORMANCE_TEST | RESET | EuropeanCompetitionParticipant delete skipped");
        }
    }

    private long countEuropeanParticipants() {
        try {
            return entityManager.createQuery(
                            "SELECT COUNT(p) FROM EuropeanCompetitionParticipant p",
                            Long.class
                    )
                    .getSingleResult();
        } catch (RuntimeException ignored) {
            return -1;
        }
    }

    private void ensureGeneratedPlayersExist() {
        if (generatedPlayerRepository.count() > 0) {
            return;
        }

        generatedPlayerRepository.saveAll(
                realTeamRepository.findAll()
                        .stream()
                        .flatMap(team -> playerGenerator.generateSquadForTeam(team).stream())
                        .toList()
        );
    }

    private Long getFirstLeagueMatchId() {
        Page<Match> page = matchRepository.findAll(PageRequest.of(0, 1));

        if (page.isEmpty()) {
            return -1L;
        }

        return page.getContent().get(0).getId();
    }

    private Long getFirstEuropeanMatchId() {
        Page<EuropeanMatch> page = europeanMatchRepository.findAll(PageRequest.of(0, 1));

        if (page.isEmpty()) {
            return -1L;
        }

        return page.getContent().get(0).getId();
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

                if (LEAGUE_LOGO_SLUGS.contains(clubSlug)) {
                    continue;
                }

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