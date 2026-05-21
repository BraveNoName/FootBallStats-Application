package com.footballstats.controller;

import com.footballstats.service.ApiFootballService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class LeagueClubController {

    private final ApiFootballService apiFootballService;

    public LeagueClubController(ApiFootballService apiFootballService) {
        this.apiFootballService = apiFootballService;
    }

    @GetMapping("/leagues/england")
    public String premierLeague(Model model) {
        String leagueFolder = "England - Premier League";

        model.addAttribute("leagueName", "England - Premier League");
        model.addAttribute("leagueLogo", "/Images/leagues/England - Premier League/premierleague.png");
        model.addAttribute("clubs", loadClubsFromFolder(leagueFolder));

        return "league-clubs";
    }

    @GetMapping("/clubs/{clubSlug}")
    public String clubDetails(@PathVariable String clubSlug, Model model) {
        String leagueFolder = "England - Premier League";

        ClubView club = loadClubsFromFolder(leagueFolder)
                .stream()
                .filter(c -> c.slug().equals(clubSlug))
                .findFirst()
                .orElseThrow();

        Map<String, Object> teamData =
                apiFootballService.getTeamFromPremierLeague(club.name());

        Map<String, Object> team =
                (Map<String, Object>) teamData.get("team");

        Integer teamId =
                (Integer) team.get("id");

        Map<String, Object> squadData =
                apiFootballService.getSquad(teamId);

        List<Map<String, Object>> playersData =
                apiFootballService.getAllPlayersStats(teamId);

        List<PlayerRow> players =
                buildPlayers(squadData, playersData);


        model.addAttribute("club", club);
        model.addAttribute("leagueName", "Premier League");
        model.addAttribute("teamData", teamData);
        model.addAttribute("players", players);

        return "club-details";
    }

    private List<PlayerRow> buildPlayersFromSquadOnly(Map<String, Object> squadData) {

        List<Map<String, Object>> squadResponse =
                (List<Map<String, Object>>) squadData.get("response");

        if (squadResponse == null || squadResponse.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> squadPlayers =
                (List<Map<String, Object>>) squadResponse.get(0).get("players");

        return squadPlayers.stream()
                .map(player -> new PlayerRow(
                        player.get("number") != null ? (Integer) player.get("number") : null,
                        player.get("name") != null ? player.get("name").toString() : "-",
                        player.get("position") != null ? player.get("position").toString() : "-",
                        player.get("age") != null ? (Integer) player.get("age") : null,
                        "-",
                        "un",
                        player.get("photo") != null ? player.get("photo").toString() : ""
                ))
                .sorted(Comparator.comparing(
                        player -> player.number() == null ? 999 : player.number()
                ))
                .toList();
    }
    private List<ClubView> loadClubsFromFolder(String leagueFolder) {
        try {
            Path folderPath = Paths.get(
                    "src/main/resources/static/Images/leagues/" + leagueFolder
            );

            return Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String file = path.getFileName().toString().toLowerCase();

                        return file.endsWith(".png")
                                || file.endsWith(".jpg")
                                || file.endsWith(".jpeg");
                    })
                    .filter(path -> {
                        String file = path.getFileName().toString().toLowerCase();

                        return !file.contains("premierleague");
                    })
                    .map(path -> {
                        String fileName = path.getFileName().toString();

                        String name = fileName
                                .replace(".png", "")
                                .replace(".jpg", "")
                                .replace(".jpeg", "");

                        String slug = name.toLowerCase()
                                .replace("&", "and")
                                .replace("'", "")
                                .replace(".", "")
                                .replace(" ", "-");

                        String logo =
                                "/Images/leagues/" + leagueFolder + "/" + fileName;

                        return new ClubView(name, logo, slug);
                    })
                    .sorted(Comparator.comparing(ClubView::name))
                    .toList();

        } catch (IOException e) {
            return List.of();
        }
    }

    private List<PlayerRow> buildPlayers(
            Map<String, Object> squadData,
            List<Map<String, Object>> playersResponse
    ) {
        List<Map<String, Object>> squadResponse =
                (List<Map<String, Object>>) squadData.get("response");

        if (squadResponse == null || squadResponse.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> squadPlayers =
                (List<Map<String, Object>>) squadResponse.get(0).get("players");

        return squadPlayers.stream()
                .map(squadPlayer -> {

                    Integer playerId =
                            (Integer) squadPlayer.get("id");

                    Map<String, Object> fullPlayerData =
                            playersResponse.stream()
                                    .filter(item -> {
                                        Map<String, Object> player =
                                                (Map<String, Object>) item.get("player");

                                        return player.get("id").equals(playerId);
                                    })
                                    .findFirst()
                                    .orElse(null);

                    String name =
                            squadPlayer.get("name") != null
                                    ? squadPlayer.get("name").toString()
                                    : "-";

                    String position =
                            squadPlayer.get("position") != null
                                    ? squadPlayer.get("position").toString()
                                    : "-";

                    Integer number =
                            squadPlayer.get("number") != null
                                    ? (Integer) squadPlayer.get("number")
                                    : null;

                    String photo =
                            squadPlayer.get("photo") != null
                                    ? squadPlayer.get("photo").toString()
                                    : "";

                    Integer age =
                            squadPlayer.get("age") != null
                                    ? (Integer) squadPlayer.get("age")
                                    : null;

                    String nationality = "-";

                    if (fullPlayerData != null) {
                        Map<String, Object> player =
                                (Map<String, Object>) fullPlayerData.get("player");

                        if (player.get("age") != null) {
                            age = (Integer) player.get("age");
                        }

                        nationality =
                                player.get("nationality") != null
                                        ? player.get("nationality").toString()
                                        : "-";
                    }

                    return new PlayerRow(
                            number,
                            name,
                            position,
                            age,
                            nationality,
                            getCountryCodeForNationality(nationality),
                            photo
                    );
                })
                .sorted(Comparator.comparing(
                        player -> player.number() == null ? 999 : player.number()
                ))
                .toList();
    }

    private String getCountryCodeForNationality(String nationality) {
        if (nationality == null || nationality.isBlank() || nationality.equals("-")) {
            return "un";
        }

        return switch (nationality) {
            case "England" -> "gb";
            case "Scotland" -> "gb-sct";
            case "Wales" -> "gb-wls";
            case "Northern Ireland" -> "gb-nir";
            case "Turkey", "Türkiye" -> "tr";
            case "South Korea" -> "kr";
            case "North Korea" -> "kp";
            case "Ivory Coast", "Côte d'Ivoire" -> "ci";
            case "Czech Republic" -> "cz";
            case "DR Congo" -> "cd";
            case "Republic of Ireland", "Ireland" -> "ie";
            case "United States" -> "us";
            default -> findIsoCountryCode(nationality);
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

    public record ClubView(
            String name,
            String logo,
            String slug
    ) {
    }

    public record PlayerRow(
            Integer number,
            String name,
            String position,
            Integer age,
            String nationality,
            String countryCode,
            String photo
    ) {
    }
}