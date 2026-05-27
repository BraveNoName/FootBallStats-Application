package com.footballstats.controller;

import com.footballstats.dto.LeagueTableRow;
import com.footballstats.dto.MatchLineupDto;
import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.Match;
import com.footballstats.model.MatchEvent;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.repository.MatchEventRepository;
import com.footballstats.repository.MatchRepository;
import com.footballstats.service.LeagueTableService;
import com.footballstats.service.MatchGeneratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/matches")
public class MatchController {

    private final MatchEventRepository matchEventRepository;
    private final MatchRepository matchRepository;
    private final MatchGeneratorService matchGeneratorService;
    private final LeagueTableService leagueTableService;
    private final GeneratedPlayerRepository generatedPlayerRepository;

    public MatchController(MatchRepository matchRepository,
                           MatchGeneratorService matchGeneratorService,
                           LeagueTableService leagueTableService,
                           MatchEventRepository matchEventRepository,
                           GeneratedPlayerRepository generatedPlayerRepository) {

        this.matchRepository = matchRepository;
        this.matchGeneratorService = matchGeneratorService;
        this.leagueTableService = leagueTableService;
        this.matchEventRepository = matchEventRepository;
        this.generatedPlayerRepository = generatedPlayerRepository;
    }

    @GetMapping
    public String matchesPage(@RequestParam(required = false) String league,
                              Model model) {

        List<Match> matches = List.of();
        List<LeagueTableRow> leagueTable = List.of();

        if (league != null && !league.isBlank()) {
            matches = matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(league);
            leagueTable = leagueTableService.calculateTable(matches, league);
        }

        model.addAttribute("matches", matches);
        model.addAttribute("leagueTable", leagueTable);
        model.addAttribute("league", league);
        model.addAttribute("leagues", getAvailableLeagues());

        model.addAttribute(
                "seasonAlreadyGenerated",
                league != null && !league.isBlank() && matchRepository.existsByLeague(league)
        );

        return "matches";
    }

    @GetMapping("/{matchId}/lineups")
    @ResponseBody
    public MatchLineupDto getMatchLineups(@PathVariable Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        List<MatchEvent> events =
                matchEventRepository.findByMatchIdOrderByEventMinuteAsc(matchId);

        List<MatchLineupDto.PlayerDto> homePlayers =
                getPlayersForTeam(
                        match.getLeague(),
                        match.getHomeTeam(),
                        events,
                        match.getHomeFormation()
                );

        List<MatchLineupDto.PlayerDto> awayPlayers =
                getPlayersForTeam(
                        match.getLeague(),
                        match.getAwayTeam(),
                        events,
                        match.getAwayFormation()
                );

        return new MatchLineupDto(
                homePlayers.stream().limit(11).toList(),
                awayPlayers.stream().limit(11).toList(),
                homePlayers.stream().skip(11).limit(9).toList(),
                awayPlayers.stream().skip(11).limit(9).toList()
        );
    }

    private List<MatchLineupDto.PlayerDto> getPlayersForTeam(String league,
                                                             String teamName,
                                                             List<MatchEvent> events,
                                                             String formation) {

        List<GeneratedPlayer> players = generatedPlayerRepository
                .findByLeagueSlugAndClubSlug(league, createSlug(teamName));

        List<GeneratedPlayer> sorted = generateLineup(players, formation);

        return sorted.stream()
                .map(player -> new MatchLineupDto.PlayerDto(
                        player.getId(),
                        player.getFirstName() + " " + player.getLastName(),
                        player.getPosition(),
                        player.getShirtNumber(),
                        player.getImageUrl(),
                        player.getCountryCode(),
                        getPlayerEvents(player.getId(), events)
                ))
                .toList();
    }

    private List<MatchLineupDto.EventIconDto> getPlayerEvents(Long playerId,
                                                              List<MatchEvent> events) {
        if (playerId == null || events == null) {
            return List.of();
        }

        List<MatchEvent> playerEvents = events.stream()
                .filter(event ->
                        playerId.equals(event.getPlayerId())
                                || playerId.equals(event.getRelatedPlayerId())
                                || isSubstitutionOut(playerId, event)
                )
                .filter(event -> isLineupVisibleEvent(event.getEventType()))
                .toList();

        boolean hasAnyRed = playerEvents.stream()
                .anyMatch(event ->
                        "SECOND_YELLOW_RED_CARD".equals(event.getEventType())
                                || "RED_CARD".equals(event.getEventType())
                );

        Set<String> alreadyShown = new HashSet<>();

        return playerEvents.stream()
                .filter(event -> {
                    String type = event.getEventType();

                    if ("YELLOW_CARD".equals(type) && hasAnyRed) {
                        return false;
                    }

                    if ("YELLOW_CARD".equals(type) && alreadyShown.contains(type)) {
                        return false;
                    }

                    alreadyShown.add(type);
                    return true;
                })
                .map(event -> {
                    String iconUrl = event.getIconUrl();
                    String eventType = event.getEventType();

                    if (playerId.equals(event.getRelatedPlayerId())
                            && "GOAL".equals(event.getEventType())) {
                        iconUrl = "/Images/match_events/assist.png";
                        eventType = "Assist";
                    }

                    return new MatchLineupDto.EventIconDto(
                            iconUrl,
                            eventType,
                            event.getEventMinute()
                    );
                })
                .toList();
    }

