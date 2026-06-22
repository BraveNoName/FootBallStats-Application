package com.footballstats.dto;

import java.time.LocalDateTime;

public class KnockoutLegDetailsView {

    private final Long matchId;
    private final String legKey;
    private final String label;
    private final String homeTeam;
    private final String awayTeam;
    private final String homeLeagueSlug;
    private final String awayLeagueSlug;
    private final String homeClubSlug;
    private final String awayClubSlug;
    private final Integer homeGoals;
    private final Integer awayGoals;
    private final String status;
    private final String stadium;
    private final String referee;
    private final LocalDateTime matchDate;

    private final int possessionHome;
    private final int possessionAway;
    private final int shotsHome;
    private final int shotsAway;
    private final int shotsOnTargetHome;
    private final int shotsOnTargetAway;
    private final int cornersHome;
    private final int cornersAway;
    private final int foulsHome;
    private final int foulsAway;
    private final int offsidesHome;
    private final int offsidesAway;
    private final int yellowCardsHome;
    private final int yellowCardsAway;
    private final int redCardsHome;
    private final int redCardsAway;
    private final int passesHome;
    private final int passesAway;
    private final int passAccuracyHome;
    private final int passAccuracyAway;
    private final double xgHome;
    private final double xgAway;
    private final String homeFormation;
    private final String awayFormation;

    public KnockoutLegDetailsView(Long matchId, String legKey, String label,
                                  String homeTeam, String awayTeam,
                                  String homeLeagueSlug, String awayLeagueSlug,
                                  String homeClubSlug, String awayClubSlug,
                                  Integer homeGoals, Integer awayGoals,
                                  String status, String stadium, String referee,
                                  LocalDateTime matchDate,
                                  int possessionHome, int possessionAway,
                                  int shotsHome, int shotsAway,
                                  int shotsOnTargetHome, int shotsOnTargetAway,
                                  int cornersHome, int cornersAway,
                                  int foulsHome, int foulsAway,
                                  int offsidesHome, int offsidesAway,
                                  int yellowCardsHome, int yellowCardsAway,
                                  int redCardsHome, int redCardsAway,
                                  int passesHome, int passesAway,
                                  int passAccuracyHome, int passAccuracyAway,
                                  double xgHome, double xgAway,
                                  String homeFormation, String awayFormation) {
        this.matchId = matchId;
        this.legKey = legKey;
        this.label = label;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeLeagueSlug = homeLeagueSlug;
        this.awayLeagueSlug = awayLeagueSlug;
        this.homeClubSlug = homeClubSlug;
        this.awayClubSlug = awayClubSlug;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.status = status;
        this.stadium = stadium;
        this.referee = referee;
        this.matchDate = matchDate;
        this.possessionHome = possessionHome;
        this.possessionAway = possessionAway;
        this.shotsHome = shotsHome;
        this.shotsAway = shotsAway;
        this.shotsOnTargetHome = shotsOnTargetHome;
        this.shotsOnTargetAway = shotsOnTargetAway;
        this.cornersHome = cornersHome;
        this.cornersAway = cornersAway;
        this.foulsHome = foulsHome;
        this.foulsAway = foulsAway;
        this.offsidesHome = offsidesHome;
        this.offsidesAway = offsidesAway;
        this.yellowCardsHome = yellowCardsHome;
        this.yellowCardsAway = yellowCardsAway;
        this.redCardsHome = redCardsHome;
        this.redCardsAway = redCardsAway;
        this.passesHome = passesHome;
        this.passesAway = passesAway;
        this.passAccuracyHome = passAccuracyHome;
        this.passAccuracyAway = passAccuracyAway;
        this.xgHome = xgHome;
        this.xgAway = xgAway;
        this.homeFormation = homeFormation;
        this.awayFormation = awayFormation;
    }

    public Long getMatchId() { return matchId; }
    public String getLegKey() { return legKey; }
    public String getLabel() { return label; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public String getHomeLeagueSlug() { return homeLeagueSlug; }
    public String getAwayLeagueSlug() { return awayLeagueSlug; }
    public String getHomeClubSlug() { return homeClubSlug; }
    public String getAwayClubSlug() { return awayClubSlug; }
    public Integer getHomeGoals() { return homeGoals; }
    public Integer getAwayGoals() { return awayGoals; }
    public String getStatus() { return status; }
    public String getStadium() { return stadium; }
    public String getReferee() { return referee; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public int getPossessionHome() { return possessionHome; }
    public int getPossessionAway() { return possessionAway; }
    public int getShotsHome() { return shotsHome; }
    public int getShotsAway() { return shotsAway; }
    public int getShotsOnTargetHome() { return shotsOnTargetHome; }
    public int getShotsOnTargetAway() { return shotsOnTargetAway; }
    public int getCornersHome() { return cornersHome; }
    public int getCornersAway() { return cornersAway; }
    public int getFoulsHome() { return foulsHome; }
    public int getFoulsAway() { return foulsAway; }
    public int getOffsidesHome() { return offsidesHome; }
    public int getOffsidesAway() { return offsidesAway; }
    public int getYellowCardsHome() { return yellowCardsHome; }
    public int getYellowCardsAway() { return yellowCardsAway; }
    public int getRedCardsHome() { return redCardsHome; }
    public int getRedCardsAway() { return redCardsAway; }
    public int getPassesHome() { return passesHome; }
    public int getPassesAway() { return passesAway; }
    public int getPassAccuracyHome() { return passAccuracyHome; }
    public int getPassAccuracyAway() { return passAccuracyAway; }
    public double getXgHome() { return xgHome; }
    public double getXgAway() { return xgAway; }
    public String getHomeFormation() { return homeFormation; }
    public String getAwayFormation() { return awayFormation; }
}