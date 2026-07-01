package com.footballstats.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RealPlayer {

    @Id
    private Integer apiPlayerId;
    private Integer teamId;
    private String teamName;
    private String leagueSlug;
    private String clubSlug;
    @Column(name = "shirt_number")
    private Integer number;
    private String name;
    private String position;
    private Integer age;
    private String nationality;
    private String countryCode;
    private String photo;
    private Integer appearances;
    private Integer lineups;
    private Integer minutes;
    private String rating;
    private Boolean captain;
    private Integer goals;
    private Integer assists;
    private Integer goalsConceded;
    private Integer saves;
    private Integer shotsTotal;
    private Integer shotsOn;
    private Integer passesTotal;
    private Integer passesKey;
    private String passesAccuracy;
    private Integer tacklesTotal;
    private Integer tacklesBlocks;
    private Integer tacklesInterceptions;
    private Integer duelsTotal;
    private Integer duelsWon;
    private Integer dribblesAttempts;
    private Integer dribblesSuccess;
    private Integer dribblesPast;
    private Integer foulsDrawn;
    private Integer foulsCommitted;
    private Integer yellowCards;
    private Integer yellowRedCards;
    private Integer redCards;
    private Integer penaltyWon;
    private Integer penaltyCommitted;
    private Integer penaltyScored;
    private Integer penaltyMissed;
    private Integer penaltySaved;

    public RealPlayer() {
    }

    public Integer getApiPlayerId() { return apiPlayerId; }
    public void setApiPlayerId(Integer apiPlayerId) { this.apiPlayerId = apiPlayerId; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getLeagueSlug() { return leagueSlug; }
    public void setLeagueSlug(String leagueSlug) { this.leagueSlug = leagueSlug; }

    public String getClubSlug() { return clubSlug; }
    public void setClubSlug(String clubSlug) { this.clubSlug = clubSlug; }

    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Integer getAppearances() { return appearances; }
    public void setAppearances(Integer appearances) { this.appearances = appearances; }

    public Integer getLineups() { return lineups; }
    public void setLineups(Integer lineups) { this.lineups = lineups; }

    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public Boolean getCaptain() { return captain; }
    public void setCaptain(Boolean captain) { this.captain = captain; }

    public Integer getGoals() { return goals; }
    public void setGoals(Integer goals) { this.goals = goals; }

    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }

    public Integer getGoalsConceded() { return goalsConceded; }
    public void setGoalsConceded(Integer goalsConceded) { this.goalsConceded = goalsConceded; }

    public Integer getSaves() { return saves; }
    public void setSaves(Integer saves) { this.saves = saves; }

    public Integer getShotsTotal() { return shotsTotal; }
    public void setShotsTotal(Integer shotsTotal) { this.shotsTotal = shotsTotal; }

    public Integer getShotsOn() { return shotsOn; }
    public void setShotsOn(Integer shotsOn) { this.shotsOn = shotsOn; }

    public Integer getPassesTotal() { return passesTotal; }
    public void setPassesTotal(Integer passesTotal) { this.passesTotal = passesTotal; }

    public Integer getPassesKey() { return passesKey; }
    public void setPassesKey(Integer passesKey) { this.passesKey = passesKey; }

    public String getPassesAccuracy() { return passesAccuracy; }
    public void setPassesAccuracy(String passesAccuracy) { this.passesAccuracy = passesAccuracy; }

    public Integer getTacklesTotal() { return tacklesTotal; }
    public void setTacklesTotal(Integer tacklesTotal) { this.tacklesTotal = tacklesTotal; }

    public Integer getTacklesBlocks() { return tacklesBlocks; }
    public void setTacklesBlocks(Integer tacklesBlocks) { this.tacklesBlocks = tacklesBlocks; }

    public Integer getTacklesInterceptions() { return tacklesInterceptions; }
    public void setTacklesInterceptions(Integer tacklesInterceptions) { this.tacklesInterceptions = tacklesInterceptions; }

    public Integer getDuelsTotal() { return duelsTotal; }
    public void setDuelsTotal(Integer duelsTotal) { this.duelsTotal = duelsTotal; }

    public Integer getDuelsWon() { return duelsWon; }
    public void setDuelsWon(Integer duelsWon) { this.duelsWon = duelsWon; }

    public Integer getDribblesAttempts() { return dribblesAttempts; }
    public void setDribblesAttempts(Integer dribblesAttempts) { this.dribblesAttempts = dribblesAttempts; }

    public Integer getDribblesSuccess() { return dribblesSuccess; }
    public void setDribblesSuccess(Integer dribblesSuccess) { this.dribblesSuccess = dribblesSuccess; }

    public Integer getDribblesPast() { return dribblesPast; }
    public void setDribblesPast(Integer dribblesPast) { this.dribblesPast = dribblesPast; }

    public Integer getFoulsDrawn() { return foulsDrawn; }
    public void setFoulsDrawn(Integer foulsDrawn) { this.foulsDrawn = foulsDrawn; }

    public Integer getFoulsCommitted() { return foulsCommitted; }
    public void setFoulsCommitted(Integer foulsCommitted) { this.foulsCommitted = foulsCommitted; }

    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }

    public Integer getYellowRedCards() { return yellowRedCards; }
    public void setYellowRedCards(Integer yellowRedCards) { this.yellowRedCards = yellowRedCards; }

    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }

    public Integer getPenaltyWon() { return penaltyWon; }
    public void setPenaltyWon(Integer penaltyWon) { this.penaltyWon = penaltyWon; }

    public Integer getPenaltyCommitted() { return penaltyCommitted; }
    public void setPenaltyCommitted(Integer penaltyCommitted) { this.penaltyCommitted = penaltyCommitted; }

    public Integer getPenaltyScored() { return penaltyScored; }
    public void setPenaltyScored(Integer penaltyScored) { this.penaltyScored = penaltyScored; }

    public Integer getPenaltyMissed() { return penaltyMissed; }
    public void setPenaltyMissed(Integer penaltyMissed) { this.penaltyMissed = penaltyMissed; }

    public Integer getPenaltySaved() { return penaltySaved; }
    public void setPenaltySaved(Integer penaltySaved) { this.penaltySaved = penaltySaved; }
}