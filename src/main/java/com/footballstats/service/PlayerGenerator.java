package com.footballstats.service;

import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.RealTeam;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class PlayerGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();
    private final List<String> feet = List.of("Right", "Left", "Both");

    public List<GeneratedPlayer> generateSquadForTeam(RealTeam team) {
        int squadSize = random.nextInt(20, 34);
        List<String> positions = buildPositionList(squadSize);
        Collections.shuffle(positions);

        List<GeneratedPlayer> squad = new ArrayList<>();

        for (int i = 0; i < squadSize; i++) {
            squad.add(generatePlayerForTeam(team, positions.get(i), i + 1));
        }

        return squad;
    }

    private GeneratedPlayer generatePlayerForTeam(RealTeam team, String position, int shirtNumber) {
        int age = randomAge();

        String rawCountryCode = randomCountryCodeForLeague(team.getLeagueSlug());
        CountryData country = normalizeCountry(nationalityFromCode(rawCountryCode));

        GeneratedPlayer player = new GeneratedPlayer();

        player.setFirstName(faker.name().firstName());
        player.setLastName(faker.name().lastName());

        player.setBirthDate(LocalDate.now().minusYears(age).minusDays(random.nextInt(365)));
        player.setAge(age);

        player.setHeight(randomHeightByPosition(position));
        player.setNationality(country.name());
        player.setCountryCode(country.code());

        player.setPosition(position);
        player.setPreferredFoot(feet.get(random.nextInt(feet.size())));
        player.setShirtNumber(shirtNumber);

        player.setCurrentClub(team.getApiTeamName());
        player.setClubSlug(team.getClubSlug());
        player.setLeagueSlug(team.getLeagueSlug());
        player.setTeamId(team.getApiTeamId());

        player.setJoinedClubDate(LocalDate.now().minusYears(random.nextInt(1, 7)));
        player.setContractUntil(LocalDate.now().plusYears(random.nextInt(1, 6)));
        player.setLastContractExtension(LocalDate.now().minusMonths(random.nextInt(1, 24)));

        player.setMarketValue(
                randomMarketValue(age, position, team.getLeagueSlug())
        );

        player.setActive(true);
        player.setImageUrl("https://via.placeholder.com/80");

        return player;
    }

    private List<String> buildPositionList(int squadSize) {
        List<String> result = new ArrayList<>();

        int goalkeepers = squadSize >= 25 ? 3 : 2;
        int defenders = Math.max(6, squadSize / 3);
        int midfielders = Math.max(6, squadSize / 3);
        int forwards = squadSize - goalkeepers - defenders - midfielders;

        for (int i = 0; i < goalkeepers; i++) result.add("Goalkeeper");
        for (int i = 0; i < defenders; i++) result.add("Defender");
        for (int i = 0; i < midfielders; i++) result.add("Midfielder");
        for (int i = 0; i < forwards; i++) result.add("Forward");

        return result;
    }

    private int randomAge() {
        int chance = random.nextInt(100);

        if (chance < 45) return random.nextInt(18, 24);
        if (chance < 80) return random.nextInt(24, 29);
        if (chance < 95) return random.nextInt(29, 34);

        return random.nextInt(34, 38);
    }

    private int randomHeightByPosition(String position) {
        return switch (position) {
            case "Goalkeeper" -> random.nextInt(188, 201);
            case "Defender" -> random.nextInt(180, 194);
            case "Midfielder" -> random.nextInt(170, 186);
            case "Forward" -> random.nextInt(173, 190);
            default -> random.nextInt(170, 195);
        };
    }

    private BigDecimal randomMarketValue(int age, String position, String leagueSlug) {
        long value;

        int chance = random.nextInt(1000);
        int leagueTier = leagueTier(leagueSlug);

        if (leagueTier == 1 && chance < 35) {
            value = random.nextLong(100_000_000L, 220_000_001L);
        }
        else if (leagueTier == 1 && chance < 120) {
            value = random.nextLong(60_000_000L, 100_000_001L);
        }
        else if (leagueTier == 2 && chance < 12) {
            value = random.nextLong(80_000_000L, 150_000_001L);
        }
        else if (leagueTier == 2 && chance < 55) {
            value = random.nextLong(35_000_000L, 80_000_001L);
        }
        else if (leagueTier == 3 && chance < 18) {
            value = random.nextLong(20_000_000L, 60_000_001L);
        }
        else {
            int minSteps, maxSteps;

            if (age <= 21) { minSteps = 1; maxSteps = 48; }
            else if (age <= 28) { minSteps = 4; maxSteps = 180; }
            else if (age <= 33) { minSteps = 2; maxSteps = 80; }
            else { minSteps = 1; maxSteps = 20; }

            int steps = random.nextInt(minSteps, maxSteps + 1);
            value = steps * 250_000L;
        }

        value = Math.round(value * leagueValueMultiplier(leagueSlug));

        if ("Goalkeeper".equals(position)) {
            value = Math.round(value * 0.75);
        }

        value = Math.round(value / 250_000.0) * 250_000L;

        return BigDecimal.valueOf(value);
    }

    private int leagueTier(String leagueSlug) {
        return switch (leagueSlug) {
            case "england", "spain", "italy", "germany", "france" -> 1;
            case "netherlands", "portugal", "belgium", "turkiye" -> 2;
            case "czech", "greece", "norway", "austria", "scotland",
                 "denmark", "switzerland" -> 3;
            default -> 4;
        };
    }

    private double leagueValueMultiplier(String leagueSlug) {
        return switch (leagueSlug) {
            case "england" -> 1.45;
            case "italy" -> 1.35;
            case "spain" -> 1.32;
            case "germany" -> 1.26;
            case "france" -> 1.16;

            case "netherlands" -> 1.00;
            case "portugal" -> 0.96;
            case "belgium" -> 0.86;
            case "turkiye" -> 0.80;

            case "czech" -> 0.74;
            case "greece" -> 0.72;
            case "norway" -> 0.69;
            case "austria" -> 0.67;
            case "scotland" -> 0.65;
            case "denmark" -> 0.63;
            case "switzerland" -> 0.61;

            case "poland" -> 0.58;
            case "israel" -> 0.55;
            case "sweden" -> 0.52;
            case "croatia" -> 0.50;
            case "serbia" -> 0.48;
            case "ukraine" -> 0.46;
            case "romania" -> 0.44;
            case "bulgaria" -> 0.40;
            case "russia" -> 0.35;

            default -> 0.55;
        };
    }

    private String randomCountryCodeForLeague(String leagueSlug) {
        List<String> globalPool = List.of(
                "br", "ar", "fr", "es", "de", "it", "pt", "nl", "be", "hr", "rs", "dk", "se", "no", "pl", "cz",
                "sk", "ua", "tr", "us", "mx", "co", "uy", "cl", "ng", "gh", "sn", "ci", "ma", "dz", "cm", "jp",
                "kr", "ch", "at", "ie", "gb-eng", "gb-sct", "gb-wls"
        );

        List<String> leagueFlavor = switch (leagueSlug) {
            case "england" -> List.of("gb-eng", "ie", "gb-sct", "gb-wls", "fr", "br", "pt", "ng");
            case "spain" -> List.of("es", "ar", "br", "uy", "co", "fr", "ma");
            case "italy" -> List.of("it", "ar", "br", "fr", "rs", "hr", "al");
            case "germany" -> List.of("de", "at", "ch", "tr", "fr", "nl", "pl", "hr");
            case "france" -> List.of("fr", "sn", "ci", "ma", "dz", "cm", "be");
            case "poland" -> List.of("pl", "ua", "cz", "sk", "hr", "rs", "br");
            case "portugal" -> List.of("pt", "br", "ao", "cv", "ar", "es", "fr");
            case "netherlands" -> List.of("nl", "be", "sr", "de", "fr", "br");
            case "belgium" -> List.of("be", "fr", "nl", "ma", "cd", "sn");
            case "scotland" -> List.of("gb-sct", "gb-eng", "ie", "gb-wls", "fr");
            case "turkiye" -> List.of("tr", "de", "br", "fr", "nl", "rs");
            default -> List.of("br", "ar", "fr", "es", "de", "it", "pt", "nl");
        };

        int chance = random.nextInt(100);

        if (chance < 35) {
            return leagueFlavor.get(random.nextInt(leagueFlavor.size()));
        }

        return globalPool.get(random.nextInt(globalPool.size()));
    }

    private String nationalityFromCode(String code) {
        return switch (code) {
            case "gb-eng" -> "England";
            case "gb-sct" -> "Scotland";
            case "gb-wls" -> "Wales";
            case "ie" -> "Ireland";
            case "es" -> "Spain";
            case "fr" -> "France";
            case "de" -> "Germany";
            case "it" -> "Italy";
            case "pt" -> "Portugal";
            case "nl" -> "Netherlands";
            case "br" -> "Brazil";
            case "ar" -> "Argentina";
            case "pl" -> "Poland";
            case "be" -> "Belgium";
            case "hr" -> "Croatia";
            case "rs" -> "Serbia";
            case "dk" -> "Denmark";
            case "se" -> "Sweden";
            case "no" -> "Norway";
            case "ng" -> "Nigeria";
            case "gh" -> "Ghana";
            case "ma" -> "Morocco";
            case "ua" -> "Ukraine";
            case "cz" -> "Czech Republic";
            case "sk" -> "Slovakia";
            case "at" -> "Austria";
            case "ch" -> "Switzerland";
            case "ci" -> "Ivory Coast";
            case "sn" -> "Senegal";
            case "ao" -> "Angola";
            case "cv" -> "Cape Verde";
            case "sr" -> "Suriname";
            case "tr" -> "Turkey";
            case "us" -> "USA";
            case "mx" -> "Mexico";
            case "co" -> "Colombia";
            case "uy" -> "Uruguay";
            case "cl" -> "Chile";
            case "dz" -> "Algeria";
            case "cm" -> "Cameroon";
            case "jp" -> "Japan";
            case "kr" -> "South Korea";
            case "al" -> "Albania";
            case "cd" -> "DR Congo";
            default -> "Unknown";
        };
    }

    private CountryData normalizeCountry(String countryName) {
        if (countryName == null) return new CountryData("Unknown", "un");

        return switch (countryName) {
            case "Antigua and Barbuda" -> new CountryData("Antigua", "ag");
            case "Bolivia (Plurinational State of)", "Bolivia, Plurinational State of" -> new CountryData("Bolivia", "bo");
            case "Bosnia and Herzegovina" -> new CountryData("Bosnia", "ba");
            case "Brunei Darussalam" -> new CountryData("Brunei", "bn");
            case "Cabo Verde" -> new CountryData("Cape Verde", "cv");
            case "Central African Republic" -> new CountryData("Central Africa", "cf");
            case "Congo, Democratic Republic of the", "Democratic Republic of the Congo" -> new CountryData("DR Congo", "cd");
            case "Congo", "Republic of the Congo" -> new CountryData("Congo", "cg");
            case "Côte d'Ivoire" -> new CountryData("Ivory Coast", "ci");
            case "Czechia" -> new CountryData("Czech Republic", "cz");
            case "Dominican Republic" -> new CountryData("Dominican Rep.", "do");
            case "Eswatini" -> new CountryData("Swaziland", "sz");
            case "Holy See" -> new CountryData("Vatican", "va");
            case "Iran (Islamic Republic of)", "Iran, Islamic Republic of" -> new CountryData("Iran", "ir");
            case "Korea (Democratic People's Republic of)", "Korea, Democratic People's Republic of" -> new CountryData("North Korea", "kp");
            case "Korea, Republic of" -> new CountryData("South Korea", "kr");
            case "Lao People's Democratic Republic" -> new CountryData("Laos", "la");
            case "Macedonia, The Former Yugoslav Republic of" -> new CountryData("North Macedonia", "mk");
            case "Micronesia (Federated States of)", "Micronesia, Federated States of" -> new CountryData("Micronesia", "fm");
            case "Moldova, Republic of" -> new CountryData("Moldova", "md");
            case "Myanmar" -> new CountryData("Myanmar", "mm");
            case "Palestine, State of" -> new CountryData("Palestine", "ps");
            case "Russian Federation" -> new CountryData("Russia", "ru");
            case "Saint Kitts and Nevis" -> new CountryData("Saint Kitts and Nevis", "kn");
            case "Saint Lucia" -> new CountryData("Saint Lucia", "lc");
            case "Saint Vincent and the Grenadines" -> new CountryData("Saint Vincent", "vc");
            case "Sao Tome and Principe", "São Tomé and Príncipe" -> new CountryData("Sao Tome and Principe", "st");
            case "Syrian Arab Republic" -> new CountryData("Syria", "sy");
            case "Taiwan, Province of China" -> new CountryData("Taiwan", "tw");
            case "Tanzania, United Republic of" -> new CountryData("Tanzania", "tz");
            case "Timor-Leste" -> new CountryData("East Timor", "tl");
            case "Trinidad and Tobago" -> new CountryData("Trinidad and Tobago", "tt");
            case "Turkey", "Türkiye" -> new CountryData("Turkey", "tr");
            case "United Arab Emirates" -> new CountryData("UAE", "ae");
            case "United Kingdom of Great Britain and Northern Ireland" -> new CountryData("United Kingdom", "gb");
            case "United States of America" -> new CountryData("USA", "us");
            case "Venezuela (Bolivarian Republic of)", "Venezuela, Bolivarian Republic of" -> new CountryData("Venezuela", "ve");
            case "Viet Nam" -> new CountryData("Vietnam", "vn");
            default -> new CountryData(countryName, findIsoCountryCode(countryName));
        };
    }

    private String findIsoCountryCode(String countryName) {
        for (String iso : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso);

            if (locale.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(countryName)) {
                return iso.toLowerCase();
            }
        }

        return "un";
    }

    private record CountryData(String name, String code) {
    }
}