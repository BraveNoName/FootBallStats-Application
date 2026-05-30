package com.footballstats.model;

import jakarta.persistence.*;

@Entity
@Table(name = "EUROPEAN_MATCH_EVENT")
public class EuropeanMatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId;
    private Integer eventMinute;
    private String eventType;

    private Long playerId;
    private String playerName;

    private Long relatedPlayerId;
    private String relatedPlayerName;


    private String description;
    private String iconUrl;
    private String teamSide;

    private Long knockoutMatchId;
    private String knockoutLeg;

    public Long getId() {
        return id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Integer getEventMinute() {
        return eventMinute;
    }

    public void setEventMinute(Integer eventMinute) {
        this.eventMinute = eventMinute;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Long getRelatedPlayerId() {
        return relatedPlayerId;
    }

    public void setRelatedPlayerId(Long relatedPlayerId) {
        this.relatedPlayerId = relatedPlayerId;
    }

    public String getRelatedPlayerName() {
        return relatedPlayerName;
    }

    public void setRelatedPlayerName(String relatedPlayerName) {
        this.relatedPlayerName = relatedPlayerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getTeamSide() {
        return teamSide;
    }

    public void setTeamSide(String teamSide) {
        this.teamSide = teamSide;
    }

    public Long getKnockoutMatchId() {
        return knockoutMatchId;
    }

    public void setKnockoutMatchId(Long knockoutMatchId) {
        this.knockoutMatchId = knockoutMatchId;
    }

    public String getKnockoutLeg() {
        return knockoutLeg;
    }

    public void setKnockoutLeg(String knockoutLeg) {
        this.knockoutLeg = knockoutLeg;
    }
}