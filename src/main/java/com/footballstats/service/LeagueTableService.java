package com.footballstats.service;

import com.footballstats.dto.LeagueTableRow;
import com.footballstats.model.Match;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeagueTableService {

    public List<LeagueTableRow> calculateTable(List<Match> matches) {

        Map<String, LeagueTableRow> table = new HashMap<>();

        for (Match match : matches) {

            table.putIfAbsent(
                    match.getHomeTeam(),
                    new LeagueTableRow(match.getHomeTeam(), match.getHomeTeamLogo())
            );

            table.putIfAbsent(
                    match.getAwayTeam(),
                    new LeagueTableRow(match.getAwayTeam(), match.getAwayTeamLogo())
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
                        .thenComparing(LeagueTableRow::getGoalDifference, Comparator.reverseOrder())
                        .thenComparing(LeagueTableRow::getGoalsFor, Comparator.reverseOrder())
        );

        assignZones(rows);

        return rows;
    }

    private void assignZones(List<LeagueTableRow> rows) {

        for (int i = 0; i < rows.size(); i++) {

            int position = i + 1;
            int size = rows.size();

            LeagueTableRow row = rows.get(i);

            if (position <= 4) {
                row.setZone("CHAMPIONS_LEAGUE");
            } else if (position == 5) {
                row.setZone("EUROPA_LEAGUE");
            } else if (position == 6) {
                row.setZone("CONFERENCE_LEAGUE");
            } else if (position == size - 2) {
                row.setZone("RELEGATION_PLAYOFF");
            } else if (position >= size - 1) {
                row.setZone("RELEGATION");
            } else {
                row.setZone("SAFE");
            }
        }
    }
}