    private boolean isSubstitutionOut(Long playerId, MatchEvent event) {
        return "SUBSTITUTION".equals(event.getEventType())
                && playerId.equals(event.getRelatedPlayerId());
    }

    private boolean isLineupVisibleEvent(String eventType) {
        return List.of(
                "GOAL",
                "PENALTY_GOAL",
                "PENALTY_MISSED",
                "OWN_GOAL",
                "YELLOW_CARD",
                "RED_CARD",
                "SECOND_YELLOW_RED_CARD",
                "INJURY",
                "SUBSTITUTION"
        ).contains(eventType);
    }

    private int positionOrder(GeneratedPlayer player) {
        String position = player.getPosition() == null
                ? ""
                : player.getPosition().toLowerCase();

        if (position.contains("goalkeeper") || position.contains("bramkarz")) return 1;
        if (position.contains("back") || position.contains("defender") || position.contains("obrońca")) return 2;
        if (position.contains("midfielder") || position.contains("pomocnik")) return 3;
        if (position.contains("forward") || position.contains("striker") || position.contains("winger") || position.contains("napastnik")) return 4;

        return 5;
    }

    private String createSlug(String name) {
        return name.toLowerCase()
                .replace("&", "and")
                .replace("'", "")
                .replace(".", "")
                .replace(" ", "-");
    }

    @GetMapping("/{matchId}/events")
    @ResponseBody
    public List<MatchEvent> getMatchEvents(@PathVariable Long matchId) {
        return matchEventRepository.findByMatchIdOrderByEventMinuteAsc(matchId);
    }

    @PostMapping("/generate")
    public String generateSeason(@RequestParam String league) {
        matchGeneratorService.generateLeagueSeason(league);
        return "redirect:/matches?league=" + league;
    }

    private List<GeneratedPlayer> generateLineup(List<GeneratedPlayer> players,
                                                 String formation) {

        if (players == null || players.isEmpty()) {
            return List.of();
        }

        int defenders = 4;
        int midfielders = 3;
        int forwards = 3;

        if (formation != null && !formation.isBlank()) {
            String[] split = formation.split("-");

            defenders = Integer.parseInt(split[0]);
            forwards = Integer.parseInt(split[split.length - 1]);
            midfielders = 10 - defenders - forwards;
        }

        List<GeneratedPlayer> starting = new ArrayList<>();

        starting.addAll(bestPlayers(players, "goalkeeper", 1));
        starting.addAll(bestPlayers(players, "defender", defenders));
        starting.addAll(bestPlayers(players, "midfielder", midfielders));
        starting.addAll(bestPlayers(players, "forward", forwards));

        List<GeneratedPlayer> bench = players.stream()
                .filter(player -> !starting.contains(player))
                .sorted(Comparator.comparing(
                        GeneratedPlayer::getMarketValue,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(9)
                .toList();

        List<GeneratedPlayer> result = new ArrayList<>();
        result.addAll(starting.stream().distinct().limit(11).toList());
        result.addAll(bench.stream().distinct().limit(9).toList());

        return result;
    }

    private List<GeneratedPlayer> bestPlayers(List<GeneratedPlayer> players,
                                              String type,
                                              int limit) {

        return players.stream()
                .filter(player -> matchesPosition(player, type))
                .sorted(
                        Comparator.comparing(
                                GeneratedPlayer::getMarketValue,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .limit(limit)
                .toList();
    }

    private boolean matchesPosition(GeneratedPlayer player, String type) {

        String position = player.getPosition() == null
                ? ""
                : player.getPosition().toLowerCase();

        return switch (type) {

            case "goalkeeper" ->
                    position.contains("goalkeeper")
                            || position.contains("keeper");

            case "defender" ->
                    position.contains("back")
                            || position.contains("defender");

            case "midfielder" ->
                    position.contains("midfield")
                            || position.contains("midfielder");

            case "forward" ->
                    position.contains("forward")
                            || position.contains("striker")
                            || position.contains("winger");

            default -> false;
        };
    }

    private List<String> getAvailableLeagues() {
        return List.of(
                "austria",
                "belgium",
                "bulgaria",
                "croatia",
                "czech-republic",
                "denmark",
                "england",
                "france",
                "germany",
                "greece",
                "israel",
                "italy",
                "netherlands",
                "norway",
                "poland",
                "portugal",
                "romania",
                "russia",
                "scotland",
                "serbia",
                "spain",
                "sweden",
                "switzerland",
                "turkiye",
                "ukraine"
        );
    }
}