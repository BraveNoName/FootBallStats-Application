package com.footballstats.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EuropeanKnockoutMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String season;
    private String competitionSlug;

    private String round;
    private Integer bracketOrder;
    private String homeTeam;
    private String awayTeam;
    private String homeClubSlug;
    private String awayClubSlug;
    private String homeLeagueSlug;
    private String awayLeagueSlug;
    private Integer homeGoals;
    private Integer awayGoals;
    private String winnerTeam;
    private String winnerClubSlug;
    private String winnerLeagueSlug;
    private Integer roundOrder;
    private Long sourceMatchOneId;
    private Long sourceMatchTwoId;
    private Integer homeSeed;
    private Integer awaySeed;
    private Integer aggregateHomeGoals;
    private Integer aggregateAwayGoals;
    private LocalDateTime matchDate;
    private String status;
    private Integer firstLegHomeGoals;
    private Integer firstLegAwayGoals;
    private Integer secondLegHomeGoals;
    private Integer secondLegAwayGoals;
    private LocalDateTime firstLegDate;
    private LocalDateTime secondLegDate;

    private String firstLegReferee;
    private String secondLegReferee;

    public Long getId() { return id; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getCompetitionSlug() { return competitionSlug; }
    public void setCompetitionSlug(String competitionSlug) { this.competitionSlug = competitionSlug; }

    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }

    public Integer getBracketOrder() { return bracketOrder; }
    public void setBracketOrder(Integer bracketOrder) { this.bracketOrder = bracketOrder; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public String getHomeClubSlug() { return homeClubSlug; }
    public void setHomeClubSlug(String homeClubSlug) { this.homeClubSlug = homeClubSlug; }

    public String getAwayClubSlug() { return awayClubSlug; }
    public void setAwayClubSlug(String awayClubSlug) { this.awayClubSlug = awayClubSlug; }

    public String getHomeLeagueSlug() { return homeLeagueSlug; }
    public void setHomeLeagueSlug(String homeLeagueSlug) { this.homeLeagueSlug = homeLeagueSlug; }

    public String getAwayLeagueSlug() { return awayLeagueSlug; }
    public void setAwayLeagueSlug(String awayLeagueSlug) { this.awayLeagueSlug = awayLeagueSlug; }

    public Integer getHomeGoals() { return homeGoals; }
    public void setHomeGoals(Integer homeGoals) { this.homeGoals = homeGoals; }

    public Integer getAwayGoals() { return awayGoals; }
    public void setAwayGoals(Integer awayGoals) { this.awayGoals = awayGoals; }

    public String getWinnerTeam() { return winnerTeam; }
    public void setWinnerTeam(String winnerTeam) { this.winnerTeam = winnerTeam; }

    public String getWinnerClubSlug() { return winnerClubSlug; }
    public void setWinnerClubSlug(String winnerClubSlug) { this.winnerClubSlug = winnerClubSlug; }

    public String getWinnerLeagueSlug() { return winnerLeagueSlug; }
    public void setWinnerLeagueSlug(String winnerLeagueSlug) { this.winnerLeagueSlug = winnerLeagueSlug; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRoundOrder() {
        return roundOrder;
    }

    public void setRoundOrder(Integer roundOrder) {
        this.roundOrder = roundOrder;
    }

    public Long getSourceMatchOneId() {
        return sourceMatchOneId;
    }

    public void setSourceMatchOneId(Long sourceMatchOneId) {
        this.sourceMatchOneId = sourceMatchOneId;
    }

    public Long getSourceMatchTwoId() {
        return sourceMatchTwoId;
    }

    public void setSourceMatchTwoId(Long sourceMatchTwoId) {
        this.sourceMatchTwoId = sourceMatchTwoId;
    }

    public Integer getHomeSeed() {
        return homeSeed;
    }

    public void setHomeSeed(Integer homeSeed) {
        this.homeSeed = homeSeed;
    }

    public Integer getAwaySeed() {
        return awaySeed;
    }

    public void setAwaySeed(Integer awaySeed) {
        this.awaySeed = awaySeed;
    }

    public Integer getAggregateHomeGoals() {
        return aggregateHomeGoals;
    }

    public void setAggregateHomeGoals(Integer aggregateHomeGoals) {
        this.aggregateHomeGoals = aggregateHomeGoals;
    }

    public Integer getAggregateAwayGoals() {
        return aggregateAwayGoals;
    }

    public void setAggregateAwayGoals(Integer aggregateAwayGoals) {
        this.aggregateAwayGoals = aggregateAwayGoals;
    }

    public Integer getFirstLegHomeGoals() {
        return firstLegHomeGoals;
    }

    public void setFirstLegHomeGoals(Integer firstLegHomeGoals) {
        this.firstLegHomeGoals = firstLegHomeGoals;
    }

    public Integer getFirstLegAwayGoals() {
        return firstLegAwayGoals;
    }

    public void setFirstLegAwayGoals(Integer firstLegAwayGoals) {
        this.firstLegAwayGoals = firstLegAwayGoals;
    }

    public Integer getSecondLegHomeGoals() {
        return secondLegHomeGoals;
    }

    public void setSecondLegHomeGoals(Integer secondLegHomeGoals) {
        this.secondLegHomeGoals = secondLegHomeGoals;
    }

    public Integer getSecondLegAwayGoals() {
        return secondLegAwayGoals;
    }

    public void setSecondLegAwayGoals(Integer secondLegAwayGoals) {
        this.secondLegAwayGoals = secondLegAwayGoals;
    }

    public LocalDateTime getFirstLegDate() { return firstLegDate; }
    public void setFirstLegDate(LocalDateTime firstLegDate) { this.firstLegDate = firstLegDate; }

    public LocalDateTime getSecondLegDate() { return secondLegDate; }
    public void setSecondLegDate(LocalDateTime secondLegDate) { this.secondLegDate = secondLegDate; }

    public String getFirstLegReferee() { return firstLegReferee; }
    public void setFirstLegReferee(String firstLegReferee) { this.firstLegReferee = firstLegReferee; }

    public String getSecondLegReferee() { return secondLegReferee; }
    public void setSecondLegReferee(String secondLegReferee) { this.secondLegReferee = secondLegReferee; }
}