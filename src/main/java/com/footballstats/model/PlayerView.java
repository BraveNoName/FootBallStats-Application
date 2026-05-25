package com.footballstats.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PlayerView {

    private String currentClub;
    private String clubSlug;
    private String leagueSlug;
    private Integer shirtNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Integer age;
    private BigDecimal marketValue;
    private String nationality;
    private String countryCode;
    private String position;
    private String preferredFoot;
    private Integer height;
    private LocalDate joinedClubDate;
    private LocalDate contractUntil;
    private LocalDate lastContractExtension;
    private boolean active;
    private String imageUrl;
    private Integer teamId;

    public static PlayerView fromGenerated(GeneratedPlayer p) {
        PlayerView v = new PlayerView();
        v.currentClub = p.getCurrentClub();
        v.clubSlug = p.getClubSlug();
        v.leagueSlug = p.getLeagueSlug();
        v.shirtNumber = p.getShirtNumber();
        v.firstName = p.getFirstName();
        v.lastName = p.getLastName();
        v.birthDate = p.getBirthDate();
        v.age = p.getAge();
        v.marketValue = p.getMarketValue();
        v.nationality = p.getNationality();
        v.countryCode = p.getCountryCode();
        v.position = p.getPosition();
        v.preferredFoot = p.getPreferredFoot();
        v.height = p.getHeight();
        v.joinedClubDate = p.getJoinedClubDate();
        v.contractUntil = p.getContractUntil();
        v.lastContractExtension = p.getLastContractExtension();
        v.active = p.isActive();
        v.imageUrl = p.getImageUrl();
        v.teamId = p.getTeamId();
        return v;
    }

    public static PlayerView fromReal(RealPlayer p) {
        PlayerView v = new PlayerView();
        v.currentClub = p.getTeamName();
        v.clubSlug = p.getClubSlug();
        v.leagueSlug = p.getLeagueSlug();
        v.shirtNumber = p.getNumber();
        v.firstName = p.getName();
        v.lastName = "";
        v.age = p.getAge();
        v.marketValue = null;
        v.nationality = p.getNationality();
        v.countryCode = p.getCountryCode();
        v.position = p.getPosition();
        v.preferredFoot = "-";
        v.height = null;
        v.active = true;
        v.imageUrl = p.getPhoto();
        v.teamId = p.getTeamId();
        return v;
    }

    public String getFormattedMarketValue() {
        if (marketValue == null) return "-";

        BigDecimal million = BigDecimal.valueOf(1_000_000);
        BigDecimal thousand = BigDecimal.valueOf(1_000);

        if (marketValue.compareTo(million) >= 0) {
            return marketValue.divide(million, 2, RoundingMode.HALF_UP)
                    .toString().replace(".", ",") + " mln €";
        }

        return marketValue.divide(thousand, 0, RoundingMode.HALF_UP)
                .toString().replace(".", ",") + " tys. €";
    }

    public String getFullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    public String getCurrentClub() { return currentClub; }
    public String getClubSlug() { return clubSlug; }
    public String getLeagueSlug() { return leagueSlug; }
    public Integer getShirtNumber() { return shirtNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public Integer getAge() { return age; }
    public BigDecimal getMarketValue() { return marketValue; }
    public String getNationality() { return nationality; }
    public String getCountryCode() { return countryCode; }
    public String getPosition() { return position; }
    public String getPreferredFoot() { return preferredFoot; }
    public Integer getHeight() { return height; }
    public LocalDate getJoinedClubDate() { return joinedClubDate; }
    public LocalDate getContractUntil() { return contractUntil; }
    public LocalDate getLastContractExtension() { return lastContractExtension; }
    public boolean isActive() { return active; }
    public String getImageUrl() { return imageUrl; }

    public String getFormattedNationality() {
        if (nationality == null || nationality.isBlank()) return "-";

        String lower = nationality.toLowerCase();
        String[] parts = lower.split(" ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isBlank()) {
                result.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public String getFormattedLeagueSlug() {
        if (leagueSlug == null || leagueSlug.isBlank()) return "-";

        return switch (leagueSlug) {
            case "austria" -> "Austrian Bundesliga";
            case "belgium" -> "Jupiler Pro League";
            case "bulgaria" -> "First Professional League";
            case "croatia" -> "HNL";
            case "czech" -> "Czech First League";
            case "denmark" -> "Danish Superliga";
            case "england" -> "Premier League";
            case "france" -> "Ligue 1";
            case "germany" -> "Bundesliga";
            case "greece" -> "Super League Greece";
            case "italy" -> "Serie A";
            case "netherlands" -> "Eredivisie";
            case "norway" -> "Eliteserien";
            case "poland" -> "Ekstraklasa";
            case "portugal" -> "Primeira Liga";
            case "russia" -> "Russian Premier League";
            case "scotland" -> "Scottish Premiership";
            case "serbia" -> "Serbian SuperLiga";
            case "spain" -> "La Liga";
            case "sweden" -> "Allsvenskan";
            case "switzerland" -> "Swiss Super League";
            case "turkiye" -> "Süper Lig";
            case "ukraine" -> "Ukrainian Premier League";
            default -> leagueSlug.substring(0, 1).toUpperCase() + leagueSlug.substring(1);
        };
    }

    public Integer getTeamId() {
        return teamId;
    }

    public String getClubLogoUrl() {
        if (teamId == null) return "/Images/default-club.png";
        return "https://media.api-sports.io/football/teams/" + teamId + ".png";
    }

    public String getLeagueLogoUrl() {
        if (leagueSlug == null) return "/Images/default-league.png";

        return switch (leagueSlug) {
            case "austria" -> "/Images/leagues/Austria - Bundesliga/austriabundesliga.png";
            case "belgium" -> "/Images/leagues/Belgium - Jupiler Pro League/proleague.png";
            case "bulgaria" -> "/Images/leagues/Bulgaria - efbet Liga/efbetliga.png";
            case "croatia" -> "/Images/leagues/Croatia - SuperSport HNL/supersporthnl.png";
            case "czech" -> "/Images/leagues/Czech Republic - Chance Liga/chanceliga.png";
            case "denmark" -> "/Images/leagues/Denmark - Superliga/superliga.png";
            case "england" -> "/Images/leagues/England - Premier League/premierleague.png";
            case "france" -> "/Images/leagues/France - Ligue 1/ligue1.png";
            case "germany" -> "/Images/leagues/Germany - Bundesliga/bundesliga.png";
            case "greece" -> "/Images/leagues/Greece - Super League 1/superleague1.png";
            case "israel" -> "/Images/leagues/Israel - Ligat ha'Al/ligathaai.png";
            case "italy" -> "/Images/leagues/Italy - Serie A/seriea.png";
            case "netherlands" -> "/Images/leagues/Netherlands - Eredivisie/eredivisie.png";
            case "norway" -> "/Images/leagues/Norway - Eliteserien/eliteserien.png";
            case "poland" -> "/Images/leagues/Poland - PKO BP Ekstraklasa/pkobpekstraklasa.png";
            case "portugal" -> "/Images/leagues/Portugal - Liga Portugal/ligaportugal.png";
            case "romania" -> "/Images/leagues/Romania - SuperLiga/romaniasuperliga.png";
            case "russia" -> "/Images/leagues/Russia - Premier Liga/russiapremierliga.jpg";
            case "scotland" -> "/Images/leagues/Scotland - Scottish Premiership/scottishpremiership.png";
            case "serbia" -> "/Images/leagues/Serbia - Super liga Srbije/superligaserbije.png";
            case "spain" -> "/Images/leagues/Spain - LaLiga/laliga.png";
            case "sweden" -> "/Images/leagues/Sweden - Allsvenskan/allsvenskan.png";
            case "switzerland" -> "/Images/leagues/Switzerland - Super League/superleague.png";
            case "turkiye" -> "/Images/leagues/Türkiye - Süper Lig/superlig.png";
            case "ukraine" -> "/Images/leagues/Ukraine - Premier Liga/premierliga.png";
            default -> "/Images/default-league.png";
        };
    }
}