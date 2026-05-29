package com.footballstats.controller;

import com.footballstats.dto.MatchLineupDto;
import com.footballstats.model.EuropeanCompetitionParticipant;
import com.footballstats.model.EuropeanMatch;
import com.footballstats.model.EuropeanMatchEvent;
import com.footballstats.model.GeneratedPlayer;
import com.footballstats.repository.EuropeanMatchEventRepository;
import com.footballstats.repository.EuropeanMatchRepository;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.service.europe.EuropeanCompetitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class EuropeanMatchesController {

    private static final String SEASON = "2025/2026";

    private final EuropeanCompetitionService europeanCompetitionService;
    private final EuropeanMatchRepository europeanMatchRepository;
    private final EuropeanMatchEventRepository europeanMatchEventRepository;
    private final GeneratedPlayerRepository generatedPlayerRepository;

    public EuropeanMatchesController(EuropeanCompetitionService europeanCompetitionService,
                                     EuropeanMatchRepository europeanMatchRepository,
                                     EuropeanMatchEventRepository europeanMatchEventRepository,
                                     GeneratedPlayerRepository generatedPlayerRepository) {
        this.europeanCompetitionService = europeanCompetitionService;
        this.europeanMatchRepository = europeanMatchRepository;
        this.europeanMatchEventRepository = europeanMatchEventRepository;
        this.generatedPlayerRepository = generatedPlayerRepository;
    }

    @GetMapping("/matches/europe/{competition}")
    public String europeanCompetition(@PathVariable String competition, Model model) {

        String competitionName = switch (competition) {
            case "champions-league" -> "UEFA Champions League";
            case "europa-league" -> "UEFA Europa League";
            case "conference-league" -> "UEFA Conference League";
            default -> "European Competition";
        };

        String logo = switch (competition) {
            case "champions-league" -> "/Images/european_leagues/UCL.png";
            case "europa-league" -> "/Images/european_leagues/UEL.png";
            case "conference-league" -> "/Images/european_leagues/UECL.png";
            default -> "/Images/Favicon.png";
        };

        List<EuropeanCompetitionParticipant> participants =
                europeanCompetitionService.getParticipants(SEASON, competition);

        List<EuropeanMatch> matches =
                europeanMatchRepository.findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(
                        SEASON,
                        competition
                );

        model.addAttribute("competition", competition);
        model.addAttribute("competitionName", competitionName);
        model.addAttribute("competitionLogo", logo);
        model.addAttribute("season", SEASON);
        model.addAttribute("participants", participants);
        model.addAttribute("matches", matches);
        model.addAttribute("participantsCount", participants.size());
        model.addAttribute("matchesCount", matches.size());
        model.addAttribute("leaguePhaseTable",
                europeanCompetitionService.calculateLeaguePhaseTable(SEASON, competition));
        model.addAttribute("activeEuropeanView", "overview");
        model.addAttribute("leagueFolders", getLeagueFolders());

        return "european_matches";
    }

    @PostMapping("/europe/{competition}/generate")
    public String generateEuropeanCompetition(@PathVariable String competition) {
        europeanCompetitionService.generateParticipantsFromDomesticTables(SEASON);
        europeanCompetitionService.generateLeaguePhaseMatches(SEASON, competition);

        return "redirect:/matches/europe/" + competition;
    }

    @GetMapping("/europe/{competition}/draw-simulation")
    public String drawSimulation(@PathVariable String competition, Model model) {
        europeanCompetition(competition, model);
        return "european_draw_simulation";
    }

    @GetMapping("/europe/{competition}/league-phase")
    public String leaguePhase(@PathVariable String competition, Model model) {
        String view = europeanCompetition(competition, model);

        model.addAttribute("activeEuropeanView", "league-phase");
        model.addAttribute(
                "roundViews",
                europeanCompetitionService.buildLeaguePhaseRoundViews(SEASON, competition)
        );

        return view;
    }

    @GetMapping("/europe/{competition}/knockout")
    public String knockout(@PathVariable String competition, Model model) {
        return europeanCompetition(competition, model);
    }

    @GetMapping("/europe/{competition}/match/{matchId}")
    public String europeanMatchDetails(@PathVariable String competition,
                                       @PathVariable Long matchId,
                                       Model model) {

        EuropeanMatch match = europeanMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("European match not found: " + matchId));

        if (!SEASON.equals(match.getSeason()) || !competition.equals(match.getCompetitionSlug())) {
            throw new RuntimeException("Match does not belong to this competition: " + matchId);
        }

        europeanCompetition(competition, model);

        model.addAttribute("activeEuropeanView", "match-details");
        model.addAttribute("selectedMatch", match);

        return "european_matches";
    }

    @GetMapping("/europe/match/{matchId}/events")
    @ResponseBody
    public List<EuropeanMatchEvent> getEuropeanMatchEvents(@PathVariable Long matchId) {
        EuropeanMatch match = europeanMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("European match not found: " + matchId));

        return europeanMatchEventRepository.findByMatchIdOrderByEventMinuteAsc(match.getId());
    }

    @GetMapping("/europe/match/{matchId}/lineups")
    @ResponseBody
    public MatchLineupDto getEuropeanMatchLineups(@PathVariable Long matchId) {
        EuropeanMatch match = europeanMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("European match not found: " + matchId));

        List<EuropeanMatchEvent> events =
                europeanMatchEventRepository.findByMatchIdOrderByEventMinuteAsc(match.getId());

        List<MatchLineupDto.PlayerDto> homePlayers =
                getEuropeanPlayersForTeam(
                        match.getHomeLeagueSlug(),
                        match.getHomeClubSlug(),
                        match.getHomeFormation(),
                        events
                );

        List<MatchLineupDto.PlayerDto> awayPlayers =
                getEuropeanPlayersForTeam(
                        match.getAwayLeagueSlug(),
                        match.getAwayClubSlug(),
                        match.getAwayFormation(),
                        events
                );

        return new MatchLineupDto(
                homePlayers.stream().limit(11).toList(),
                awayPlayers.stream().limit(11).toList(),
                homePlayers.stream().skip(11).limit(9).toList(),
                awayPlayers.stream().skip(11).limit(9).toList()
        );
    }

    private List<MatchLineupDto.PlayerDto> getEuropeanPlayersForTeam(String leagueSlug,
                                                                     String clubSlug,
                                                                     String formation,
                                                                     List<EuropeanMatchEvent> events) {
        List<GeneratedPlayer> players =
                generatedPlayerRepository.findByLeagueSlugAndClubSlug(leagueSlug, clubSlug);

        return generateLineup(players, formation)
                .stream()
                .map(player -> new MatchLineupDto.PlayerDto(
                        player.getId(),
                        playerName(player),
                        player.getPosition(),
                        player.getShirtNumber(),
                        player.getImageUrl(),
                        player.getCountryCode(),
                        getEuropeanPlayerEvents(player.getId(), events)
                ))
                .toList();
    }

    private List<MatchLineupDto.EventIconDto> getEuropeanPlayerEvents(Long playerId,
                                                                      List<EuropeanMatchEvent> events) {
        if (playerId == null || events == null) {
            return List.of();
        }

        List<EuropeanMatchEvent> playerEvents = events.stream()
                .filter(event ->
                        playerId.equals(event.getPlayerId())
                                || playerId.equals(event.getRelatedPlayerId())
                                || isEuropeanSubstitutionOut(playerId, event)
                )
                .filter(event -> isEuropeanLineupVisibleEvent(event.getEventType()))
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
                        eventType = "ASSIST";
                    }

                    return new MatchLineupDto.EventIconDto(
                            iconUrl,
                            eventType,
                            event.getEventMinute()
                    );
                })
                .toList();
    }

    private boolean isEuropeanSubstitutionOut(Long playerId, EuropeanMatchEvent event) {
        return "SUBSTITUTION".equals(event.getEventType())
                && playerId.equals(event.getRelatedPlayerId());
    }

    private boolean isEuropeanLineupVisibleEvent(String eventType) {
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
                .sorted(Comparator.comparing(
                        GeneratedPlayer::getMarketValue,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
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
                            || position.contains("keeper")
                            || position.contains("bramkarz");

            case "defender" ->
                    position.contains("back")
                            || position.contains("defender")
                            || position.contains("obrońca")
                            || position.contains("obronca");

            case "midfielder" ->
                    position.contains("midfield")
                            || position.contains("midfielder")
                            || position.contains("pomocnik");

            case "forward" ->
                    position.contains("forward")
                            || position.contains("striker")
                            || position.contains("winger")
                            || position.contains("napastnik");

            default -> false;
        };
    }

    private String playerName(GeneratedPlayer player) {
        if (player == null) {
            return "Unknown player";
        }

        return player.getFirstName() + " " + player.getLastName();
    }

    private Map<String, String> getLeagueFolders() {
        return Map.ofEntries(
                Map.entry("england", "England - Premier League"),
                Map.entry("spain", "Spain - LaLiga"),
                Map.entry("italy", "Italy - Serie A"),
                Map.entry("germany", "Germany - Bundesliga"),
                Map.entry("france", "France - Ligue 1"),
                Map.entry("netherlands", "Netherlands - Eredivisie"),
                Map.entry("portugal", "Portugal - Liga Portugal"),
                Map.entry("belgium", "Belgium - Jupiler Pro League"),
                Map.entry("turkiye", "Türkiye - Süper Lig"),
                Map.entry("scotland", "Scotland - Scottish Premiership"),
                Map.entry("austria", "Austria - Bundesliga"),
                Map.entry("switzerland", "Switzerland - Super League"),
                Map.entry("greece", "Greece - Super League 1"),
                Map.entry("denmark", "Denmark - Superliga"),
                Map.entry("poland", "Poland - PKO BP Ekstraklasa"),
                Map.entry("sweden", "Sweden - Allsvenskan"),
                Map.entry("norway", "Norway - Eliteserien"),
                Map.entry("croatia", "Croatia - SuperSport HNL"),
                Map.entry("serbia", "Serbia - Super liga Srbije"),
                Map.entry("romania", "Romania - SuperLiga"),
                Map.entry("bulgaria", "Bulgaria - efbet Liga"),
                Map.entry("ukraine", "Ukraine - Premier Liga"),
                Map.entry("czech-republic", "Czech Republic - Chance Liga"),
                Map.entry("israel", "Israel - Ligat ha'Al")
        );
    }
}