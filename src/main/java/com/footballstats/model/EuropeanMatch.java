package com.footballstats.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EuropeanMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String season;
    private String competitionSlug;

    private String homeTeam;
    private String awayTeam;

    private String homeClubSlug;
    private String awayClubSlug;

    private String homeLeagueSlug;
    private String awayLeagueSlug;

    private Integer homeGoals;
    private Integer awayGoals;

    private Integer halfTimeHomeGoals;
    private Integer halfTimeAwayGoals;

    private int roundNumber;

    private LocalDateTime matchDate;

    private String status;
    private String stadium;
    private String referee;

    private Integer possessionHome;
    private Integer possessionAway;

    private Integer shotsHome;
    private Integer shotsAway;

    private Integer shotsOnTargetHome;
    private Integer shotsOnTargetAway;

    private Integer cornersHome;
    private Integer cornersAway;

    private Integer foulsHome;
    private Integer foulsAway;

    private Integer offsidesHome;
    private Integer offsidesAway;

    private Integer yellowCardsHome;
    private Integer yellowCardsAway;

    private Integer redCardsHome;
    private Integer redCardsAway;

    private Integer passesHome;
    private Integer passesAway;

    private Integer passAccuracyHome;
    private Integer passAccuracyAway;

    private Double xgHome;
    private Double xgAway;

    private String homeFormation;
    private String awayFormation;

    public Long getId() {
        return id;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getCompetitionSlug() {
        return competitionSlug;
    }

    public void setCompetitionSlug(String competitionSlug) {
        this.competitionSlug = competitionSlug;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getHomeClubSlug() {
        return homeClubSlug;
    }

    public void setHomeClubSlug(String homeClubSlug) {
        this.homeClubSlug = homeClubSlug;
    }

    public String getAwayClubSlug() {
        return awayClubSlug;
    }

    public void setAwayClubSlug(String awayClubSlug) {
        this.awayClubSlug = awayClubSlug;
    }

    public String getHomeLeagueSlug() {
        return homeLeagueSlug;
    }

    public void setHomeLeagueSlug(String homeLeagueSlug) {
        this.homeLeagueSlug = homeLeagueSlug;
    }

    public String getAwayLeagueSlug() {
        return awayLeagueSlug;
    }

    public void setAwayLeagueSlug(String awayLeagueSlug) {
        this.awayLeagueSlug = awayLeagueSlug;
    }

    public Integer getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(Integer homeGoals) {
        this.homeGoals = homeGoals;
    }

    public Integer getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(Integer awayGoals) {
        this.awayGoals = awayGoals;
    }

    public Integer getHalfTimeHomeGoals() {
        return halfTimeHomeGoals;
    }

    public void setHalfTimeHomeGoals(Integer halfTimeHomeGoals) {
        this.halfTimeHomeGoals = halfTimeHomeGoals;
    }

    public Integer getHalfTimeAwayGoals() {
        return halfTimeAwayGoals;
    }

    public void setHalfTimeAwayGoals(Integer halfTimeAwayGoals) {
        this.halfTimeAwayGoals = halfTimeAwayGoals;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public String getReferee() {
        return referee;
    }

    public void setReferee(String referee) {
        this.referee = referee;
    }

    public Integer getPossessionHome() {
        return possessionHome;
    }

    public void setPossessionHome(Integer possessionHome) {
        this.possessionHome = possessionHome;
    }

    public Integer getPossessionAway() {
        return possessionAway;
    }

    public void setPossessionAway(Integer possessionAway) {
        this.possessionAway = possessionAway;
    }

    public Integer getShotsHome() {
        return shotsHome;
    }

    public void setShotsHome(Integer shotsHome) {
        this.shotsHome = shotsHome;
    }

    public Integer getShotsAway() {
        return shotsAway;
    }

    public void setShotsAway(Integer shotsAway) {
        this.shotsAway = shotsAway;
    }

    public Integer getShotsOnTargetHome() {
        return shotsOnTargetHome;
    }

    public void setShotsOnTargetHome(Integer shotsOnTargetHome) {
        this.shotsOnTargetHome = shotsOnTargetHome;
    }

    public Integer getShotsOnTargetAway() {
        return shotsOnTargetAway;
    }

    public void setShotsOnTargetAway(Integer shotsOnTargetAway) {
        this.shotsOnTargetAway = shotsOnTargetAway;
    }

    public Integer getCornersHome() {
        return cornersHome;
    }

    public void setCornersHome(Integer cornersHome) {
        this.cornersHome = cornersHome;
    }

    public Integer getCornersAway() {
        return cornersAway;
    }

    public void setCornersAway(Integer cornersAway) {
        this.cornersAway = cornersAway;
    }

    public Integer getFoulsHome() {
        return foulsHome;
    }

    public void setFoulsHome(Integer foulsHome) {
        this.foulsHome = foulsHome;
    }

    public Integer getFoulsAway() {
        return foulsAway;
    }

    public void setFoulsAway(Integer foulsAway) {
        this.foulsAway = foulsAway;
    }

    public Integer getOffsidesHome() {
        return offsidesHome;
    }

    public void setOffsidesHome(Integer offsidesHome) {
        this.offsidesHome = offsidesHome;
    }

    public Integer getOffsidesAway() {
        return offsidesAway;
    }

    public void setOffsidesAway(Integer offsidesAway) {
        this.offsidesAway = offsidesAway;
    }

    public Integer getYellowCardsHome() {
        return yellowCardsHome;
    }

    public void setYellowCardsHome(Integer yellowCardsHome) {
        this.yellowCardsHome = yellowCardsHome;
    }

    public Integer getYellowCardsAway() {
        return yellowCardsAway;
    }

    public void setYellowCardsAway(Integer yellowCardsAway) {
        this.yellowCardsAway = yellowCardsAway;
    }

    public Integer getRedCardsHome() {
        return redCardsHome;
    }

    public void setRedCardsHome(Integer redCardsHome) {
        this.redCardsHome = redCardsHome;
    }

    public Integer getRedCardsAway() {
        return redCardsAway;
    }

    public void setRedCardsAway(Integer redCardsAway) {
        this.redCardsAway = redCardsAway;
    }

    public Integer getPassesHome() {
        return passesHome;
    }

    public void setPassesHome(Integer passesHome) {
        this.passesHome = passesHome;
    }

    public Integer getPassesAway() {
        return passesAway;
    }

    public void setPassesAway(Integer passesAway) {
        this.passesAway = passesAway;
    }

    public Integer getPassAccuracyHome() {
        return passAccuracyHome;
    }

    public void setPassAccuracyHome(Integer passAccuracyHome) {
        this.passAccuracyHome = passAccuracyHome;
    }

    public Integer getPassAccuracyAway() {
        return passAccuracyAway;
    }

    public void setPassAccuracyAway(Integer passAccuracyAway) {
        this.passAccuracyAway = passAccuracyAway;
    }

    public Double getXgHome() {
        return xgHome;
    }

    public void setXgHome(Double xgHome) {
        this.xgHome = xgHome;
    }

    public Double getXgAway() {
        return xgAway;
    }

    public void setXgAway(Double xgAway) {
        this.xgAway = xgAway;
    }

    public String getHomeFormation() {
        return homeFormation;
    }

    public void setHomeFormation(String homeFormation) {
        this.homeFormation = homeFormation;
    }

    public String getAwayFormation() {
        return awayFormation;
    }

    public void setAwayFormation(String awayFormation) {
        this.awayFormation = awayFormation;
    }
}