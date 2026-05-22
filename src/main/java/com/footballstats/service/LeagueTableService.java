package com.footballstats.service;

import com.footballstats.dto.LeagueTableRow;
import com.footballstats.model.Match;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeagueTableService {

    public List<LeagueTableRow> calculateTable(List<Match> matches, String league) {

        Map<String, LeagueTableRow> table = new HashMap<>();

        for (Match match : matches) {

            table.putIfAbsent(
                    match.getHomeTeam(),
                    new LeagueTableRow(
                            match.getHomeTeam(),
                            match.getHomeTeamLogo()
                    )
            );

            table.putIfAbsent(
                    match.getAwayTeam(),
                    new LeagueTableRow(
                            match.getAwayTeam(),
                            match.getAwayTeamLogo()
                    )
            );

            LeagueTableRow home = table.get(match.getHomeTeam());
            LeagueTableRow away = table.get(match.getAwayTeam());

            int homeGoals = match.getHomeGoals();
            int awayGoals = match.getAwayGoals();

            if (homeGoals > awayGoals) {

                home.addWin(homeGoals, awayGoals);
                away.addLoss(awayGoals, homeGoals);

            } else if (homeGoals < awayGoals) {

                away.addWin(awayGoals, homeGoals);
                home.addLoss(homeGoals, awayGoals);

            } else {

                home.addDraw(homeGoals, awayGoals);
                away.addDraw(awayGoals, homeGoals);
            }
        }

        List<LeagueTableRow> rows = new ArrayList<>(table.values());

        rows.sort(
                Comparator.comparingInt(LeagueTableRow::getPoints).reversed()
                        .thenComparing(
                                LeagueTableRow::getGoalDifference,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                LeagueTableRow::getGoalsFor,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(LeagueTableRow::getTeam)
        );

        assignZones(rows, league);

        return rows;
    }

    private void assignZones(List<LeagueTableRow> rows, String league) {

        for (int i = 0; i < rows.size(); i++) {

            int position = i + 1;
            int size = rows.size();

            LeagueTableRow row = rows.get(i);

            row.setZone(
                    getZoneForLeague(league, position, size)
            );
        }
    }

    private String getZoneForLeague(
            String league,
            int position,
            int size
    ) {

        return switch (league) {

            case "england", "spain", "italy" ->
                    topFourLeague(position, size);

            case "germany" ->
                    germanyRules(position, size);

            case "france" ->
                    franceRules(position, size);

            case "netherlands" ->
                    netherlandsRules(position, size);

            case "portugal" ->
                    portugalRules(position, size);

            case "belgium" ->
                    belgiumRules(position, size);

            case "turkiye" ->
                    turkiyeRules(position, size);

            case "scotland" ->
                    scotlandRules(position, size);

            case "austria" ->
                    austriaRules(position, size);

            case "switzerland" ->
                    switzerlandRules(position, size);

            case "greece" ->
                    greeceRules(position, size);

            case "denmark" ->
                    denmarkRules(position, size);

            case "poland" ->
                    polandRules(position, size);

            case "sweden", "norway" ->
                    scandinaviaRules(position, size);

            case "croatia" ->
                    croatiaRules(position, size);

            case "serbia" ->
                    serbiaRules(position, size);

            case "romania", "bulgaria" ->
                    romaniaBulgariaRules(position, size);

            case "ukraine", "czech-republic" ->
                    ukraineCzechRules(position, size);

            case "israel" ->
                    israelRules(position, size);

            case "russia" ->
                    russiaRules(position, size);

            default ->
                    defaultRules(position, size);
        };
    }

    private String topFourLeague(int position, int size) {

        if (position <= 4)
            return "CHAMPIONS_LEAGUE";

        if (position == 5)
            return "EUROPA_LEAGUE";

        if (position == 6)
            return "CONFERENCE_LEAGUE";

        if (position >= size - 2)
            return "RELEGATION";

        return "SAFE";
    }

    private String germanyRules(int position, int size) {

        if (position <= 4)
            return "CHAMPIONS_LEAGUE";

        if (position == 5)
            return "EUROPA_LEAGUE";

        if (position == 6)
            return "CONFERENCE_LEAGUE";

        if (position == size - 2)
            return "RELEGATION_PLAYOFF";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String franceRules(int position, int size) {

        if (position <= 3)
            return "CHAMPIONS_LEAGUE";

        if (position == 4)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 5)
            return "EUROPA_LEAGUE";

        if (position == 6)
            return "CONFERENCE_LEAGUE";

        if (position == size - 2)
            return "RELEGATION_PLAYOFF";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String netherlandsRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE";

        if (position == 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position <= 5)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 2)
            return "RELEGATION_PLAYOFF";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String portugalRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE";

        if (position == 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 2)
            return "RELEGATION_PLAYOFF";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String belgiumRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE";

        if (position == 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 1)
            return "RELEGATION_PLAYOFF";

        if (position == size)
            return "RELEGATION";

        return "SAFE";
    }

    private String turkiyeRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 2)
            return "RELEGATION";

        return "SAFE";
    }

    private String scotlandRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 1)
            return "RELEGATION_PLAYOFF";

        if (position == size)
            return "RELEGATION";

        return "SAFE";
    }

    private String austriaRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 1)
            return "RELEGATION_PLAYOFF";

        if (position == size)
            return "RELEGATION";

        return "SAFE";
    }

    private String switzerlandRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3 || position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 1)
            return "RELEGATION_PLAYOFF";

        if (position == size)
            return "RELEGATION";

        return "SAFE";
    }

    private String greeceRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String denmarkRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 1)
            return "RELEGATION_PLAYOFF";

        if (position == size)
            return "RELEGATION";

        return "SAFE";
    }

    private String polandRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 2)
            return "RELEGATION";

        return "SAFE";
    }

    private String scandinaviaRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 2)
            return "RELEGATION_PLAYOFF";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String croatiaRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 1)
            return "RELEGATION_PLAYOFF";

        if (position == size)
            return "RELEGATION";

        return "SAFE";
    }

    private String serbiaRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String romaniaBulgariaRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String ukraineCzechRules(int position, int size) {

        if (position <= 2)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 4)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String israelRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String russiaRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2)
            return "EUROPA_LEAGUE_QUALIFIERS";

        if (position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position == size - 3 || position == size - 2)
            return "RELEGATION_PLAYOFF";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }

    private String defaultRules(int position, int size) {

        if (position == 1)
            return "CHAMPIONS_LEAGUE_QUALIFIERS";

        if (position == 2 || position == 3)
            return "CONFERENCE_LEAGUE_QUALIFIERS";

        if (position >= size - 1)
            return "RELEGATION";

        return "SAFE";
    }
}