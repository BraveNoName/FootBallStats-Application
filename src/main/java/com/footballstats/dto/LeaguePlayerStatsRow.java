package com.footballstats.dto;

public class LeaguePlayerStatsRow {

    private Long playerId;
    private String playerName;
    private String teamName;
    private String countryCode;

    private int goals;
    private int assists;

    public LeaguePlayerStatsRow(Long playerId,
                                String playerName,
                                String teamName,
                                String countryCode) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.teamName = teamName;
        this.countryCode = countryCode;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getGoals() {
        return goals;
    }

    public int getAssists() {
        return assists;
    }

    public void addGoal() {
        this.goals++;
    }

    public void addAssist() {
        this.assists++;
    }
}