package com.footballstats.dto;

public class LeagueTableRow {

    private String team;
    private String logo;

    private int played;
    private int wins;
    private int draws;
    private int losses;

    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;

    private int points;

    private String zone;

    public LeagueTableRow(String team, String logo) {
        this.team = team;
        this.logo = logo;
    }

    public void addWin(int goalsFor, int goalsAgainst) {
        this.played++;
        this.wins++;
        this.goalsFor += goalsFor;
        this.goalsAgainst += goalsAgainst;
        this.goalDifference = this.goalsFor - this.goalsAgainst;
        this.points += 3;
    }

    public void addDraw(int goalsFor, int goalsAgainst) {
        this.played++;
        this.draws++;
        this.goalsFor += goalsFor;
        this.goalsAgainst += goalsAgainst;
        this.goalDifference = this.goalsFor - this.goalsAgainst;
        this.points += 1;
    }

    public void addLoss(int goalsFor, int goalsAgainst) {
        this.played++;
        this.losses++;
        this.goalsFor += goalsFor;
        this.goalsAgainst += goalsAgainst;
        this.goalDifference = this.goalsFor - this.goalsAgainst;
    }

    public String getTeam() {
        return team;
    }

    public String getLogo() {
        return logo;
    }

    public int getPlayed() {
        return played;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public int getGoalDifference() {
        return goalDifference;
    }

    public int getPoints() {
        return points;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}