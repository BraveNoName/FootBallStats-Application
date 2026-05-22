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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class LeagueClubController {

    private final ApiFootballService apiFootballService;

    public LeagueClubController(ApiFootballService apiFootballService) {
        this.apiFootballService = apiFootballService;
    }

    private final Map<String, LeagueView> leagues = new LinkedHashMap<>() {{
        put("austria", new LeagueView(
                "austria",
                "Austrian Bundesliga",
                "Austria - Bundesliga",
                "/Images/leagues/Austria - Bundesliga/austriabundesliga.png",
                218
        ));

        put("belgium", new LeagueView(
                "belgium",
                "Jupiler Pro League",
                "Belgium - Jupiler Pro League",
                "/Images/leagues/Belgium - Jupiler Pro League/proleague.png",
                144
        ));

        put("bulgaria", new LeagueView(
                "bulgaria",
                "Efbet Liga",
                "Bulgaria - efbet Liga",
                "/Images/leagues/Bulgaria - efbet Liga/efbetliga.png",
                172
        ));

        put("croatia", new LeagueView(
                "croatia",
                "SuperSport HNL",
                "Croatia - SuperSport HNL",
                "/Images/leagues/Croatia - SuperSport HNL/supersporthnl.png",
                210
        ));

        put("czech", new LeagueView(
                "czecha",
                "Chance Liga",
                "Czech Republic - Chance Liga",
                "/Images/leagues/Czech Republic - Chance Liga/chanceliga.png",
                345
        ));

        put("denmark", new LeagueView(
                "denmark",
                "Superliga",
                "Denmark - Superliga",
                "/Images/leagues/Denmark - Superliga/superliga.png",
                119
        ));

        put("england", new LeagueView(
                "england",
                "Premier League",
                "England - Premier League",
                "/Images/leagues/England - Premier League/premierleague.png",
                39
        ));

        put("france", new LeagueView(
                "france",
                "Ligue 1",
                "France - Ligue 1",
                "/Images/leagues/France - Ligue 1/ligue1.png",
                61
        ));

        put("germany", new LeagueView(
                "germany",
                "Bundesliga",
                "Germany - Bundesliga",
                "/Images/leagues/Germany - Bundesliga/bundesliga.png",
                78
        ));

        put("greece", new LeagueView(
                "greece",
                "Super League 1",
                "Greece - Super League 1",
                "/Images/leagues/Greece - Super League 1/superleague1.png",
                197
        ));

        put("italy", new LeagueView(
                "italy",
                "Serie A",
                "Italy - Serie A",
                "/Images/leagues/Italy - Serie A/seriea.png",
                135
        ));

        put("israel", new LeagueView(
                "israel",
                "Ligat ha'Al",
                "Israel - Ligat ha'Al",
                "/Images/leagues/Israel - Ligat ha'Al/ligathaai.png",
                325
        ));

        put("netherlands", new LeagueView(
                "netherlands",
                "Eredivisie",
                "Netherlands - Eredivisie",
                "/Images/leagues/Netherlands - Eredivisie/eredivisie.png",
                88
        ));

        put("norway", new LeagueView(
                "norway",
                "Eliteserien",
                "Norway - Eliteserien",
                "/Images/leagues/Norway - Eliteserien/eliteserien.png",
                103
        ));

        put("poland", new LeagueView(
                "poland",
                "PKO BP Ekstraklasa",
                "Poland - PKO BP Ekstraklasa",
                "/Images/leagues/Poland - PKO BP Ekstraklasa/pkobpekstraklasa.png",
                106
        ));

        put("portugal", new LeagueView(
                "portugal",
                "Liga Portugal",
                "Portugal - Liga Portugal",
                "/Images/leagues/Portugal - Liga Portugal/ligaportugal.png",
                94
        ));

        put("romania", new LeagueView(
                "romania",
                "SuperLiga",
                "Romania - SuperLiga",
                "/Images/leagues/Romania - SuperLiga/romaniasuperliga.png",
                283
        ));

        put("russia", new LeagueView(
                "russia",
                "Premier Liga",
                "Russia - Premier Liga",
                "/Images/leagues/Russia - Premier Liga/russiapremierliga.jpg",
                235
        ));

        put("scotland", new LeagueView(
                "scotland",
                "Scottish Premiership",
                "Scotland - Scottish Premiership",
                "/Images/leagues/Scotland - Scottish Premiership/scottishpremiership.png",
                179
        ));

        put("serbia", new LeagueView(
                "serbia",
                "Super liga Srbije",
                "Serbia - Super liga Srbije",
                "/Images/leagues/Serbia - Super liga Srbije/superligaserbije.png",
                286
        ));

        put("spain", new LeagueView(
                "spain",
                "LaLiga",
                "Spain - LaLiga",
                "/Images/leagues/Spain - LaLiga/laliga.png",
                140
        ));

        put("sweden", new LeagueView(
                "sweden",
                "Allsvenskan",
                "Sweden - Allsvenskan",
                "/Images/leagues/Sweden - Allsvenskan/allsvenskan.png",
                113
        ));

        put("switzerland", new LeagueView(
                "switzerland",
                "Super League",
                "Switzerland - Super League",
                "/Images/leagues/Switzerland - Super League/superleague.png",
                207
        ));

        put("turkiye", new LeagueView(
                "turkiye",
                "Süper Lig",
                "Türkiye - Süper Lig",
                "/Images/leagues/Türkiye - Süper Lig/superlig.png",
                203
        ));

        put("ukraine", new LeagueView(
                "ukraine",
                "Premier Liga",
                "Ukraine - Premier Liga",
                "/Images/leagues/Ukraine - Premier Liga/premierliga.png",
                333
        ));
    }};

    @GetMapping("/leagues/{leagueSlug}")
    public String leagueClubs(
            @PathVariable String leagueSlug,
            Model model
    ) {
        LeagueView league = leagues.get(leagueSlug);

        if (league == null) {
            throw new RuntimeException("League not found: " + leagueSlug);
        }

        model.addAttribute("leagueSlug", league.slug());
        model.addAttribute("leagueName", league.name());
        model.addAttribute("leagueLogo", league.logo());
        model.addAttribute("clubs", loadClubsFromFolder(league.folder()));

        return "league-clubs";
    }

    @GetMapping("/clubs/{leagueSlug}/{clubSlug}")
    public String clubDetails(
            @PathVariable String leagueSlug,
            @PathVariable String clubSlug,
            Model model
    ) {
        LeagueView league = leagues.get(leagueSlug);

        if (league == null) {
            throw new RuntimeException("League not found: " + leagueSlug);
        }

        ClubView club = loadClubsFromFolder(league.folder())
                .stream()
                .filter(c -> c.slug().equals(clubSlug))
                .findFirst()
                .orElseThrow();

        Map<String, Object> teamData =
                apiFootballService.getTeamFromLeague(club.name(), league.apiLeagueId());

        if (teamData == null) {
            throw new RuntimeException("Team not found in API-Football: " + club.name());
        }

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
        model.addAttribute("leagueSlug", league.slug());
        model.addAttribute("leagueName", league.name());
        model.addAttribute("teamData", teamData);
        model.addAttribute("players", players);

        return "club-details";
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

                        return !file.contains("league")
                                && !file.contains("liga")
                                && !file.contains("bundesliga")
                                && !file.contains("seriea")
                                && !file.contains("ligue1")
                                && !file.contains("eredivisie")
                                && !file.contains("primeiraliga")
                                && !file.contains("superlig")
                                && !file.contains("premier");
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

    public record LeagueView(
            String slug,
            String name,
            String folder,
            String logo,
            Integer apiLeagueId
    ) {
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