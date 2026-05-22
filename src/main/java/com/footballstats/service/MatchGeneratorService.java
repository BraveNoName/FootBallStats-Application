package com.footballstats.service;

import com.footballstats.model.Match;
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
    private final Random random = new Random();

    public MatchGeneratorService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Transactional
    public void generateLeagueSeason(String league) {

        matchRepository.deleteByLeague(league);

        List<TeamInfo> teams = loadTeamsFromLeagueFolder(league);

        if (teams.size() < 2) {
            throw new RuntimeException("Not enough teams for league: " + league);
        }

        List<Match> generatedMatches = generateRoundRobinMatches(league, teams);

        matchRepository.saveAll(generatedMatches);
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

        /* GOALS */

        int homeGoals = generateGoals();
        int awayGoals = generateGoals();

        /* lekka przewaga gospodarzy */

        if (random.nextInt(100) < 12) {
            homeGoals++;
        }

        /* ograniczenie absurdów typu 9:8 */

        if (homeGoals >= 7 && awayGoals >= 5) {
            awayGoals = random.nextInt(3);
        }

        if (awayGoals >= 7 && homeGoals >= 5) {
            homeGoals = random.nextInt(3);
        }

        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);

        /* HALF TIME */

        match.setHalfTimeHomeGoals(random.nextInt(homeGoals + 1));
        match.setHalfTimeAwayGoals(random.nextInt(awayGoals + 1));

        /* POSSESSION */

        match.setPossessionHome(40 + random.nextInt(30));
        match.setPossessionAway(100 - match.getPossessionHome());

        /* SHOTS */

        match.setShotsHome(5 + random.nextInt(16));
        match.setShotsAway(5 + random.nextInt(16));

        match.setShotsOnTargetHome(random.nextInt(match.getShotsHome() + 1));
        match.setShotsOnTargetAway(random.nextInt(match.getShotsAway() + 1));

        /* OTHER STATS */

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
}