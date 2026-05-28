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

    private int homeGoals;
    private int awayGoals;

    private int roundNumber;

    private LocalDateTime matchDate;

    private String status;

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

    public int getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(int homeGoals) {
        this.homeGoals = homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(int awayGoals) {
        this.awayGoals = awayGoals;
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
}