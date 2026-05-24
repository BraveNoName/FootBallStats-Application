package com.footballstats.model;

import jakarta.persistence.*;

@Entity
public class RealTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String leagueSlug;
    private String clubSlug;

    private Integer apiTeamId;
    private String apiTeamName;
    private String apiLogo;

    public Long getId() { return id; }

    public String getLeagueSlug() { return leagueSlug; }
    public void setLeagueSlug(String leagueSlug) { this.leagueSlug = leagueSlug; }

    public String getClubSlug() { return clubSlug; }
    public void setClubSlug(String clubSlug) { this.clubSlug = clubSlug; }

    public Integer getApiTeamId() { return apiTeamId; }
    public void setApiTeamId(Integer apiTeamId) { this.apiTeamId = apiTeamId; }

    public String getApiTeamName() { return apiTeamName; }
    public void setApiTeamName(String apiTeamName) { this.apiTeamName = apiTeamName; }

    public String getApiLogo() { return apiLogo; }
    public void setApiLogo(String apiLogo) { this.apiLogo = apiLogo; }
}