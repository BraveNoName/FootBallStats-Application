package com.footballstats.service;

import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.Match;
import com.footballstats.model.MatchEvent;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.repository.MatchEventRepository;
import com.footballstats.repository.MatchRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class MatchGeneratorService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final Random random = new Random();


    public MatchGeneratorService(MatchRepository matchRepository,
                                 MatchEventRepository matchEventRepository,
                                 GeneratedPlayerRepository generatedPlayerRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.generatedPlayerRepository = generatedPlayerRepository;
    }

    @Transactional
    public void generateLeagueSeason(String league) {

        List<Long> oldMatchIds = matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(league)
                .stream()
                .map(Match::getId)
                .toList();

        if (!oldMatchIds.isEmpty()) {
            matchEventRepository.deleteByMatchIdIn(oldMatchIds);
        }

        matchRepository.deleteByLeague(league);

        List<TeamInfo> teams = loadTeamsFromLeagueFolder(league);

        if (teams.size() < 2) {
            throw new RuntimeException("Not enough teams for league: " + league);
        }

        List<Match> generatedMatches = generateRoundRobinMatches(league, teams);
        List<Match> savedMatches = matchRepository.saveAll(generatedMatches);

        for (Match match : savedMatches) {
            reduceInjuriesAndSuspensionsBeforeMatch(match);
            generateMatchEvents(match);
        }
    }

    private List<Match> generateRoundRobinMatches(String league, List<TeamInfo> teams) {
        List<TeamInfo> clubs = new ArrayList<>(teams);

        if (clubs.size() % 2 != 0) {
            clubs.add(new TeamInfo("BYE", ""));
        }

        int numberOfTeams = clubs.size();
        int roundsPerLeg = numberOfTeams - 1;
        int matchesPerRound = numberOfTeams / 2;

        List<TeamInfo> rotation = new ArrayList<>(clubs);
        List<Match> matches = new ArrayList<>();

        for (int round = 1; round <= roundsPerLeg; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                TeamInfo home = rotation.get(i);
                TeamInfo away = rotation.get(numberOfTeams - 1 - i);

                if (!home.name().equals("BYE") && !away.name().equals("BYE")) {
                    if (round % 2 == 0) {
                        matches.add(createMatch(league, round, away, home));
                    } else {
                        matches.add(createMatch(league, round, home, away));
                    }

                    int secondLegRound = round + roundsPerLeg;
                    matches.add(createMatch(league, secondLegRound, away, home));
                }
            }

            TeamInfo fixed = rotation.get(0);
            List<TeamInfo> rest = new ArrayList<>(rotation.subList(1, rotation.size()));

            Collections.rotate(rest, 1);

            rotation.clear();
            rotation.add(fixed);
            rotation.addAll(rest);
        }

        matches.sort(Comparator
                .comparingInt(Match::getRoundNumber)
                .thenComparing(Match::getMatchDate));

        return matches;
    }

    private Match createMatch(String league, int round, TeamInfo home, TeamInfo away) {
        Match match = new Match();

        match.setLeague(league);
        match.setRoundNumber(round);

        LocalDateTime seasonStart = LocalDateTime.of(2026, 8, 15, 18, 0);

        int dayOffset = random.nextInt(2);
        int hour = List.of(15, 17, 18, 20).get(random.nextInt(4));
        int minute = random.nextBoolean() ? 0 : 30;

        match.setMatchDate(
                seasonStart
                        .plusWeeks(round - 1L)
                        .plusDays(dayOffset)
                        .withHour(hour)
                        .withMinute(minute)
        );

        match.setHomeTeam(home.name());
        match.setAwayTeam(away.name());

        match.setHomeTeamLogo(home.logo());
        match.setAwayTeamLogo(away.logo());

        Formation homeFormation = randomFormation();
        Formation awayFormation = randomFormation();

        match.setHomeFormation(homeFormation.label);
        match.setAwayFormation(awayFormation.label);

        int homeGoals = generateGoals();
        int awayGoals = generateGoals();

        if (random.nextInt(100) < 8) {
            homeGoals++;
        }

        if (homeGoals >= 7 && awayGoals >= 5) {
            awayGoals = random.nextInt(3);
        }

        if (awayGoals >= 7 && homeGoals >= 5) {
            homeGoals = random.nextInt(3);
        }

        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);

        match.setHalfTimeHomeGoals(random.nextInt(homeGoals + 1));
        match.setHalfTimeAwayGoals(random.nextInt(awayGoals + 1));

        int possessionHome = 20 + random.nextInt(61);
        match.setPossessionHome(possessionHome);
        match.setPossessionAway(100 - possessionHome);

        match.setShotsHome(5 + random.nextInt(16));
        match.setShotsAway(5 + random.nextInt(16));

        match.setShotsOnTargetHome(random.nextInt(match.getShotsHome() + 1));
        match.setShotsOnTargetAway(random.nextInt(match.getShotsAway() + 1));

        match.setCornersHome(random.nextInt(11));
        match.setCornersAway(random.nextInt(11));

        match.setFoulsHome(5 + random.nextInt(16));
        match.setFoulsAway(5 + random.nextInt(16));

        match.setOffsidesHome(generateOffsides());
        match.setOffsidesAway(generateOffsides());

        match.setYellowCardsHome(generateYellowCards());
        match.setYellowCardsAway(generateYellowCards());

        match.setRedCardsHome(generateRedCards());
        match.setRedCardsAway(generateRedCards());

        applyRedCardImpact(match);

        match.setPassesHome(300 + random.nextInt(450));
        match.setPassesAway(300 + random.nextInt(450));

        match.setPassAccuracyHome(70 + random.nextInt(25));
        match.setPassAccuracyAway(70 + random.nextInt(25));

        match.setXgHome(Math.round(random.nextDouble() * 400.0) / 100.0);
        match.setXgAway(Math.round(random.nextDouble() * 400.0) / 100.0);

        match.setStatus("FINISHED");
        match.setStadium(home.name() + " Stadium");
        match.setReferee("Random Referee");

        return match;
    }

    private void generateMatchEvents(Match match) {
        TeamMatchState homeState = createTeamState(match, true);
        TeamMatchState awayState = createTeamState(match, false);

        List<PlannedEvent> plannedEvents = new ArrayList<>();

        addGoalPlans(plannedEvents, match.getHomeGoals(), PlannedEventType.HOME_GOAL);
        addGoalPlans(plannedEvents, match.getAwayGoals(), PlannedEventType.AWAY_GOAL);

        addCardPlans(plannedEvents, match.getYellowCardsHome(), PlannedEventType.HOME_YELLOW);
        addCardPlans(plannedEvents, match.getYellowCardsAway(), PlannedEventType.AWAY_YELLOW);

        addCardPlans(plannedEvents, match.getRedCardsHome(), PlannedEventType.HOME_RED);
        addCardPlans(plannedEvents, match.getRedCardsAway(), PlannedEventType.AWAY_RED);

        addSubstitutionPlans(plannedEvents, generateSubstitutionsCount(), PlannedEventType.HOME_SUBSTITUTION);
        addSubstitutionPlans(plannedEvents, generateSubstitutionsCount(), PlannedEventType.AWAY_SUBSTITUTION);

        if (random.nextInt(100) < 18) {
            plannedEvents.add(new PlannedEvent(1 + random.nextInt(90), PlannedEventType.INJURY));
        }

        if (random.nextInt(100) < 15) {
            PlannedEventType type = random.nextBoolean()
                    ? PlannedEventType.HOME_PENALTY_MISSED
                    : PlannedEventType.AWAY_PENALTY_MISSED;
            plannedEvents.add(new PlannedEvent(1 + random.nextInt(90), type));
        }

        plannedEvents.sort(Comparator
                .comparingInt(PlannedEvent::minute)
                .thenComparingInt(event -> eventPriority(event.type())));

        for (PlannedEvent plannedEvent : plannedEvents) {
            handlePlannedEvent(match, plannedEvent, homeState, awayState);
        }
    }

    private TeamMatchState createTeamState(Match match, boolean homeTeam) {
        List<GeneratedPlayer> teamPlayers = getTeamPlayers(match, homeTeam);

        List<GeneratedPlayer> startingPlayers = getStartingPlayers(
                teamPlayers,
                homeTeam ? match.getHomeFormation() : match.getAwayFormation()
        );

        List<GeneratedPlayer> benchPlayers = getBenchPlayers(teamPlayers, startingPlayers);

        return new TeamMatchState(homeTeam, startingPlayers, benchPlayers);
    }

    private void addGoalPlans(List<PlannedEvent> events, int goals, PlannedEventType type) {
        for (int i = 0; i < goals; i++) {
            int minute;
            int guard = 0;

            do {
                minute = 1 + random.nextInt(90);
                guard++;
            } while (hasGoalInSameMinute(events, minute) && guard < 200);

            events.add(new PlannedEvent(minute, type));
        }
    }

    private boolean hasGoalInSameMinute(List<PlannedEvent> events, int minute) {
        return events.stream()
                .anyMatch(event -> event.minute() == minute
                        && (event.type() == PlannedEventType.HOME_GOAL || event.type() == PlannedEventType.AWAY_GOAL));
    }

    private void addCardPlans(List<PlannedEvent> events, int cards, PlannedEventType type) {
        for (int i = 0; i < cards; i++) {
            events.add(new PlannedEvent(generateCardMinute(), type));
        }
    }

    private void addSubstitutionPlans(List<PlannedEvent> events, int substitutions, PlannedEventType type) {
        for (int i = 0; i < substitutions; i++) {
            events.add(new PlannedEvent(generateSubstitutionMinute(), type));
        }
    }

    private int eventPriority(PlannedEventType type) {
        return switch (type) {
            case HOME_RED, AWAY_RED -> 1;
            case HOME_YELLOW, AWAY_YELLOW -> 2;
            case INJURY -> 3;
            case HOME_SUBSTITUTION, AWAY_SUBSTITUTION -> 4;
            case HOME_PENALTY_MISSED, AWAY_PENALTY_MISSED -> 5;
            case HOME_GOAL, AWAY_GOAL -> 6;
        };
    }

    private void handlePlannedEvent(Match match,
                                    PlannedEvent event,
                                    TeamMatchState homeState,
                                    TeamMatchState awayState) {
        switch (event.type()) {
            case HOME_GOAL -> handleGoal(match, event.minute(), homeState, awayState);
            case AWAY_GOAL -> handleGoal(match, event.minute(), awayState, homeState);
            case HOME_YELLOW -> handleYellowCard(match, event.minute(), homeState);
            case AWAY_YELLOW -> handleYellowCard(match, event.minute(), awayState);
            case HOME_RED -> handleRedCard(match, event.minute(), homeState);
            case AWAY_RED -> handleRedCard(match, event.minute(), awayState);
            case HOME_SUBSTITUTION -> handleTacticalSubstitution(match, event.minute(), homeState);
            case AWAY_SUBSTITUTION -> handleTacticalSubstitution(match, event.minute(), awayState);
            case HOME_PENALTY_MISSED -> handlePenaltyMissed(match, event.minute(), homeState);
            case AWAY_PENALTY_MISSED -> handlePenaltyMissed(match, event.minute(), awayState);
            case INJURY -> handleInjury(match, event.minute(), random.nextBoolean() ? homeState : awayState);
        }
    }

    private void handleGoal(Match match, int minute, TeamMatchState scoringState, TeamMatchState opponentState) {
        int roll = random.nextInt(100);

        String eventType;
        String description;
        String iconUrl;
        GeneratedPlayer scorer;

        if (roll < 8) {
            eventType = "PENALTY_GOAL";
            description = "Penalty scored";
            iconUrl = "/Images/match_events/penalty.png";
            scorer = randomFieldPlayerOnPitch(scoringState);
        } else if (roll < 12) {
            /*
             * OWN_GOAL jest golem dla scoringState, ale zawodnikiem zapisanym przy evencie
             * musi być gracz przeciwnika. Dzięki temu suma bramek z timeline'u zgadza się
             * z wynikiem meczu: samobój HOME liczy się dla AWAY i odwrotnie.
             */
            eventType = "OWN_GOAL";
            description = "Own goal";
            iconUrl = "/Images/match_events/own_goal.png";
            scorer = randomFieldPlayerOnPitch(opponentState);
        } else {
            eventType = "GOAL";
            description = "Goal";
            iconUrl = "/Images/match_events/goal.png";
            scorer = randomFieldPlayerOnPitch(scoringState);
        }

        if (scorer == null) {
            return;
        }

        MatchEvent event = new MatchEvent();
        event.setMatchId(match.getId());
        event.setEventMinute(minute);
        event.setEventType(eventType);
        event.setTeamSide(scoringState.teamSide);
        event.setIconUrl(iconUrl);
        event.setPlayerId(playerId(scorer));
        event.setPlayerName(playerFullName(scorer, eventType.equals("OWN_GOAL")
                ? (opponentState.homeTeam ? "Home player" : "Away player")
                : (scoringState.homeTeam ? "Home player" : "Away player")));

        if (eventType.equals("GOAL") && random.nextInt(100) < 75) {
            GeneratedPlayer assistant = randomDifferentFieldPlayerOnPitch(scoringState, scorer);

            if (assistant != null) {
                event.setRelatedPlayerId(playerId(assistant));
                event.setRelatedPlayerName(playerFullName(assistant, scoringState.homeTeam ? "Home assistant" : "Away assistant"));
                event.setDescription("Goal, assist");
            } else {
                event.setDescription(description);
            }
        } else {
            event.setDescription(description);
        }

        matchEventRepository.save(event);
    }

    private void handleYellowCard(Match match, int minute, TeamMatchState state) {
        /*
         * Najpierw szukamy zawodnika bez żółtej kartki.
         * Dopiero gdy każdy dostępny zawodnik ma już żółtą, losujemy gracza z jedną żółtą
         * i robimy SECOND_YELLOW_RED_CARD. Dzięki temu zwykłe YELLOW_CARD nie zapisze się
         * drugi raz dla tego samego PLAYER_ID w jednym meczu.
         */
        GeneratedPlayer player = randomPlayerOnPitchWithoutYellow(state);

        if (player == null) {
            player = randomPlayerOnPitchWithOneYellow(state);
        }

        if (player == null || player.getId() == null) {
            return;
        }

        int currentYellowCards = state.yellowCards.getOrDefault(player.getId(), 0);

        if (currentYellowCards >= 1) {
            handleSecondYellow(match, minute, state, player);
            return;
        }

        state.yellowCards.put(player.getId(), 1);

        saveEvent(
                match,
                minute,
                "YELLOW_CARD",
                playerId(player),
                playerFullName(player, state.homeTeam ? "Home player" : "Away player"),
                null,
                null,
                "Yellow card",
                "/Images/match_events/yellow_card.png",
                state.teamSide
        );
    }

    private void handleSecondYellow(Match match, int minute, TeamMatchState state, GeneratedPlayer player) {
        saveEvent(
                match,
                minute,
                "SECOND_YELLOW_RED_CARD",
                playerId(player),
                playerFullName(player, state.homeTeam ? "Home player" : "Away player"),
                null,
                null,
                "Second yellow card",
                "/Images/match_events/two_yellows_to_red_card.png",
                state.teamSide
        );

        state.statuses.put(player.getId(), PlayerMatchStatus.SENT_OFF);
        suspendPlayer(player);

        if (matchesPosition(player, "goalkeeper")) {
            handleGoalkeeperProblem(match, minute, state, "Goalkeeper sent off");
        }
    }

    private void handleRedCard(Match match, int minute, TeamMatchState state) {
        GeneratedPlayer player = randomPlayerOnPitch(state);

        if (player == null || player.getId() == null) {
            return;
        }

        saveEvent(
                match,
                minute,
                "RED_CARD",
                playerId(player),
                playerFullName(player, state.homeTeam ? "Home player" : "Away player"),
                null,
                null,
                "Red card",
                "/Images/match_events/red_card.png",
                state.teamSide
        );

        state.statuses.put(player.getId(), PlayerMatchStatus.SENT_OFF);
        suspendPlayer(player);

        if (matchesPosition(player, "goalkeeper")) {
            handleGoalkeeperProblem(match, minute, state, "Goalkeeper sent off");
        }
    }

    private void handleInjury(Match match, int minute, TeamMatchState state) {
        GeneratedPlayer injuredPlayer = randomPlayerOnPitch(state);

        if (injuredPlayer == null || injuredPlayer.getId() == null) {
            return;
        }

        saveEvent(
                match,
                minute,
                "INJURY",
                playerId(injuredPlayer),
                playerFullName(injuredPlayer, state.homeTeam ? "Home injured player" : "Away injured player"),
                null,
                null,
                "Injury: " + getRandomInjuryType(),
                "/Images/match_events/injury.png",
                state.teamSide
        );

        state.statuses.put(injuredPlayer.getId(), PlayerMatchStatus.INJURED_OFF);

        int injuredForMatches = 1 + random.nextInt(4);
        injuredPlayer.setInjuredMatches(injuredForMatches);
        generatedPlayerRepository.save(injuredPlayer);

        if (matchesPosition(injuredPlayer, "goalkeeper")) {
            handleGoalkeeperProblem(match, minute, state, "Goalkeeper injury");
        } else {
            handleForcedInjurySubstitution(match, minute, state, injuredPlayer);
        }
    }

    private void handleTacticalSubstitution(Match match, int minute, TeamMatchState state) {
        if (state.substitutionsUsed >= 5 || state.substitutionWindowsUsed >= 3) {
            return;
        }

        GeneratedPlayer playerOut = randomFieldPlayerOnPitch(state);
        GeneratedPlayer playerIn = randomBenchPlayer(state, false);

        if (playerOut == null || playerIn == null) {
            return;
        }

        makeSubstitution(match, minute, state, playerOut, playerIn, "Substitution");
        state.substitutionWindowsUsed++;
    }

    private void handleForcedInjurySubstitution(Match match,
                                                int minute,
                                                TeamMatchState state,
                                                GeneratedPlayer injuredPlayer) {
        if (state.substitutionsUsed >= 5) {
            return;
        }

        GeneratedPlayer playerIn = randomBenchPlayer(state, false);

        if (playerIn == null) {
            return;
        }

        makeSubstitution(match, Math.min(minute + 1, 90), state, injuredPlayer, playerIn, "Substitution due to injury");
    }

    private void handleGoalkeeperProblem(Match match, int minute, TeamMatchState state, String reason) {
        if (state.substitutionsUsed < 5) {
            GeneratedPlayer benchGoalkeeper = randomBenchPlayer(state, true);

            if (benchGoalkeeper != null) {
                GeneratedPlayer fieldPlayerOut = randomFieldPlayerOnPitch(state);

                if (fieldPlayerOut != null) {
                    makeSubstitution(
                            match,
                            Math.min(minute + 1, 90),
                            state,
                            fieldPlayerOut,
                            benchGoalkeeper,
                            reason + " - goalkeeper substitution"
                    );
                    return;
                }
            }
        }

        GeneratedPlayer emergencyGoalkeeper = randomFieldPlayerOnPitch(state);

        if (emergencyGoalkeeper == null || emergencyGoalkeeper.getId() == null) {
            return;
        }

        state.hasEmergencyGoalkeeper = true;

        saveEvent(
                match,
                Math.min(minute + 1, 90),
                "EMERGENCY_GOALKEEPER",
                playerId(emergencyGoalkeeper),
                playerFullName(emergencyGoalkeeper, state.homeTeam ? "Home emergency goalkeeper" : "Away emergency goalkeeper"),
                null,
                null,
                "Emergency goalkeeper after " + reason,
                "/Images/match_events/change.png",
                state.teamSide
        );
    }

    private void handlePenaltyMissed(Match match, int minute, TeamMatchState state) {
        GeneratedPlayer penaltyTaker = randomFieldPlayerOnPitch(state);

        if (penaltyTaker == null) {
            return;
        }

        saveEvent(
                match,
                minute,
                "PENALTY_MISSED",
                playerId(penaltyTaker),
                playerFullName(penaltyTaker, state.homeTeam ? "Home player" : "Away player"),
                null,
                null,
                "Penalty missed",
                "/Images/match_events/penalty_missed.png",
                state.teamSide
        );
    }

    private void makeSubstitution(Match match,
                                  int minute,
                                  TeamMatchState state,
                                  GeneratedPlayer playerOut,
                                  GeneratedPlayer playerIn,
                                  String description) {
        if (state.substitutionsUsed >= 5) {
            return;
        }

        if (playerOut == null || playerIn == null || playerOut.getId() == null || playerIn.getId() == null) {
            return;
        }

        if (state.statuses.get(playerIn.getId()) != PlayerMatchStatus.BENCH) {
            return;
        }

        PlayerMatchStatus playerOutStatus = state.statuses.get(playerOut.getId());

        if (playerOutStatus != PlayerMatchStatus.ON_PITCH
                && playerOutStatus != PlayerMatchStatus.INJURED_OFF) {
            return;
        }

        if (playerOutStatus == PlayerMatchStatus.ON_PITCH) {
            state.statuses.put(playerOut.getId(), PlayerMatchStatus.SUBSTITUTED);
        }

        state.statuses.put(playerIn.getId(), PlayerMatchStatus.ON_PITCH);
        state.substitutionsUsed++;

        saveEvent(
                match,
                minute,
                "SUBSTITUTION",
                playerId(playerIn),
                playerFullName(playerIn, state.homeTeam ? "Home player in" : "Away player in"),
                playerId(playerOut),
                playerFullName(playerOut, state.homeTeam ? "Home player out" : "Away player out"),
                description,
                "/Images/match_events/change.png",
                state.teamSide
        );
    }

    private List<GeneratedPlayer> playersOnPitch(TeamMatchState state) {
        return state.statuses.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == PlayerMatchStatus.ON_PITCH)
                .map(Map.Entry::getKey)
                .map(id -> findPlayerById(state.startingPlayers, state.benchPlayers, id))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<GeneratedPlayer> fieldPlayersOnPitch(TeamMatchState state) {
        return playersOnPitch(state)
                .stream()
                .filter(player -> !matchesPosition(player, "goalkeeper"))
                .toList();
    }

    private GeneratedPlayer randomPlayerOnPitchWithoutYellow(TeamMatchState state) {
        List<GeneratedPlayer> candidates = playersOnPitch(state)
                .stream()
                .filter(player -> player.getId() != null)
                .filter(player -> state.yellowCards.getOrDefault(player.getId(), 0) == 0)
                .toList();

        return randomPlayer(candidates);
    }

    private GeneratedPlayer randomPlayerOnPitchWithOneYellow(TeamMatchState state) {
        List<GeneratedPlayer> candidates = playersOnPitch(state)
                .stream()
                .filter(player -> player.getId() != null)
                .filter(player -> state.yellowCards.getOrDefault(player.getId(), 0) == 1)
                .toList();

        return randomPlayer(candidates);
    }

    private GeneratedPlayer randomPlayerOnPitch(TeamMatchState state) {
        return randomPlayer(playersOnPitch(state));
    }

    private GeneratedPlayer randomFieldPlayerOnPitch(TeamMatchState state) {
        List<GeneratedPlayer> fieldPlayers = fieldPlayersOnPitch(state);

        if (fieldPlayers.isEmpty()) {
            return randomPlayerOnPitch(state);
        }

        return randomPlayer(fieldPlayers);
    }

    private GeneratedPlayer randomDifferentFieldPlayerOnPitch(TeamMatchState state, GeneratedPlayer excluded) {
        List<GeneratedPlayer> fieldPlayers = fieldPlayersOnPitch(state)
                .stream()
                .filter(player -> excluded == null || !Objects.equals(player.getId(), excluded.getId()))
                .toList();

        return randomPlayer(fieldPlayers);
    }

    private GeneratedPlayer randomBenchPlayer(TeamMatchState state, boolean goalkeeperOnly) {
        List<GeneratedPlayer> availableBench = state.benchPlayers.stream()
                .filter(player -> player.getId() != null)
                .filter(player -> state.statuses.get(player.getId()) == PlayerMatchStatus.BENCH)
                .filter(player -> !goalkeeperOnly || matchesPosition(player, "goalkeeper"))
                .toList();

        return randomPlayer(availableBench);
    }

    private GeneratedPlayer findPlayerById(List<GeneratedPlayer> startingPlayers,
                                           List<GeneratedPlayer> benchPlayers,
                                           Long id) {
        if (id == null) {
            return null;
        }

        return Stream.concat(startingPlayers.stream(), benchPlayers.stream())
                .filter(player -> id.equals(player.getId()))
                .findFirst()
                .orElse(null);
    }

    private void reduceInjuriesAndSuspensionsBeforeMatch(Match match) {
        List<GeneratedPlayer> players = new ArrayList<>();
        players.addAll(getTeamPlayers(match, true));
        players.addAll(getTeamPlayers(match, false));

        for (GeneratedPlayer player : players) {
            if (player.getInjuredMatches() > 0) {
                player.setInjuredMatches(player.getInjuredMatches() - 1);
            }

            if (player.getSuspendedMatches() > 0) {
                player.setSuspendedMatches(player.getSuspendedMatches() - 1);
            }

            generatedPlayerRepository.save(player);
        }
    }

    private void suspendPlayer(GeneratedPlayer player) {
        if (player == null) {
            return;
        }

        int suspensionLength = 1 + random.nextInt(3); // 1-3 matches
        player.setSuspendedMatches(suspensionLength);
        generatedPlayerRepository.save(player);
    }

    private Formation randomFormation() {
        Formation[] formations = Formation.values();
        return formations[random.nextInt(formations.length)];
    }

    private List<GeneratedPlayer> getStartingPlayers(List<GeneratedPlayer> players, String formationLabel) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        Formation formation = Arrays.stream(Formation.values())
                .filter(f -> f.label.equals(formationLabel))
                .findFirst()
                .orElse(Formation.F433);

        List<GeneratedPlayer> available = players.stream()
                .filter(player -> player.getInjuredMatches() <= 0)
                .filter(player -> player.getSuspendedMatches() <= 0)
                .toList();

        List<GeneratedPlayer> lineup = new ArrayList<>();

        lineup.addAll(bestPlayersByPosition(available, "goalkeeper", 1));
        lineup.addAll(bestPlayersByPosition(available, "defender", formation.defenders));
        lineup.addAll(bestPlayersByPosition(available, "midfielder", formation.midfielders));
        lineup.addAll(bestPlayersByPosition(available, "forward", formation.forwards));

        if (lineup.size() < 11) {
            List<GeneratedPlayer> rest = available.stream()
                    .filter(player -> !lineup.contains(player))
                    .sorted(playerValueComparator())
                    .limit(11 - lineup.size())
                    .toList();

            lineup.addAll(rest);
        }

        return lineup.stream()
                .distinct()
                .limit(11)
                .toList();
    }

    private List<GeneratedPlayer> getStartingPlayers(List<GeneratedPlayer> players) {
        return getStartingPlayers(players, "4-3-3");
    }

    private List<GeneratedPlayer> getBenchPlayers(List<GeneratedPlayer> players, List<GeneratedPlayer> startingPlayers) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        Set<Long> startingIds = new HashSet<>();

        for (GeneratedPlayer player : startingPlayers) {
            if (player.getId() != null) {
                startingIds.add(player.getId());
            }
        }

        return players.stream()
                .filter(player -> player.getInjuredMatches() <= 0)
                .filter(player -> player.getSuspendedMatches() <= 0)
                .filter(player -> player.getId() != null && !startingIds.contains(player.getId()))
                .sorted(playerValueComparator())
                .limit(9)
                .toList();
    }

    private List<GeneratedPlayer> bestPlayersByPosition(List<GeneratedPlayer> players, String type, int limit) {
        return players.stream()
                .filter(player -> matchesPosition(player, type))
                .sorted(playerValueComparator())
                .limit(limit)
                .toList();
    }

    private Comparator<GeneratedPlayer> playerValueComparator() {
        return Comparator
                .comparing(
                        GeneratedPlayer::getMarketValue,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
                .thenComparing(GeneratedPlayer::getShirtNumber, Comparator.nullsLast(Integer::compareTo));
    }

    private boolean matchesPosition(GeneratedPlayer player, String type) {
        String position = player.getPosition() == null
                ? ""
                : player.getPosition().toLowerCase();

        return switch (type) {
            case "goalkeeper" ->
                    position.contains("goalkeeper")
                            || position.contains("keeper")
                            || position.contains("bramkarz");

            case "defender" ->
                    position.contains("back")
                            || position.contains("defender")
                            || position.contains("centre-back")
                            || position.contains("center-back")
                            || position.contains("left-back")
                            || position.contains("right-back")
                            || position.contains("obrońca");

            case "midfielder" ->
                    position.contains("midfield")
                            || position.contains("midfielder")
                            || position.contains("defensive midfield")
                            || position.contains("central midfield")
                            || position.contains("attacking midfield")
                            || position.contains("pomocnik");

            case "forward" ->
                    position.contains("forward")
                            || position.contains("striker")
                            || position.contains("winger")
                            || position.contains("left winger")
                            || position.contains("right winger")
                            || position.contains("centre-forward")
                            || position.contains("center-forward")
                            || position.contains("napastnik");

            default -> false;
        };
    }

    private GeneratedPlayer randomPlayer(List<GeneratedPlayer> players) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        return players.get(random.nextInt(players.size()));
    }

    private Long playerId(GeneratedPlayer player) {
        return player == null ? null : player.getId();
    }

    private String playerFullName(GeneratedPlayer player, String fallback) {
        if (player == null) {
            return fallback;
        }

        return player.getFirstName() + " " + player.getLastName();
    }

    private int positionOrder(GeneratedPlayer player) {
        String position = player.getPosition() == null ? "" : player.getPosition().toLowerCase();

        if (position.contains("goalkeeper") || position.contains("bramkarz")) return 1;
        if (position.contains("back") || position.contains("defender") || position.contains("obrońca")) return 2;
        if (position.contains("midfielder") || position.contains("pomocnik")) return 3;
        if (position.contains("forward") || position.contains("striker") || position.contains("winger") || position.contains("napastnik")) return 4;

        return 5;
    }

    private String createSlug(String name) {
        return name.toLowerCase()
                .replace("&", "and")
                .replace("'", "")
                .replace(".", "")
                .replace(" ", "-");
    }

    private void applyRedCardImpact(Match match) {
        int homeRedCards = match.getRedCardsHome();
        int awayRedCards = match.getRedCardsAway();

        if (homeRedCards > awayRedCards) {
            int difference = homeRedCards - awayRedCards;

            match.setHomeGoals(Math.max(0, match.getHomeGoals() - difference));
            match.setAwayGoals(match.getAwayGoals() + random.nextInt(difference + 2));
        }

        if (awayRedCards > homeRedCards) {
            int difference = awayRedCards - homeRedCards;

            match.setAwayGoals(Math.max(0, match.getAwayGoals() - difference));
            match.setHomeGoals(match.getHomeGoals() + random.nextInt(difference + 2));
        }
    }

    private int generateGoals() {
        int roll = random.nextInt(1000);

        if (roll < 110) return 0;
        if (roll < 360) return 1;
        if (roll < 620) return 2;
        if (roll < 820) return 3;
        if (roll < 930) return 4;
        if (roll < 975) return 5;
        if (roll < 993) return 6;
        if (roll < 998) return 7;

        return 8;
    }

    private int generateYellowCards() {
        int roll = random.nextInt(1000);

        if (roll < 60) return 0;
        if (roll < 190) return 1;
        if (roll < 390) return 2;
        if (roll < 620) return 3;
        if (roll < 790) return 4;
        if (roll < 900) return 5;
        if (roll < 960) return 6;
        if (roll < 985) return 7;
        if (roll < 995) return 8;

        return 9 + random.nextInt(3);
    }

    private int generateRedCards() {
        int roll = random.nextInt(1000);

        if (roll < 900) return 0;
        if (roll < 985) return 1;

        return 2;
    }

    private int generateCardMinute() {
        int roll = random.nextInt(100);

        if (roll < 8) return 1 + random.nextInt(30);
        if (roll < 35) return 31 + random.nextInt(30);
        if (roll < 75) return 61 + random.nextInt(20);

        return 81 + random.nextInt(10);
    }

    private int generateSubstitutionsCount() {
        int roll = random.nextInt(100);

        if (roll < 3) return 0;
        if (roll < 8) return 1;
        if (roll < 18) return 2;
        if (roll < 35) return 3;
        if (roll < 65) return 4;

        return 5;
    }

    private int generateSubstitutionMinute() {
        int roll = random.nextInt(100);

        if (roll < 5) return 1 + random.nextInt(45);
        if (roll < 35) return 46 + random.nextInt(20);
        if (roll < 75) return 66 + random.nextInt(15);

        return 81 + random.nextInt(10);
    }

    private int generateOffsides() {
        int roll = random.nextInt(1000);

        if (roll < 80) return 0;
        if (roll < 220) return 1;
        if (roll < 420) return 2;
        if (roll < 620) return 3;
        if (roll < 780) return 4;
        if (roll < 890) return 5;
        if (roll < 950) return 6;
        if (roll < 980) return 7;
        if (roll < 992) return 8;

        return 9 + random.nextInt(4);
    }

    private String[] getRandomVarResult() {
        int roll = random.nextInt(100);

        if (roll < 20) return new String[]{"VAR_GOAL_DISALLOWED", "VAR: goal disallowed for offside"};
        if (roll < 38) return new String[]{"VAR_GOAL_CONFIRMED", "VAR: goal confirmed"};
        if (roll < 55) return new String[]{"VAR_PENALTY_AWARDED", "VAR: penalty awarded"};
        if (roll < 72) return new String[]{"VAR_NO_PENALTY", "VAR: no penalty"};
        if (roll < 86) return new String[]{"VAR_RED_CARD_CONFIRMED", "VAR: red card confirmed"};

        return new String[]{"VAR_NO_RED_CARD", "VAR: no red card"};
    }

    private String getRandomInjuryType() {
        return List.of(
                "Concussion",
                "Nasal fracture",
                "Zygomatic fracture",
                "Neck strain",
                "Scalp laceration",
                "Sports hernia",
                "Abdominal strain",
                "Rib contusion",
                "Rib fracture",
                "Lower back strain",
                "Shoulder dislocation",
                "Clavicle fracture",
                "Rotator cuff strain",
                "Finger sprain",
                "Scaphoid fracture",
                "Hip pointer",
                "Femoroacetabular impingement",
                "Hamstring strain",
                "Quadriceps strain",
                "Groin strain",
                "Quadriceps contusion",
                "Rectus femoris rupture",
                "Anterior cruciate ligament tear",
                "Posterior cruciate ligament tear",
                "Medial collateral ligament sprain",
                "Meniscus tear",
                "Patellar tendinitis",
                "Patellar dislocation",
                "Runner's knee",
                "Shin splints",
                "Calf strain",
                "Tibia fracture",
                "Fibula fracture",
                "Compartment syndrome",
                "Ankle sprain",
                "Ankle dislocation",
                "Achilles tendinitis",
                "Achilles tendon rupture",
                "Plantar fasciitis",
                "Metatarsal fracture",
                "Metatarsal stress fracture",
                "Foot injury",
                "Toe injury",
                "Turf toe"
        ).get(random.nextInt(44));
    }

    private void saveEvent(Match match,
                           int minute,
                           String eventType,
                           Long playerId,
                           String playerName,
                           Long relatedPlayerId,
                           String relatedPlayerName,
                           String description,
                           String iconUrl,
                           String teamSide) {

        MatchEvent event = new MatchEvent();

        event.setMatchId(match.getId());
        event.setEventMinute(minute);
        event.setEventType(eventType);
        event.setPlayerId(playerId);
        event.setPlayerName(playerName);
        event.setRelatedPlayerId(relatedPlayerId);
        event.setRelatedPlayerName(relatedPlayerName);
        event.setDescription(description);
        event.setIconUrl(iconUrl);
        event.setTeamSide(teamSide);

        matchEventRepository.save(event);
    }

    private List<GeneratedPlayer> getTeamPlayers(Match match, boolean homeTeam) {
        String leagueSlug = homeTeam ? match.getHomeLeagueSlug() : match.getAwayLeagueSlug();
        String clubSlug = homeTeam ? match.getHomeClubSlug() : match.getAwayClubSlug();

        if (leagueSlug == null || clubSlug == null) {
            String clubName = homeTeam ? match.getHomeTeam() : match.getAwayTeam();

            leagueSlug = match.getLeague();
            clubSlug = createSlug(clubName);
        }

        List<GeneratedPlayer> players = generatedPlayerRepository
                .findByLeagueSlugAndClubSlug(leagueSlug, clubSlug);

        if (players.isEmpty()) {
            return List.of();
        }

        return players.stream()
                .sorted(Comparator.comparingInt(this::positionOrder)
                        .thenComparing(playerValueComparator()))
                .toList();
    }

    private List<TeamInfo> loadTeamsFromLeagueFolder(String league) {
        String folderName = getLeagueFolderName(league);

        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();

            Resource[] resources = resolver.getResources(
                    "classpath:/static/Images/leagues/" + folderName + "/*.png"
            );

            List<TeamInfo> teams = new ArrayList<>();

            for (Resource resource : resources) {
                String fileName = resource.getFilename();

                if (fileName == null) {
                    continue;
                }

                String name = fileName.replace(".png", "");

                if (name.equals(name.toLowerCase())) {
                    continue;
                }

                String logoPath = "/Images/leagues/" + folderName + "/" + fileName;

                teams.add(new TeamInfo(name, logoPath));
            }

            teams.sort(Comparator.comparing(TeamInfo::name));

            return teams;

        } catch (Exception e) {
            throw new RuntimeException("Cannot load teams for league: " + league, e);
        }
    }

    private String getLeagueFolderName(String league) {
        return switch (league) {
            case "austria" -> "Austria - Bundesliga";
            case "belgium" -> "Belgium - Jupiler Pro League";
            case "bulgaria" -> "Bulgaria - efbet Liga";
            case "croatia" -> "Croatia - SuperSport HNL";
            case "czech-republic" -> "Czech Republic - Chance Liga";
            case "denmark" -> "Denmark - Superliga";
            case "england" -> "England - Premier League";
            case "france" -> "France - Ligue 1";
            case "germany" -> "Germany - Bundesliga";
            case "greece" -> "Greece - Super League 1";
            case "israel" -> "Israel - Ligat ha'Al";
            case "italy" -> "Italy - Serie A";
            case "netherlands" -> "Netherlands - Eredivisie";
            case "norway" -> "Norway - Eliteserien";
            case "poland" -> "Poland - PKO BP Ekstraklasa";
            case "portugal" -> "Portugal - Liga Portugal";
            case "romania" -> "Romania - SuperLiga";
            case "russia" -> "Russia - Premier Liga";
            case "scotland" -> "Scotland - Scottish Premiership";
            case "serbia" -> "Serbia - Super liga Srbije";
            case "spain" -> "Spain - LaLiga";
            case "sweden" -> "Sweden - Allsvenskan";
            case "switzerland" -> "Switzerland - Super League";
            case "turkiye" -> "Türkiye - Süper Lig";
            case "ukraine" -> "Ukraine - Premier Liga";
            default -> throw new RuntimeException("Unknown league: " + league);
        };
    }

    private enum Formation {
        F442("4-4-2", 4, 4, 2),
        F433("4-3-3", 4, 3, 3),
        F4231("4-2-3-1", 4, 5, 1),
        F41212("4-1-2-1-2", 4, 4, 2),
        F451("4-5-1", 4, 5, 1),
        F4321("4-3-2-1", 4, 5, 1),
        F4222("4-2-2-2", 4, 4, 2),
        F4141("4-1-4-1", 4, 5, 1),

        F352("3-5-2", 3, 5, 2),
        F343("3-4-3", 3, 4, 3),
        F3412("3-4-1-2", 3, 5, 2),
        F3421("3-4-2-1", 3, 6, 1),
        F3142("3-1-4-2", 3, 5, 2),
        F361("3-6-1", 3, 6, 1),

        F532("5-3-2", 5, 3, 2),
        F541("5-4-1", 5, 4, 1),
        F523("5-2-3", 5, 2, 3),
        F5212("5-2-1-2", 5, 4, 2),
        F5122("5-1-2-2", 5, 3, 2);

        private final String label;
        private final int defenders;
        private final int midfielders;
        private final int forwards;

        Formation(String label, int defenders, int midfielders, int forwards) {
            this.label = label;
            this.defenders = defenders;
            this.midfielders = midfielders;
            this.forwards = forwards;
        }
    }

    private enum PlayerMatchStatus {
        ON_PITCH,
        BENCH,
        SUBSTITUTED,
        SENT_OFF,
        INJURED_OFF
    }

    private enum PlannedEventType {
        HOME_GOAL,
        AWAY_GOAL,
        HOME_YELLOW,
        AWAY_YELLOW,
        HOME_RED,
        AWAY_RED,
        HOME_SUBSTITUTION,
        AWAY_SUBSTITUTION,
        HOME_PENALTY_MISSED,
        AWAY_PENALTY_MISSED,
        INJURY
    }

    private record PlannedEvent(int minute, PlannedEventType type) {
    }

    private static class TeamMatchState {
        private final boolean homeTeam;
        private final String teamSide;
        private final List<GeneratedPlayer> startingPlayers;
        private final List<GeneratedPlayer> benchPlayers;
        private final Map<Long, PlayerMatchStatus> statuses = new HashMap<>();
        private final Map<Long, Integer> yellowCards = new HashMap<>();
        private int substitutionsUsed = 0;
        private int substitutionWindowsUsed = 0;
        private boolean hasEmergencyGoalkeeper = false;

        private TeamMatchState(boolean homeTeam,
                               List<GeneratedPlayer> startingPlayers,
                               List<GeneratedPlayer> benchPlayers) {
            this.homeTeam = homeTeam;
            this.teamSide = homeTeam ? "HOME" : "AWAY";
            this.startingPlayers = startingPlayers;
            this.benchPlayers = benchPlayers;

            for (GeneratedPlayer player : startingPlayers) {
                if (player.getId() != null) {
                    statuses.put(player.getId(), PlayerMatchStatus.ON_PITCH);
                }
            }

            for (GeneratedPlayer player : benchPlayers) {
                if (player.getId() != null) {
                    statuses.put(player.getId(), PlayerMatchStatus.BENCH);
                }
            }
        }
    }

    private record TeamInfo(String name, String logo) {
    }
}