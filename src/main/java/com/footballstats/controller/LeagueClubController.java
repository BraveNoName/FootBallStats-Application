package com.footballstats.controller;

import com.footballstats.model.RealPlayer;
import com.footballstats.model.RealTeam;
import com.footballstats.repository.RealPlayerRepository;
import com.footballstats.repository.RealTeamRepository;
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
    private final RealPlayerRepository realPlayerRepository;
    private final RealTeamRepository realTeamRepository;

    public LeagueClubController(ApiFootballService apiFootballService,
                                RealPlayerRepository realPlayerRepository,
                                RealTeamRepository realTeamRepository) {
        this.apiFootballService = apiFootballService;
        this.realPlayerRepository = realPlayerRepository;
        this.realTeamRepository = realTeamRepository;
    }

    private final Map<String, LeagueView> leagues = new LinkedHashMap<>() {{
        put("austria", new LeagueView("austria", "Austrian Bundesliga", "Austria - Bundesliga", "/Images/leagues/Austria - Bundesliga/austriabundesliga.png", 218));
        put("belgium", new LeagueView("belgium", "Jupiler Pro League", "Belgium - Jupiler Pro League", "/Images/leagues/Belgium - Jupiler Pro League/proleague.png", 144));
        put("bulgaria", new LeagueView("bulgaria", "Efbet Liga", "Bulgaria - efbet Liga", "/Images/leagues/Bulgaria - efbet Liga/efbetliga.png", 172));
        put("croatia", new LeagueView("croatia", "SuperSport HNL", "Croatia - SuperSport HNL", "/Images/leagues/Croatia - SuperSport HNL/supersporthnl.png", 210));
        put("czech", new LeagueView("czech", "Chance Liga", "Czech Republic - Chance Liga", "/Images/leagues/Czech Republic - Chance Liga/chanceliga.png", 345));
        put("denmark", new LeagueView("denmark", "Superliga", "Denmark - Superliga", "/Images/leagues/Denmark - Superliga/superliga.png", 119));
        put("england", new LeagueView("england", "Premier League", "England - Premier League", "/Images/leagues/England - Premier League/premierleague.png", 39));
        put("france", new LeagueView("france", "Ligue 1", "France - Ligue 1", "/Images/leagues/France - Ligue 1/ligue1.png", 61));
        put("germany", new LeagueView("germany", "Bundesliga", "Germany - Bundesliga", "/Images/leagues/Germany - Bundesliga/bundesliga.png", 78));
        put("greece", new LeagueView("greece", "Super League 1", "Greece - Super League 1", "/Images/leagues/Greece - Super League 1/superleague1.png", 197));
        put("italy", new LeagueView("italy", "Serie A", "Italy - Serie A", "/Images/leagues/Italy - Serie A/seriea.png", 135));
        put("israel", new LeagueView("israel", "Ligat ha'Al", "Israel - Ligat ha'Al", "/Images/leagues/Israel - Ligat ha'Al/ligathaai.png", 325));
        put("netherlands", new LeagueView("netherlands", "Eredivisie", "Netherlands - Eredivisie", "/Images/leagues/Netherlands - Eredivisie/eredivisie.png", 88));
        put("norway", new LeagueView("norway", "Eliteserien", "Norway - Eliteserien", "/Images/leagues/Norway - Eliteserien/eliteserien.png", 103));
        put("poland", new LeagueView("poland", "PKO BP Ekstraklasa", "Poland - PKO BP Ekstraklasa", "/Images/leagues/Poland - PKO BP Ekstraklasa/pkobpekstraklasa.png", 106));
        put("portugal", new LeagueView("portugal", "Liga Portugal", "Portugal - Liga Portugal", "/Images/leagues/Portugal - Liga Portugal/ligaportugal.png", 94));
        put("romania", new LeagueView("romania", "SuperLiga", "Romania - SuperLiga", "/Images/leagues/Romania - SuperLiga/romaniasuperliga.png", 283));
        put("russia", new LeagueView("russia", "Premier Liga", "Russia - Premier Liga", "/Images/leagues/Russia - Premier Liga/russiapremierliga.jpg", 235));
        put("scotland", new LeagueView("scotland", "Scottish Premiership", "Scotland - Scottish Premiership", "/Images/leagues/Scotland - Scottish Premiership/scottishpremiership.png", 179));
        put("serbia", new LeagueView("serbia", "Super liga Srbije", "Serbia - Super liga Srbije", "/Images/leagues/Serbia - Super liga Srbije/superligaserbije.png", 286));
        put("spain", new LeagueView("spain", "LaLiga", "Spain - LaLiga", "/Images/leagues/Spain - LaLiga/laliga.png", 140));
        put("sweden", new LeagueView("sweden", "Allsvenskan", "Sweden - Allsvenskan", "/Images/leagues/Sweden - Allsvenskan/allsvenskan.png", 113));
        put("switzerland", new LeagueView("switzerland", "Super League", "Switzerland - Super League", "/Images/leagues/Switzerland - Super League/superleague.png", 207));
        put("turkiye", new LeagueView("turkiye", "Süper Lig", "Türkiye - Süper Lig", "/Images/leagues/Türkiye - Süper Lig/superlig.png", 203));
        put("ukraine", new LeagueView("ukraine", "Premier Liga", "Ukraine - Premier Liga", "/Images/leagues/Ukraine - Premier Liga/premierliga.png", 333));
    }};

    @GetMapping("/leagues/{leagueSlug}")
    public String leagueClubs(@PathVariable String leagueSlug, Model model) {
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
    public String clubDetails(@PathVariable String leagueSlug,
                              @PathVariable String clubSlug,
                              Model model) {

        LeagueView league = leagues.get(leagueSlug);

        if (league == null) {
            throw new RuntimeException("League not found: " + leagueSlug);
        }

        ClubView club = loadClubsFromFolder(league.folder())
                .stream()
                .filter(c -> c.slug().equals(clubSlug))
                .findFirst()
                .orElseThrow();

        Map<String, Object> teamData = null;
        List<PlayerRow> players = List.of();

        try {
            Integer teamId = realTeamRepository.findByLeagueSlugAndClubSlug(leagueSlug, clubSlug)
                    .map(RealTeam::getApiTeamId)
                    .orElse(null);

            if (teamId == null) {
                teamData = apiFootballService.getTeamFromLeague(club.name(), league.apiLeagueId());

                if (teamData != null) {
                    Map<String, Object> team = (Map<String, Object>) teamData.get("team");
                    teamId = (Integer) team.get("id");
                }
            }

            if (teamId != null) {
                Map<String, Object> squadData = apiFootballService.getSquad(teamId);
                List<Map<String, Object>> playersData = apiFootballService.getAllPlayersStats(teamId);

                players = buildPlayers(squadData, playersData);

                if (!players.isEmpty()) {
                    savePlayersToDatabase(players, teamId, club.name(), leagueSlug, clubSlug);
                }
            }

        } catch (Exception e) {
            System.out.println("API problem, loading players from H2: " + e.getMessage());
        }

        if (players.isEmpty()) {
            players = loadPlayersFromDatabase(leagueSlug, clubSlug);
        }

        model.addAttribute("club", club);
        model.addAttribute("leagueSlug", league.slug());
        model.addAttribute("leagueName", league.name());
        model.addAttribute("teamData", teamData);
        model.addAttribute("players", players);

        return "club-details";
    }

    private void savePlayersToDatabase(List<PlayerRow> players,
                                       Integer teamId,
                                       String teamName,
                                       String leagueSlug,
                                       String clubSlug) {

        List<RealPlayer> realPlayers = players.stream()
                .map(player -> {
                    RealPlayer realPlayer = new RealPlayer();

                    realPlayer.setApiPlayerId(player.id());
                    realPlayer.setTeamId(teamId);
                    realPlayer.setTeamName(teamName);
                    realPlayer.setLeagueSlug(leagueSlug);
                    realPlayer.setClubSlug(clubSlug);

                    realPlayer.setNumber(player.number());
                    realPlayer.setName(player.name());
                    realPlayer.setPosition(player.position());
                    realPlayer.setAge(player.age());
                    realPlayer.setNationality(player.nationality());
                    realPlayer.setCountryCode(player.countryCode());
                    realPlayer.setPhoto(player.photo());

                    realPlayer.setAppearances(player.appearances());
                    realPlayer.setLineups(player.lineups());
                    realPlayer.setMinutes(player.minutes());
                    realPlayer.setRating(player.rating());
                    realPlayer.setCaptain(player.captain());

                    realPlayer.setGoals(player.goals());
                    realPlayer.setAssists(player.assists());
                    realPlayer.setGoalsConceded(player.goalsConceded());
                    realPlayer.setSaves(player.saves());

                    realPlayer.setShotsTotal(player.shotsTotal());
                    realPlayer.setShotsOn(player.shotsOn());

                    realPlayer.setPassesTotal(player.passesTotal());
                    realPlayer.setPassesKey(player.passesKey());
                    realPlayer.setPassesAccuracy(player.passesAccuracy());

                    realPlayer.setTacklesTotal(player.tacklesTotal());
                    realPlayer.setTacklesBlocks(player.tacklesBlocks());
                    realPlayer.setTacklesInterceptions(player.tacklesInterceptions());

                    realPlayer.setDuelsTotal(player.duelsTotal());
                    realPlayer.setDuelsWon(player.duelsWon());

                    realPlayer.setDribblesAttempts(player.dribblesAttempts());
                    realPlayer.setDribblesSuccess(player.dribblesSuccess());
                    realPlayer.setDribblesPast(player.dribblesPast());

                    realPlayer.setFoulsDrawn(player.foulsDrawn());
                    realPlayer.setFoulsCommitted(player.foulsCommitted());

                    realPlayer.setYellowCards(player.yellowCards());
                    realPlayer.setYellowRedCards(player.yellowRedCards());
                    realPlayer.setRedCards(player.redCards());

                    realPlayer.setPenaltyWon(player.penaltyWon());
                    realPlayer.setPenaltyCommitted(player.penaltyCommitted());
                    realPlayer.setPenaltyScored(player.penaltyScored());
                    realPlayer.setPenaltyMissed(player.penaltyMissed());
                    realPlayer.setPenaltySaved(player.penaltySaved());

                    return realPlayer;
                })
                .toList();

        realPlayerRepository.saveAll(realPlayers);
    }

    private List<PlayerRow> loadPlayersFromDatabase(String leagueSlug, String clubSlug) {
        return realPlayerRepository.findByLeagueSlugAndClubSlug(leagueSlug, clubSlug)
                .stream()
                .map(player -> new PlayerRow(
                        player.getApiPlayerId(),
                        player.getNumber(),
                        player.getName(),
                        player.getPosition(),
                        player.getAge(),
                        player.getNationality(),
                        player.getCountryCode(),
                        player.getPhoto(),

                        player.getAppearances(),
                        player.getLineups(),
                        player.getMinutes(),
                        player.getRating(),
                        player.getCaptain(),

                        player.getGoals(),
                        player.getAssists(),
                        player.getGoalsConceded(),
                        player.getSaves(),

                        player.getShotsTotal(),
                        player.getShotsOn(),

                        player.getPassesTotal(),
                        player.getPassesKey(),
                        player.getPassesAccuracy(),

                        player.getTacklesTotal(),
                        player.getTacklesBlocks(),
                        player.getTacklesInterceptions(),

                        player.getDuelsTotal(),
                        player.getDuelsWon(),

                        player.getDribblesAttempts(),
                        player.getDribblesSuccess(),
                        player.getDribblesPast(),

                        player.getFoulsDrawn(),
                        player.getFoulsCommitted(),

                        player.getYellowCards(),
                        player.getYellowRedCards(),
                        player.getRedCards(),

                        player.getPenaltyWon(),
                        player.getPenaltyCommitted(),
                        player.getPenaltyScored(),
                        player.getPenaltyMissed(),
                        player.getPenaltySaved()
                ))
                .sorted(Comparator
                        .comparing((PlayerRow player) -> positionOrder(player.position()))
                        .thenComparing(player -> player.number() == null ? 999 : player.number())
                        .thenComparing(PlayerRow::name))
                .toList();
    }

    private List<ClubView> loadClubsFromFolder(String leagueFolder) {
        try {
            Path folderPath = Paths.get("src/main/resources/static/Images/leagues/" + leagueFolder);

            return Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String file = path.getFileName().toString().toLowerCase();
                        return file.endsWith(".png") || file.endsWith(".jpg") || file.endsWith(".jpeg");
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

                        String logo = "/Images/leagues/" + leagueFolder + "/" + fileName;

                        return new ClubView(name, logo, slug);
                    })
                    .sorted(Comparator.comparing(ClubView::name))
                    .toList();

        } catch (IOException e) {
            return List.of();
        }
    }

    private List<PlayerRow> buildPlayers(Map<String, Object> squadData,
                                         List<Map<String, Object>> playersResponse) {

        if (squadData == null) {
            return List.of();
        }

        List<Map<String, Object>> squadResponse =
                (List<Map<String, Object>>) squadData.get("response");

        if (squadResponse == null || squadResponse.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> squadPlayers =
                (List<Map<String, Object>>) squadResponse.get(0).get("players");

        if (squadPlayers == null || squadPlayers.isEmpty()) {
            return List.of();
        }

        return squadPlayers.stream()
                .map(squadPlayer -> {
                    Integer playerId = getInt(squadPlayer, "id");

                    Map<String, Object> fullPlayerData = null;

                    if (playersResponse != null) {
                        fullPlayerData = playersResponse.stream()
                                .filter(item -> {
                                    Map<String, Object> player =
                                            (Map<String, Object>) item.get("player");

                                    return player != null && player.get("id").equals(playerId);
                                })
                                .findFirst()
                                .orElse(null);
                    }

                    String name = getString(squadPlayer, "name", "-");
                    String position = getString(squadPlayer, "position", "-");
                    Integer number = getInt(squadPlayer, "number");
                    String photo = getString(squadPlayer, "photo", "/Images/no-player.png");
                    Integer age = getInt(squadPlayer, "age");
                    String nationality = "-";

                    Integer appearances = null;
                    Integer lineups = null;
                    Integer minutes = null;
                    String rating = null;
                    Boolean captain = null;

                    Integer goals = null;
                    Integer assists = null;
                    Integer goalsConceded = null;
                    Integer saves = null;

                    Integer shotsTotal = null;
                    Integer shotsOn = null;

                    Integer passesTotal = null;
                    Integer passesKey = null;
                    String passesAccuracy = null;

                    Integer tacklesTotal = null;
                    Integer tacklesBlocks = null;
                    Integer tacklesInterceptions = null;

                    Integer duelsTotal = null;
                    Integer duelsWon = null;

                    Integer dribblesAttempts = null;
                    Integer dribblesSuccess = null;
                    Integer dribblesPast = null;

                    Integer foulsDrawn = null;
                    Integer foulsCommitted = null;

                    Integer yellowCards = null;
                    Integer yellowRedCards = null;
                    Integer redCards = null;

                    Integer penaltyWon = null;
                    Integer penaltyCommitted = null;
                    Integer penaltyScored = null;
                    Integer penaltyMissed = null;
                    Integer penaltySaved = null;

                    if (fullPlayerData != null) {
                        Map<String, Object> player =
                                (Map<String, Object>) fullPlayerData.get("player");

                        if (player != null) {
                            if (player.get("age") != null) {
                                age = getInt(player, "age");
                            }

                            nationality = getString(player, "nationality", "-");
                        }

                        List<Map<String, Object>> statistics =
                                (List<Map<String, Object>>) fullPlayerData.get("statistics");

                        if (statistics != null && !statistics.isEmpty()) {
                            Map<String, Object> stats = statistics.get(0);

                            Map<String, Object> games = getMap(stats, "games");
                            Map<String, Object> goalsMap = getMap(stats, "goals");
                            Map<String, Object> shots = getMap(stats, "shots");
                            Map<String, Object> passes = getMap(stats, "passes");
                            Map<String, Object> tackles = getMap(stats, "tackles");
                            Map<String, Object> duels = getMap(stats, "duels");
                            Map<String, Object> dribbles = getMap(stats, "dribbles");
                            Map<String, Object> fouls = getMap(stats, "fouls");
                            Map<String, Object> cards = getMap(stats, "cards");
                            Map<String, Object> penalty = getMap(stats, "penalty");

                            appearances = getInt(games, "appearences");
                            lineups = getInt(games, "lineups");
                            minutes = getInt(games, "minutes");
                            rating = getString(games, "rating", null);
                            captain = getBoolean(games, "captain");

                            goals = getInt(goalsMap, "total");
                            assists = getInt(goalsMap, "assists");
                            goalsConceded = getInt(goalsMap, "conceded");
                            saves = getInt(goalsMap, "saves");

                            shotsTotal = getInt(shots, "total");
                            shotsOn = getInt(shots, "on");

                            passesTotal = getInt(passes, "total");
                            passesKey = getInt(passes, "key");
                            passesAccuracy = getString(passes, "accuracy", null);

                            tacklesTotal = getInt(tackles, "total");
                            tacklesBlocks = getInt(tackles, "blocks");
                            tacklesInterceptions = getInt(tackles, "interceptions");

                            duelsTotal = getInt(duels, "total");
                            duelsWon = getInt(duels, "won");

                            dribblesAttempts = getInt(dribbles, "attempts");
                            dribblesSuccess = getInt(dribbles, "success");
                            dribblesPast = getInt(dribbles, "past");

                            foulsDrawn = getInt(fouls, "drawn");
                            foulsCommitted = getInt(fouls, "committed");

                            yellowCards = getInt(cards, "yellow");
                            yellowRedCards = getInt(cards, "yellowred");
                            redCards = getInt(cards, "red");

                            penaltyWon = getInt(penalty, "won");
                            penaltyCommitted = getInt(penalty, "commited");
                            penaltyScored = getInt(penalty, "scored");
                            penaltyMissed = getInt(penalty, "missed");
                            penaltySaved = getInt(penalty, "saved");
                        }
                    }

                    return new PlayerRow(
                            playerId,
                            number,
                            name,
                            position,
                            age,
                            nationality,
                            getCountryCodeForNationality(nationality),
                            photo,

                            appearances,
                            lineups,
                            minutes,
                            rating,
                            captain,

                            goals,
                            assists,
                            goalsConceded,
                            saves,

                            shotsTotal,
                            shotsOn,

                            passesTotal,
                            passesKey,
                            passesAccuracy,

                            tacklesTotal,
                            tacklesBlocks,
                            tacklesInterceptions,

                            duelsTotal,
                            duelsWon,

                            dribblesAttempts,
                            dribblesSuccess,
                            dribblesPast,

                            foulsDrawn,
                            foulsCommitted,

                            yellowCards,
                            yellowRedCards,
                            redCards,

                            penaltyWon,
                            penaltyCommitted,
                            penaltyScored,
                            penaltyMissed,
                            penaltySaved
                    );
                })
                .sorted(Comparator
                        .comparing((PlayerRow player) -> positionOrder(player.position()))
                        .thenComparing(player -> player.number() == null ? 999 : player.number())
                        .thenComparing(PlayerRow::name))
                .toList();
    }

    private Map<String, Object> getMap(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        return (Map<String, Object>) source.get(key);
    }

    private Integer getInt(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        Object value = source.get(key);

        if (value instanceof Integer integer) {
            return integer;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String getString(Map<String, Object> source, String key, String defaultValue) {
        if (source == null || source.get(key) == null) {
            return defaultValue;
        }

        return source.get(key).toString();
    }

    private Boolean getBoolean(Map<String, Object> source, String key) {
        if (source == null || source.get(key) == null) {
            return null;
        }

        Object value = source.get(key);

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(value.toString());
    }

    private int positionOrder(String position) {
        if (position == null) {
            return 99;
        }

        return switch (position) {
            case "Goalkeeper" -> 1;
            case "Defender" -> 2;
            case "Midfielder" -> 3;
            case "Attacker" -> 4;
            default -> 99;
        };
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

    public record LeagueView(String slug, String name, String folder, String logo, Integer apiLeagueId) {
    }

    public record ClubView(String name, String logo, String slug) {
    }

    public record PlayerRow(
            Integer id,
            Integer number,
            String name,
            String position,
            Integer age,
            String nationality,
            String countryCode,
            String photo,

            Integer appearances,
            Integer lineups,
            Integer minutes,
            String rating,
            Boolean captain,

            Integer goals,
            Integer assists,
            Integer goalsConceded,
            Integer saves,

            Integer shotsTotal,
            Integer shotsOn,

            Integer passesTotal,
            Integer passesKey,
            String passesAccuracy,

            Integer tacklesTotal,
            Integer tacklesBlocks,
            Integer tacklesInterceptions,

            Integer duelsTotal,
            Integer duelsWon,

            Integer dribblesAttempts,
            Integer dribblesSuccess,
            Integer dribblesPast,

            Integer foulsDrawn,
            Integer foulsCommitted,

            Integer yellowCards,
            Integer yellowRedCards,
            Integer redCards,

            Integer penaltyWon,
            Integer penaltyCommitted,
            Integer penaltyScored,
            Integer penaltyMissed,
            Integer penaltySaved
    ) {
    }
}