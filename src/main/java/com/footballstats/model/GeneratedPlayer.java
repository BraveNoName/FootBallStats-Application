package com.footballstats.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
public class GeneratedPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private int age;
    private int height;
    private String nationality;
    private String countryCode;
    private String position;
    private String preferredFoot;
    private int shirtNumber;
    private String currentClub;
    private String clubSlug;
    private String leagueSlug;
    private Integer teamId;
    private LocalDate joinedClubDate;
    private LocalDate contractUntil;
    private LocalDate lastContractExtension;
    private BigDecimal marketValue;
    private boolean active;
    private String imageUrl;

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

    public Long getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPreferredFoot() { return preferredFoot; }
    public void setPreferredFoot(String preferredFoot) { this.preferredFoot = preferredFoot; }

    public int getShirtNumber() { return shirtNumber; }
    public void setShirtNumber(int shirtNumber) { this.shirtNumber = shirtNumber; }

    public String getCurrentClub() { return currentClub; }
    public void setCurrentClub(String currentClub) { this.currentClub = currentClub; }

    public String getClubSlug() { return clubSlug; }
    public void setClubSlug(String clubSlug) { this.clubSlug = clubSlug; }

    public String getLeagueSlug() { return leagueSlug; }
    public void setLeagueSlug(String leagueSlug) { this.leagueSlug = leagueSlug; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public LocalDate getJoinedClubDate() { return joinedClubDate; }
    public void setJoinedClubDate(LocalDate joinedClubDate) { this.joinedClubDate = joinedClubDate; }

    public LocalDate getContractUntil() { return contractUntil; }
    public void setContractUntil(LocalDate contractUntil) { this.contractUntil = contractUntil; }

    public LocalDate getLastContractExtension() { return lastContractExtension; }
    public void setLastContractExtension(LocalDate lastContractExtension) { this.lastContractExtension = lastContractExtension; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}