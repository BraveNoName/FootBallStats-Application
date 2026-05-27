package com.footballstats.model;

import jakarta.persistence.*;

@Entity
@Table(name = "MATCH_EVENT")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long matchId;
    private Integer eventMinute;
    private String eventType;
    private Long teamId;
    private Long playerId;
    private String playerName;
    private Long relatedPlayerId;
    private String relatedPlayerName;
    private String description;
    private String iconUrl;
    private String teamSide;

    public Long getId() { return id; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Integer getEventMinute() { return eventMinute; }
    public void setEventMinute(Integer eventMinute) { this.eventMinute = eventMinute; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Long getRelatedPlayerId() { return relatedPlayerId; }
    public void setRelatedPlayerId(Long relatedPlayerId) { this.relatedPlayerId = relatedPlayerId; }

    public String getRelatedPlayerName() { return relatedPlayerName; }
    public void setRelatedPlayerName(String relatedPlayerName) { this.relatedPlayerName = relatedPlayerName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getTeamSide() {return teamSide;}
    public void setTeamSide(String teamSide) {this.teamSide = teamSide;}

}