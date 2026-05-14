package com.footballstats.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.math.RoundingMode;

public class Player {
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
    private LocalDate joinedClubDate;
    private LocalDate contractUntil;
    private LocalDate lastContractExtension;
    private BigDecimal marketValue;
    private boolean active;
    private String imageUrl;

    public Player() {}

    public Player(Long id, String firstName, String lastName, LocalDate birthDate, int age, int height,
                  String nationality, String countryCode, String position, String preferredFoot,
                  int shirtNumber, String currentClub, LocalDate joinedClubDate,
                  LocalDate contractUntil, LocalDate lastContractExtension,
                  BigDecimal marketValue, boolean active, String imageUrl) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.age = age;
        this.height = height;
        this.nationality = nationality;
        this.countryCode = countryCode;
        this.position = position;
        this.preferredFoot = preferredFoot;
        this.shirtNumber = shirtNumber;
        this.currentClub = currentClub;
        this.joinedClubDate = joinedClubDate;
        this.contractUntil = contractUntil;
        this.lastContractExtension = lastContractExtension;
        this.marketValue = marketValue;
        this.active = active;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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


    public String getFormattedMarketValue() {
        if (marketValue == null) {
            return "-";
        }

        BigDecimal million = BigDecimal.valueOf(1_000_000);
        BigDecimal thousand = BigDecimal.valueOf(1_000);

        if (marketValue.compareTo(million) >= 0) {
            BigDecimal value = marketValue
                    .divide(million, 2, RoundingMode.HALF_UP);

            return value.toString().replace(".", ",") + " mln €";
        }

        BigDecimal value = marketValue
                .divide(thousand, 0, RoundingMode.HALF_UP);

        return value.toString().replace(".", ",") + " tys. €";
    }

}