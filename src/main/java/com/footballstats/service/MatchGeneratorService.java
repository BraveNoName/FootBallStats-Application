package com.footballstats.service;

import com.footballstats.model.Match;
import com.footballstats.model.MatchEvent;
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
    private final Random random = new Random();

    private final Map<Long, InjuredPlayerInfo> injuredHomePlayers = new HashMap<>();
    private final Map<Long, InjuredPlayerInfo> injuredAwayPlayers = new HashMap<>();

    public MatchGeneratorService(MatchRepository matchRepository,
                                 MatchEventRepository matchEventRepository) {
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
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
        generateEventsForTeam(match, match.getHomeGoals(), match.getYellowCardsHome(), match.getRedCardsHome(), true);
        generateEventsForTeam(match, match.getAwayGoals(), match.getYellowCardsAway(), match.getRedCardsAway(), false);

        generateInjuryEvents(match);

        generateSubstitutionEvents(match, true);
        generateSubstitutionEvents(match, false);

        generatePenaltyMissEvents(match);
        generateVarEvents(match);
    }

    private void generateEventsForTeam(Match match,
                                       int goals,
                                       int yellowCards,
                                       int redCards,
                                       boolean homeTeam) {

        String teamSide = homeTeam ? "HOME" : "AWAY";

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
                iconUrl = "/Images/match_events/goal.png";
            } else {
                eventType = "GOAL";
                description = "Goal";
                iconUrl = "/Images/match_events/goal.png";
            }

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(1 + random.nextInt(90));
            event.setEventType(eventType);
            event.setTeamSide(teamSide);
            event.setIconUrl(iconUrl);
            event.setPlayerName(homeTeam ? "Home player" : "Away player");

            if (eventType.equals("GOAL") && random.nextInt(100) < 75) {
                event.setRelatedPlayerName(homeTeam ? "Home assistant" : "Away assistant");
                event.setDescription("Goal, assist");
            } else {
                event.setDescription(description);
            }

            matchEventRepository.save(event);
        }

        for (int i = 0; i < yellowCards; i++) {
            saveEvent(
                    match,
                    generateCardMinute(),
                    "YELLOW_CARD",
                    homeTeam ? "Home player" : "Away player",
                    "Yellow card",
                    "/Images/match_events/yellow_card.png",
                    teamSide
            );
        }

        for (int i = 0; i < redCards; i++) {
            boolean secondYellow = random.nextInt(100) < 45;

            saveEvent(
                    match,
                    generateCardMinute(),
                    secondYellow ? "SECOND_YELLOW_RED_CARD" : "RED_CARD",
                    homeTeam ? "Home player" : "Away player",
                    secondYellow ? "Second yellow card" : "Red card",
                    secondYellow
                            ? "/Images/match_events/two_yellows_to_red_card.png"
                            : "/Images/match_events/red_card.png",
                    teamSide
            );
        }
    }

    private void generateInjuryEvents(Match match) {
        int injuries = random.nextInt(100) < 18 ? 1 : 0;

        for (int i = 0; i < injuries; i++) {
            boolean homeTeam = random.nextBoolean();
            String teamSide = homeTeam ? "HOME" : "AWAY";

            int injuryMinute = 1 + random.nextInt(90);

            String injuredPlayer = homeTeam ? "Home injured player" : "Away injured player";

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(injuryMinute);
            event.setEventType("INJURY");
            event.setTeamSide(teamSide);
            event.setIconUrl("/Images/match_events/injury.png");
            event.setPlayerName(injuredPlayer);
            event.setDescription("Injury: " + getRandomInjuryType());

            matchEventRepository.save(event);

            InjuredPlayerInfo injuredPlayerInfo =
                    new InjuredPlayerInfo(injuredPlayer, injuryMinute);

            if (homeTeam) {
                injuredHomePlayers.put(match.getId(), injuredPlayerInfo);
            } else {
                injuredAwayPlayers.put(match.getId(), injuredPlayerInfo);
            }
        }
    }

    private void generateSubstitutionEvents(Match match, boolean homeTeam) {
        String teamSide = homeTeam ? "HOME" : "AWAY";

        InjuredPlayerInfo injuredPlayerInfo = homeTeam
                ? injuredHomePlayers.remove(match.getId())
                : injuredAwayPlayers.remove(match.getId());

        int substitutions = generateSubstitutionsCount();

        if (injuredPlayerInfo != null && substitutions == 0) {
            substitutions = 1;
        }

        for (int i = 0; i < substitutions; i++) {
            boolean forcedInjurySubstitution = i == 0 && injuredPlayerInfo != null;

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

            event.setPlayerName(homeTeam ? "Home player in" : "Away player in");

            event.setRelatedPlayerName(
                    forcedInjurySubstitution
                            ? injuredPlayerInfo.playerName()
                            : homeTeam ? "Home player out" : "Away player out"
            );

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

            saveEvent(
                    match,
                    1 + random.nextInt(90),
                    "PENALTY_MISSED",
                    homeTeam ? "Home player" : "Away player",
                    "Penalty missed",
                    "/Images/match_events/penalty_missed.png",
                    homeTeam ? "HOME" : "AWAY"
            );
        }
    }

    private void generateVarEvents(Match match) {
        int varEvents = random.nextInt(100) < 20 ? 1 : 0;

        for (int i = 0; i < varEvents; i++) {
            boolean homeTeam = random.nextBoolean();
            String teamSide = homeTeam ? "HOME" : "AWAY";

            int minute = 1 + random.nextInt(89);

            String[] result = getRandomVarResult();

            saveEvent(
                    match,
                    minute,
                    result[0],
                    "",
                    result[1],
                    "/Images/match_events/Var.png",
                    teamSide
            );

            if (result[0].equals("VAR_PENALTY_AWARDED")) {
                boolean scored = random.nextInt(100) < 78;

                saveEvent(
                        match,
                        minute + 1,
                        scored ? "PENALTY_GOAL" : "PENALTY_MISSED",
                        homeTeam ? "Home player" : "Away player",
                        scored ? "Penalty scored after VAR" : "Penalty missed after VAR",
                        scored
                                ? "/Images/match_events/penalty.png"
                                : "/Images/match_events/penalty_missed.png",
                        teamSide
                );
            }
        }
    }

    private String[] getRandomVarResult() {
        int roll = random.nextInt(100);

        if (roll < 20) {
            return new String[]{"VAR_GOAL_DISALLOWED", "VAR: goal disallowed for offside"};
        }

        if (roll < 38) {
            return new String[]{"VAR_GOAL_CONFIRMED", "VAR: goal confirmed"};
        }

        if (roll < 55) {
            return new String[]{"VAR_PENALTY_AWARDED", "VAR: penalty awarded"};
        }

        if (roll < 72) {
            return new String[]{"VAR_NO_PENALTY", "VAR: no penalty"};
        }

        if (roll < 86) {
            return new String[]{"VAR_RED_CARD_CONFIRMED", "VAR: red card confirmed"};
        }

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
                           String playerName,
                           String description,
                           String iconUrl,
                           String teamSide) {

        MatchEvent event = new MatchEvent();

        event.setMatchId(match.getId());
        event.setEventMinute(minute);
        event.setEventType(eventType);
        event.setPlayerName(playerName);
        event.setDescription(description);
        event.setIconUrl(iconUrl);
        event.setTeamSide(teamSide);

        matchEventRepository.save(event);
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

        if (roll < 90) return 0;
        if (roll < 320) return 1;
        if (roll < 570) return 2;
        if (roll < 760) return 3;
        if (roll < 880) return 4;
        if (roll < 945) return 5;
        if (roll < 980) return 6;
        if (roll < 993) return 7;
        if (roll < 998) return 8;

        return 9 + random.nextInt(3);
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

    private record TeamInfo(String name, String logo) {
    }

    private record InjuredPlayerInfo(String playerName, int injuryMinute) {
    }
}