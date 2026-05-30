package com.footballstats.dto;

public class KnockoutTieView {

    private String homeTeam;
    private String awayTeam;
    private String winner;
    private String label;

    public KnockoutTieView(String homeTeam, String awayTeam, String winner, String label) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.winner = winner;
        this.label = label;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public String getWinner() {
        return winner;
    }

    public String getLabel() {
        return label;
    }
}