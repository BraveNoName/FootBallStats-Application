package com.footballstats.service;

import com.footballstats.model.Player;
import net.datafaker.Faker;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.LongStream;

public class PlayerGenerator {
    private final Faker faker = new Faker();
    private final Random random = new Random();

    private final List<String> positions = List.of("Goalkeeper", "Defender", "Midfielder", "Forward");
    private final List<String> feet = List.of("Right", "Left", "Both");
    private final List<String> clubs = List.of("FC Barcelona", "Real Madrid", "Manchester City", "Bayern Munich", "Arsenal");

    public List<Player> generatePlayers(int count) {
        return LongStream.rangeClosed(1, count).mapToObj(this::generatePlayer).toList();
    }

    private Player generatePlayer(Long id) {
        int age = random.nextInt(18, 41);
        LocalDate birthDate = LocalDate.now().minusYears(age);
        String originalNationality = faker.country().name();
        CountryData country = countryData(originalNationality);

        return new Player(
                id, faker.name().firstName(), faker.name().lastName(), birthDate, age,
                random.nextInt(165, 205), country.name(), country.code(),
                positions.get(random.nextInt(positions.size())), feet.get(random.nextInt(feet.size())),
                random.nextInt(1, 100), clubs.get(random.nextInt(clubs.size())),
                LocalDate.now().minusYears(random.nextInt(1, 8)), LocalDate.now().plusYears(random.nextInt(1, 6)),
                LocalDate.now().minusMonths(random.nextInt(1, 24)), randomMarketValue(),
                true, "https://via.placeholder.com/80"
        );
    }

    private BigDecimal randomMarketValue() {
        double value;
        if (random.nextInt(100) < 70) value = 250_000 + (random.nextInt(40) * 250_000);
        else value = 10_000_000 + (random.nextInt(40) * 2_500_000);
        return BigDecimal.valueOf(value);
    }

    private CountryData countryData(String countryName) {
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
            case "Turkey", "Türkiye" -> new CountryData("Turkey", "tr");            case "United Arab Emirates" -> new CountryData("UAE", "ae");
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
            if (locale.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(countryName)) return iso.toLowerCase();
        }
        return "un";
    }

    private record CountryData(String name, String code) {}

}