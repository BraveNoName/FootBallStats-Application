package com.footballstats.service.europe;

public class EuropeanTeam {

    private final String name;
    private final String clubSlug;
    private final String leagueSlug;
    private final String countryCode;
    private final int strength;

    public EuropeanTeam(String name, String clubSlug, String leagueSlug, String countryCode, int strength) {
        this.name = name;
        this.clubSlug = clubSlug;
        this.leagueSlug = leagueSlug;
        this.countryCode = countryCode;
        this.strength = strength;
    }

    public String getName() {
        return name;
    }

    public String getClubSlug() {
        return clubSlug;
    }

    public String getLeagueSlug() {
        return leagueSlug;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public int getStrength() {
        return strength;
    }
}