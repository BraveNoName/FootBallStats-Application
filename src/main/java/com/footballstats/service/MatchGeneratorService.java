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

@Service
public class MatchGeneratorService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final Random random = new Random();

    private final Map<Long, InjuredPlayerInfo> injuredHomePlayers = new HashMap<>();
    private final Map<Long, InjuredPlayerInfo> injuredAwayPlayers = new HashMap<>();
    private final Map<Long, Set<Long>> sentOffPlayers = new HashMap<>();

    public MatchGeneratorService(MatchRepository matchRepository,
                                 MatchEventRepository matchEventRepository,
                                 GeneratedPlayerRepository generatedPlayerRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.generatedPlayerRepository = generatedPlayerRepository;
    }

    @Transactional
    public void generateLeagueSeason(String league) {
        matchRepository.deleteByLeague(league);
        matchEventRepository.deleteAll();

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
        sentOffPlayers.remove(match.getId());

        generateInjuryEvents(match);

        generateEventsForTeam(match, match.getHomeGoals(), match.getYellowCardsHome(), match.getRedCardsHome(), true);
        generateEventsForTeam(match, match.getAwayGoals(), match.getYellowCardsAway(), match.getRedCardsAway(), false);

        generateSubstitutionEvents(match, true);
        generateSubstitutionEvents(match, false);

        generatePenaltyMissEvents(match);
        // generateVarEvents(match);
    }

    private void generateEventsForTeam(Match match,
                                       int goals,
                                       int yellowCards,
                                       int redCards,
                                       boolean homeTeam) {

        String teamSide = homeTeam ? "HOME" : "AWAY";

        List<GeneratedPlayer> teamPlayers = getTeamPlayers(match, homeTeam);
        List<GeneratedPlayer> startingPlayers = getStartingPlayers(
                teamPlayers,
                homeTeam ? match.getHomeFormation() : match.getAwayFormation()
        );

        for (int i = 0; i < goals; i++) {
            int roll = random.nextInt(100);

            String eventType;
            String description;
            String iconUrl;

            if (roll < 8) {
                eventType = "PENALTY_GOAL";
                description = "Penalty scored";
                iconUrl = "/Images/match_events/penalty.png";
            } else if (roll < 12) {
                eventType = "OWN_GOAL";
                description = "Own goal";
                iconUrl = "/Images/match_events/own_goal.png";
            } else {
                eventType = "GOAL";
                description = "Goal";
                iconUrl = "/Images/match_events/goal.png";
            }

            GeneratedPlayer scorer = randomAvailablePlayerWithoutGoalkeepers(startingPlayers, match.getId());

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(1 + random.nextInt(90));
            event.setEventType(eventType);
            event.setTeamSide(teamSide);
            event.setIconUrl(iconUrl);
            event.setPlayerId(playerId(scorer));
            event.setPlayerName(playerFullName(scorer, homeTeam ? "Home player" : "Away player"));

            if (eventType.equals("GOAL") && random.nextInt(100) < 75) {
                GeneratedPlayer assistant =
                        randomDifferentAvailablePlayerWithoutGoalkeeper(startingPlayers, scorer, match.getId());

                event.setRelatedPlayerId(playerId(assistant));
                event.setRelatedPlayerName(playerFullName(assistant, homeTeam ? "Home assistant" : "Away assistant"));
                event.setDescription("Goal, assist");
            } else {
                event.setDescription(description);
            }

            matchEventRepository.save(event);
        }

        for (int i = 0; i < yellowCards; i++) {
            GeneratedPlayer bookedPlayer = randomAvailablePlayer(startingPlayers, match.getId());

            saveEvent(
                    match,
                    generateCardMinute(),
                    "YELLOW_CARD",
                    playerId(bookedPlayer),
                    playerFullName(bookedPlayer, homeTeam ? "Home player" : "Away player"),
                    null,
                    null,
                    "Yellow card",
                    "/Images/match_events/yellow_card.png",
                    teamSide
            );
        }

        for (int i = 0; i < redCards; i++) {
            boolean secondYellow = random.nextInt(100) < 20; // red card after second yellow

            GeneratedPlayer sentOffPlayer =
                    randomPlayerWithoutInjury(startingPlayers, match.getId(), homeTeam);

            if (sentOffPlayer == null || isSentOff(match.getId(), sentOffPlayer)) {
                continue;
            }

            if (secondYellow) {
                MatchEvent secondYellowEvent = new MatchEvent();
                secondYellowEvent.setMatchId(match.getId());
                secondYellowEvent.setEventMinute(generateCardMinute());
                secondYellowEvent.setEventType("SECOND_YELLOW_RED_CARD");
                secondYellowEvent.setTeamSide(teamSide);
                secondYellowEvent.setIconUrl("/Images/match_events/two_yellows_to_red_card.png");
                secondYellowEvent.setPlayerId(playerId(sentOffPlayer));
                secondYellowEvent.setPlayerName(playerFullName(sentOffPlayer, homeTeam ? "Home player" : "Away player"));
                secondYellowEvent.setDescription("Second yellow card");

                matchEventRepository.save(secondYellowEvent);
                markPlayerSentOff(match.getId(), sentOffPlayer);
                suspendPlayer(sentOffPlayer);
                continue;
            }

            saveEvent(
                    match,
                    generateCardMinute(),
                    "RED_CARD",
                    playerId(sentOffPlayer),
                    playerFullName(sentOffPlayer, homeTeam ? "Home player" : "Away player"),
                    null,
                    null,
                    "Red card",
                    "/Images/match_events/red_card.png",
                    teamSide
            );

            markPlayerSentOff(match.getId(), sentOffPlayer);
            suspendPlayer(sentOffPlayer);
        }
    }

    private void generateInjuryEvents(Match match) {
        int injuries = random.nextInt(100) < 18 ? 1 : 0;

        for (int i = 0; i < injuries; i++) {
            boolean homeTeam = random.nextBoolean();
            String teamSide = homeTeam ? "HOME" : "AWAY";

            List<GeneratedPlayer> teamPlayers = getTeamPlayers(match, homeTeam);
            List<GeneratedPlayer> startingPlayers = getStartingPlayers(
                    teamPlayers,
                    homeTeam ? match.getHomeFormation() : match.getAwayFormation()
            );

            GeneratedPlayer injuredPlayer = randomAvailablePlayer(startingPlayers, match.getId());

            if (injuredPlayer == null) {
                continue;
            }

            int injuryMinute = 1 + random.nextInt(90);

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(injuryMinute);
            event.setEventType("INJURY");
            event.setTeamSide(teamSide);
            event.setIconUrl("/Images/match_events/injury.png");
            event.setPlayerId(playerId(injuredPlayer));
            event.setPlayerName(playerFullName(injuredPlayer, homeTeam ? "Home injured player" : "Away injured player"));
            event.setDescription("Injury: " + getRandomInjuryType());

            matchEventRepository.save(event);

            int injuredForMatches = 1 + random.nextInt(4);
            injuredPlayer.setInjuredMatches(injuredForMatches);
            generatedPlayerRepository.save(injuredPlayer);

            InjuredPlayerInfo injuredPlayerInfo =
                    new InjuredPlayerInfo(
                            playerId(injuredPlayer),
                            playerFullName(injuredPlayer, homeTeam ? "Home injured player" : "Away injured player"),
                            injuryMinute
                    );

            if (homeTeam) {
                injuredHomePlayers.put(match.getId(), injuredPlayerInfo);
            } else {
                injuredAwayPlayers.put(match.getId(), injuredPlayerInfo);
            }
        }
    }

    private void generateSubstitutionEvents(Match match, boolean homeTeam) {
        String teamSide = homeTeam ? "HOME" : "AWAY";

        List<GeneratedPlayer> teamPlayers = getTeamPlayers(match, homeTeam);
        List<GeneratedPlayer> startingPlayers = getStartingPlayers(
                teamPlayers,
                homeTeam ? match.getHomeFormation() : match.getAwayFormation()
        );
        List<GeneratedPlayer> benchPlayers = getBenchPlayers(teamPlayers, startingPlayers);

        InjuredPlayerInfo injuredPlayerInfo = homeTeam
                ? injuredHomePlayers.remove(match.getId())
                : injuredAwayPlayers.remove(match.getId());

        int substitutions = generateSubstitutionsCount();

        if (injuredPlayerInfo != null && substitutions == 0) {
            substitutions = 1;
        }

        Set<Long> usedPlayersIn = new HashSet<>();
        Set<Long> usedPlayersOut = new HashSet<>();

        for (int i = 0; i < substitutions; i++) {
            boolean forcedInjurySubstitution = i == 0 && injuredPlayerInfo != null;

            GeneratedPlayer playerIn;

            if (forcedInjurySubstitution) {
                boolean injuredWasGoalkeeper = startingPlayers.stream()
                        .filter(player -> player.getId() != null && player.getId().equals(injuredPlayerInfo.playerId()))
                        .findFirst()
                        .map(player -> matchesPosition(player, "goalkeeper"))
                        .orElse(false);

                if (injuredWasGoalkeeper) {
                    playerIn = randomUnusedPlayer(
                            benchPlayers.stream()
                                    .filter(player -> matchesPosition(player, "goalkeeper"))
                                    .filter(player -> !isSentOff(match.getId(), player))
                                    .toList(),
                            usedPlayersIn
                    );
                } else {
                    playerIn = randomUnusedPlayer(
                            benchPlayers.stream()
                                    .filter(player -> !isSentOff(match.getId(), player))
                                    .toList(),
                            usedPlayersIn
                    );
                }
            } else {
                playerIn = randomUnusedPlayer(
                        benchPlayers.stream()
                                .filter(player -> !isSentOff(match.getId(), player))
                                .toList(),
                        usedPlayersIn
                );
            }

            GeneratedPlayer playerOut = forcedInjurySubstitution
                    ? null
                    : randomUnusedPlayer(
                    startingPlayers.stream()
                            .filter(player -> !isSentOff(match.getId(), player))
                            .filter(player -> !matchesPosition(player, "goalkeeper"))
                            .toList(),
                    usedPlayersOut
            );

            Long playerOutId;
            String playerOutName;

            if (forcedInjurySubstitution) {
                playerOutId = injuredPlayerInfo.playerId();
                playerOutName = injuredPlayerInfo.playerName();

                if (playerOutId != null) {
                    usedPlayersOut.add(playerOutId);
                }
            } else {
                playerOutId = playerId(playerOut);
                playerOutName = playerFullName(playerOut, homeTeam ? "Home player out" : "Away player out");
            }

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());

            if (forcedInjurySubstitution) {
                event.setEventMinute(Math.min(injuredPlayerInfo.injuryMinute() + 1, 90));
            } else {
                event.setEventMinute(generateSubstitutionMinute());
            }

            event.setEventType("SUBSTITUTION");
            event.setTeamSide(teamSide);
            event.setIconUrl("/Images/match_events/change.png");

            event.setPlayerId(playerId(playerIn));
            event.setPlayerName(playerFullName(playerIn, homeTeam ? "Home player in" : "Away player in"));

            event.setRelatedPlayerId(playerOutId);
            event.setRelatedPlayerName(playerOutName);

            event.setDescription(
                    forcedInjurySubstitution
                            ? "Substitution due to injury"
                            : "Substitution"
            );

            matchEventRepository.save(event);
        }
    }

    private void generatePenaltyMissEvents(Match match) {
        int penaltiesMissed = random.nextInt(100) < 15 ? 1 : 0;

        for (int i = 0; i < penaltiesMissed; i++) {
            boolean homeTeam = random.nextBoolean();

            List<GeneratedPlayer> teamPlayers = getStartingPlayers(
                    getTeamPlayers(match, homeTeam),
                    homeTeam ? match.getHomeFormation() : match.getAwayFormation()
            );

            GeneratedPlayer penaltyTaker = randomAvailablePlayerWithoutGoalkeepers(teamPlayers, match.getId());

            saveEvent(
                    match,
                    1 + random.nextInt(90),
                    "PENALTY_MISSED",
                    playerId(penaltyTaker),
                    playerFullName(penaltyTaker, homeTeam ? "Home player" : "Away player"),
                    null,
                    null,
                    "Penalty missed",
                    "/Images/match_events/penalty_missed.png",
                    homeTeam ? "HOME" : "AWAY"
            );
        }
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

    private void markPlayerSentOff(Long matchId, GeneratedPlayer player) {
        if (matchId == null || player == null || player.getId() == null) {
            return;
        }

        sentOffPlayers
                .computeIfAbsent(matchId, key -> new HashSet<>())
                .add(player.getId());
    }

    private boolean isSentOff(Long matchId, GeneratedPlayer player) {
        if (matchId == null || player == null || player.getId() == null) {
            return false;
        }

        return sentOffPlayers
                .getOrDefault(matchId, Set.of())
                .contains(player.getId());
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

    private GeneratedPlayer randomAvailablePlayer(List<GeneratedPlayer> players, Long matchId) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        List<GeneratedPlayer> available = players.stream()
                .filter(player -> !isSentOff(matchId, player))
                .toList();

        if (available.isEmpty()) {
            return null;
        }

        return randomPlayer(available);
    }

    private GeneratedPlayer randomAvailablePlayerWithoutGoalkeepers(List<GeneratedPlayer> players, Long matchId) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        List<GeneratedPlayer> fieldPlayers = players.stream()
                .filter(player -> !isSentOff(matchId, player))
                .filter(player -> !matchesPosition(player, "goalkeeper"))
                .toList();

        if (fieldPlayers.isEmpty()) {
            return randomAvailablePlayer(players, matchId);
        }

        return randomPlayer(fieldPlayers);
    }

    private GeneratedPlayer randomPlayerWithoutInjury(List<GeneratedPlayer> players,
                                                      Long matchId,
                                                      boolean homeTeam) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        InjuredPlayerInfo injuredPlayerInfo = homeTeam
                ? injuredHomePlayers.get(matchId)
                : injuredAwayPlayers.get(matchId);

        List<GeneratedPlayer> available = players.stream()
                .filter(player -> !isSentOff(matchId, player))
                .filter(player ->
                        injuredPlayerInfo == null
                                || injuredPlayerInfo.playerId() == null
                                || !injuredPlayerInfo.playerId().equals(player.getId())
                )
                .toList();

        if (available.isEmpty()) {
            return null;
        }

        return randomPlayer(available);
    }

    private GeneratedPlayer randomDifferentAvailablePlayerWithoutGoalkeeper(List<GeneratedPlayer> players,
                                                                            GeneratedPlayer excluded,
                                                                            Long matchId) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        List<GeneratedPlayer> available = players.stream()
                .filter(player -> !isSentOff(matchId, player))
                .filter(player -> !matchesPosition(player, "goalkeeper"))
                .filter(player -> excluded == null || !player.getId().equals(excluded.getId()))
                .toList();

        if (available.isEmpty()) {
            return null;
        }

        return randomPlayer(available);
    }

    private GeneratedPlayer randomUnusedPlayer(List<GeneratedPlayer> players, Set<Long> usedPlayerIds) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        List<GeneratedPlayer> available = players.stream()
                .filter(player -> player.getId() != null && !usedPlayerIds.contains(player.getId()))
                .toList();

        if (available.isEmpty()) {
            return randomPlayer(players);
        }

        GeneratedPlayer selectedPlayer = available.get(random.nextInt(available.size()));
        usedPlayerIds.add(selectedPlayer.getId());

        return selectedPlayer;
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
        String clubName = homeTeam ? match.getHomeTeam() : match.getAwayTeam();

        List<GeneratedPlayer> players = generatedPlayerRepository
                .findByLeagueSlugAndClubSlug(match.getLeague(), createSlug(clubName));

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

    private record TeamInfo(String name, String logo) {
    }

    private record InjuredPlayerInfo(Long playerId, String playerName, int injuryMinute) {
    }
}