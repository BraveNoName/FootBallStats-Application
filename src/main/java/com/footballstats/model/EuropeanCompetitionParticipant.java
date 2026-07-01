package com.footballstats.model;

import jakarta.persistence.*;

@Entity
public class EuropeanCompetitionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String season;
    private String competitionSlug;
    private String clubName;
    private String clubSlug;
    private String leagueSlug;
    private String countryCode;
    private int potNumber;
    private boolean titleHolder;
    private int uefaRankingPoints;

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

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getClubSlug() {
        return clubSlug;
    }

    public void setClubSlug(String clubSlug) {
        this.clubSlug = clubSlug;
    }

    public String getLeagueSlug() {
        return leagueSlug;
    }

    public void setLeagueSlug(String leagueSlug) {
        this.leagueSlug = leagueSlug;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getPotNumber() {
        return potNumber;
    }

    public void setPotNumber(int potNumber) {
        this.potNumber = potNumber;
    }

    public boolean isTitleHolder() {
        return titleHolder;
    }

    public void setTitleHolder(boolean titleHolder) {
        this.titleHolder = titleHolder;
    }

    public int getUefaRankingPoints() {
        return uefaRankingPoints;
    }

    public void setUefaRankingPoints(int uefaRankingPoints) {
        this.uefaRankingPoints = uefaRankingPoints;
    }
}