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

        if (random.nextInt(100) < 12) {
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

        match.setPossessionHome(40 + random.nextInt(30));
        match.setPossessionAway(100 - match.getPossessionHome());

        match.setShotsHome(5 + random.nextInt(16));
        match.setShotsAway(5 + random.nextInt(16));

        match.setShotsOnTargetHome(random.nextInt(match.getShotsHome() + 1));
        match.setShotsOnTargetAway(random.nextInt(match.getShotsAway() + 1));

        match.setCornersHome(random.nextInt(11));
        match.setCornersAway(random.nextInt(11));

        match.setFoulsHome(5 + random.nextInt(16));
        match.setFoulsAway(5 + random.nextInt(16));

        match.setOffsidesHome(random.nextInt(6));
        match.setOffsidesAway(random.nextInt(6));

        match.setYellowCardsHome(random.nextInt(5));
        match.setYellowCardsAway(random.nextInt(5));

        match.setRedCardsHome(random.nextInt(2));
        match.setRedCardsAway(random.nextInt(2));

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

        generateEventsForTeam(
                match,
                match.getHomeTeam(),
                match.getHomeGoals(),
                match.getYellowCardsHome(),
                match.getRedCardsHome(),
                true
        );

        generateEventsForTeam(
                match,
                match.getAwayTeam(),
                match.getAwayGoals(),
                match.getYellowCardsAway(),
                match.getRedCardsAway(),
                false
        );

        generateInjuryEvents(match);

        generateSubstitutionEvents(match, true);
        generateSubstitutionEvents(match, false);

        generatePenaltyMissEvents(match);
        generateVarEvents(match);
    }

    private void generateEventsForTeam(Match match,
                                       String teamName,
                                       int goals,
                                       int yellowCards,
                                       int redCards,
                                       boolean homeTeam) {

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
            event.setTeamSide(homeTeam ? "HOME" : "AWAY");
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
                    1 + random.nextInt(90),
                    "YELLOW_CARD",
                    homeTeam ? "Home player" : "Away player",
                    "Yellow card",
                    "/Images/match_events/yellow_card.png"
            );
        }

        for (int i = 0; i < redCards; i++) {
            saveEvent(
                    match,
                    1 + random.nextInt(90),
                    "RED_CARD",
                    homeTeam ? "Home player" : "Away player",
                    "Red card",
                    "/Images/match_events/red_card.png"
            );
        }
    }

    private void generateInjuryEvents(Match match) {

        int injuries = random.nextInt(100) < 18 ? 1 : 0;

        for (int i = 0; i < injuries; i++) {

            boolean homeTeam = random.nextBoolean();

            int injuryMinute = 10 + random.nextInt(65);

            String injuredPlayer = homeTeam ? "Home injured player" : "Away injured player";

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(injuryMinute);
            event.setEventType("INJURY");
            event.setTeamSide(homeTeam ? "HOME" : "AWAY");
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

        InjuredPlayerInfo injuredPlayerInfo = homeTeam
                ? injuredHomePlayers.remove(match.getId())
                : injuredAwayPlayers.remove(match.getId());

        int substitutions = 3 + random.nextInt(3);

        for (int i = 0; i < substitutions; i++) {

            boolean forcedInjurySubstitution = i == 0 && injuredPlayerInfo != null;

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());

            if (forcedInjurySubstitution) {
                event.setEventMinute(injuredPlayerInfo.injuryMinute() + 1);
            } else {
                event.setEventMinute(46 + random.nextInt(45));
            }

            event.setEventType("SUBSTITUTION");
            event.setTeamSide(homeTeam ? "HOME" : "AWAY");
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

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(1 + random.nextInt(90));
            event.setEventType("PENALTY_MISSED");
            event.setTeamSide(homeTeam ? "HOME" : "AWAY");
            event.setIconUrl("/Images/match_events/penalty_missed.png");
            event.setPlayerName(homeTeam ? "Home player" : "Away player");
            event.setDescription("Penalty missed");

            matchEventRepository.save(event);
        }
    }

    private void generateVarEvents(Match match) {

        int varEvents = random.nextInt(100) < 20 ? 1 : 0;

        for (int i = 0; i < varEvents; i++) {

            MatchEvent event = new MatchEvent();

            event.setMatchId(match.getId());
            event.setEventMinute(1 + random.nextInt(90));
            event.setEventType("VAR");
            event.setTeamSide("CENTER");
            event.setIconUrl("/Images/match_events/Var.png");
            event.setDescription("VAR review");

            matchEventRepository.save(event);
        }
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
                           String iconUrl) {

        MatchEvent event = new MatchEvent();

        event.setMatchId(match.getId());
        event.setEventMinute(minute);
        event.setEventType(eventType);
        event.setPlayerName(playerName);
        event.setDescription(description);
        event.setIconUrl(iconUrl);

        matchEventRepository.save(event);
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