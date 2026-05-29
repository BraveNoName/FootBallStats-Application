package com.footballstats.dto;

public class EuropeanLeagueTableRow {

    private String team;
    private String leagueSlug;

    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int points;
    private String zone;

    public EuropeanLeagueTableRow(String team, String leagueSlug) {
        this.team = team;
        this.leagueSlug = leagueSlug;
    }

    public void addWin(int gf, int ga) {
        played++;
        wins++;
        goalsFor += gf;
        goalsAgainst += ga;
        goalDifference = goalsFor - goalsAgainst;
        points += 3;
    }

    public void addDraw(int gf, int ga) {
        played++;
        draws++;
        goalsFor += gf;
        goalsAgainst += ga;
        goalDifference = goalsFor - goalsAgainst;
        points += 1;
    }

    public void addLoss(int gf, int ga) {
        played++;
        losses++;
        goalsFor += gf;
        goalsAgainst += ga;
        goalDifference = goalsFor - goalsAgainst;
    }

    public String getTeam() { return team; }
    public String getLeagueSlug() { return leagueSlug; }
    public int getPlayed() { return played; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getGoalsFor() { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getGoalDifference() { return goalDifference; }
    public int getPoints() { return points; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
}