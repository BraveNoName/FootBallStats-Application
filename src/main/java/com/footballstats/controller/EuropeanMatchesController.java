package com.footballstats.controller;

import com.footballstats.dto.MatchLineupDto;
import com.footballstats.dto.KnockoutLegDetailsView;
import com.footballstats.dto.european_knockout.EuropeanBracketHalfView;
import com.footballstats.dto.european_knockout.EuropeanBracketPairView;
import com.footballstats.dto.european_knockout.EuropeanBracketQuarterView;
import com.footballstats.dto.european_knockout.EuropeanBracketTreeView;
import com.footballstats.model.*;
import com.footballstats.repository.EuropeanKnockoutMatchRepository;
import com.footballstats.repository.EuropeanMatchEventRepository;
import com.footballstats.repository.EuropeanMatchRepository;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.service.europe.EuropeanCompetitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.footballstats.model.Referee;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import com.footballstats.repository.RefereeRepository;
import java.time.LocalDateTime;

@Controller
public class EuropeanMatchesController {

    private static final String SEASON = "2025/2026";

    private final EuropeanCompetitionService europeanCompetitionService;
    private final EuropeanMatchRepository europeanMatchRepository;
    private final EuropeanMatchEventRepository europeanMatchEventRepository;
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final EuropeanKnockoutMatchRepository europeanKnockoutMatchRepository;
    private final RefereeRepository refereeRepository;

    public EuropeanMatchesController(EuropeanCompetitionService europeanCompetitionService,
                                     EuropeanMatchRepository europeanMatchRepository,
                                     EuropeanMatchEventRepository europeanMatchEventRepository,
                                     GeneratedPlayerRepository generatedPlayerRepository,
                                     EuropeanKnockoutMatchRepository europeanKnockoutMatchRepository,
                                     RefereeRepository refereeRepository) {
        this.europeanCompetitionService = europeanCompetitionService;
        this.europeanMatchRepository = europeanMatchRepository;
        this.europeanMatchEventRepository = europeanMatchEventRepository;
        this.generatedPlayerRepository = generatedPlayerRepository;
        this.europeanKnockoutMatchRepository = europeanKnockoutMatchRepository;
        this.refereeRepository = refereeRepository;
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

        model.addAttribute("europeTopScorers",
                europeanMatchEventRepository.findAll().stream()
                        .filter(e -> "GOAL".equals(e.getEventType()) || "PENALTY_GOAL".equals(e.getEventType()))
                        .filter(e -> e.getPlayerId() != null)
                        .collect(java.util.stream.Collectors.groupingBy(
                                EuropeanMatchEvent::getPlayerId,
                                java.util.stream.Collectors.counting()
                        ))
                        .entrySet().stream()
                        .map(entry -> {
                            EuropeanMatchEvent sample = europeanMatchEventRepository.findAll().stream()
                                    .filter(e -> entry.getKey().equals(e.getPlayerId()))
                                    .findFirst()
                                    .orElse(null);

                            return buildPlayerStat(
                                    entry.getKey(),
                                    sample == null ? "Unknown player" : sample.getPlayerName(),
                                    entry.getValue()
                            );
                        })
                        .sorted(java.util.Comparator.comparing(EuropeanPlayerStatView::value).reversed())
                        .limit(10)
                        .toList()
        );

        model.addAttribute("europeTopAssists",
                buildEventPlayerStats(
                        europeanMatchEventRepository.findAll(),
                        List.of("GOAL", "PENALTY_GOAL"),
                        true
                )
        );

        model.addAttribute("europeYellowCards",
                buildEventPlayerStats(
                        europeanMatchEventRepository.findAll(),
                        List.of("YELLOW_CARD"),
                        false
                )
        );

        model.addAttribute("europeRedCards",
                buildEventPlayerStats(
                        europeanMatchEventRepository.findAll(),
                        List.of("RED_CARD", "SECOND_YELLOW_RED_CARD"),
                        false
                )
        );

        model.addAttribute("europeCleanSheets",
                calculateEuropeanCleanSheets(
                        europeanMatchRepository
                                .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(
                                        SEASON,
                                        competition
                                )
                )
        );

        return "european_matches";
    }

    @PostMapping("/europe/{competition}/generate")
    public String generateEuropeanCompetition(@PathVariable String competition) {
        europeanCompetitionService.generateOrRegenerateEuropeanCompetition(SEASON, competition);

        return "redirect:/matches/europe/" + competition;
    }

    @GetMapping("/europe/{competition}/generation-status")
    @ResponseBody
    public Map<String, Object> europeanGenerationStatus(@PathVariable String competition) {
        Map<String, Object> status = new HashMap<>();

        status.put(
                "competitionGenerated",
                europeanCompetitionService.isEuropeanCompetitionGenerated(SEASON, competition)
        );

        status.put(
                "allDomesticLeaguesGenerated",
                europeanCompetitionService.areAllDomesticLeaguesGenerated()
        );

        return status;
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

    @GetMapping("/europe/{competition}/statistics")
    public String statistics(@PathVariable String competition, Model model) {
        String view = europeanCompetition(competition, model);

        model.addAttribute("activeEuropeanView", "statistics");

        return view;
    }

    @GetMapping("/europe/{competition}/knockout")
    public String knockout(@PathVariable String competition,
                           Model model) {

        String view = europeanCompetition(competition, model);

        if (!europeanCompetitionService.isKnockoutGenerated(SEASON, competition)) {
            europeanCompetitionService.generateKnockoutStage(SEASON, competition);
        }

        List<EuropeanKnockoutMatch> knockoutMatches =
                europeanCompetitionService.getKnockoutMatches(SEASON, competition);

        List<EuropeanKnockoutMatch> playOffMatches = knockoutMatches.stream()
                .filter(match -> "PLAY-OFF".equals(match.getRound()))
                .toList();

        List<EuropeanKnockoutMatch> roundOf16Matches = knockoutMatches.stream()
                .filter(match -> "ROUND-OF-16".equals(match.getRound()))
                .toList();

        List<EuropeanKnockoutMatch> quarterFinalMatches = knockoutMatches.stream()
                .filter(match -> "QUARTER-FINAL".equals(match.getRound()))
                .toList();

        List<EuropeanKnockoutMatch> semiFinalMatches = knockoutMatches.stream()
                .filter(match -> "SEMI-FINAL".equals(match.getRound()))
                .toList();

        List<EuropeanKnockoutMatch> finalMatches = knockoutMatches.stream()
                .filter(match -> "FINAL".equals(match.getRound()))
                .toList();

        model.addAttribute("activeEuropeanView", "knockout");
        model.addAttribute("playOffMatches", playOffMatches);
        model.addAttribute("roundOf16Matches", roundOf16Matches);
        model.addAttribute("quarterFinalMatches", quarterFinalMatches);
        model.addAttribute("semiFinalMatches", semiFinalMatches);
        model.addAttribute("finalMatches", finalMatches);
        model.addAttribute("bracketTree", buildEuropeanBracketTree(
                playOffMatches,
                roundOf16Matches,
                quarterFinalMatches,
                semiFinalMatches,
                finalMatches
        ));

        return view;
    }

    private EuropeanBracketTreeView buildEuropeanBracketTree(
            List<EuropeanKnockoutMatch> playOffMatches,
            List<EuropeanKnockoutMatch> roundOf16Matches,
            List<EuropeanKnockoutMatch> quarterFinalMatches,
            List<EuropeanKnockoutMatch> semiFinalMatches,
            List<EuropeanKnockoutMatch> finalMatches
    ) {
        if (playOffMatches.size() < 8
                || roundOf16Matches.size() < 8
                || quarterFinalMatches.size() < 4
                || semiFinalMatches.size() < 2) {
            throw new RuntimeException("Knockout bracket is incomplete.");
        }

        EuropeanKnockoutMatch finalMatch = finalMatches.isEmpty()
                ? null
                : finalMatches.get(0);

        List<EuropeanBracketPairView> pairs = List.of(
                new EuropeanBracketPairView(playOffMatches.get(7), roundOf16Matches.get(0)),
                new EuropeanBracketPairView(playOffMatches.get(6), roundOf16Matches.get(1)),
                new EuropeanBracketPairView(playOffMatches.get(5), roundOf16Matches.get(2)),
                new EuropeanBracketPairView(playOffMatches.get(4), roundOf16Matches.get(3)),
                new EuropeanBracketPairView(playOffMatches.get(3), roundOf16Matches.get(4)),
                new EuropeanBracketPairView(playOffMatches.get(2), roundOf16Matches.get(5)),
                new EuropeanBracketPairView(playOffMatches.get(1), roundOf16Matches.get(6)),
                new EuropeanBracketPairView(playOffMatches.get(0), roundOf16Matches.get(7))
        );

        EuropeanBracketQuarterView qf1 = new EuropeanBracketQuarterView(
                pairs.get(0),
                pairs.get(1),
                quarterFinalMatches.get(0)
        );

        EuropeanBracketQuarterView qf2 = new EuropeanBracketQuarterView(
                pairs.get(2),
                pairs.get(3),
                quarterFinalMatches.get(1)
        );

        EuropeanBracketQuarterView qf3 = new EuropeanBracketQuarterView(
                pairs.get(4),
                pairs.get(5),
                quarterFinalMatches.get(2)
        );

        EuropeanBracketQuarterView qf4 = new EuropeanBracketQuarterView(
                pairs.get(6),
                pairs.get(7),
                quarterFinalMatches.get(3)
        );

        EuropeanBracketHalfView leftHalf = new EuropeanBracketHalfView(
                qf1,
                qf2,
                semiFinalMatches.get(0)
        );

        EuropeanBracketHalfView rightHalf = new EuropeanBracketHalfView(
                qf3,
                qf4,
                semiFinalMatches.get(1)
        );

        return new EuropeanBracketTreeView(
                leftHalf,
                rightHalf,
                finalMatch
        );
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

        List<EuropeanMatch> roundMatches =
                europeanMatchRepository
                        .findBySeasonAndCompetitionSlugAndRoundNumberOrderByMatchDateAsc(
                                match.getSeason(),
                                match.getCompetitionSlug(),
                                match.getRoundNumber()
                        );

        Long previousMatchId = null;
        Long nextMatchId = null;

        int currentIndex = -1;

        for (int i = 0; i < roundMatches.size(); i++) {
            if (roundMatches.get(i).getId().equals(match.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex > 0) {
            previousMatchId = roundMatches.get(currentIndex - 1).getId();
        }

        if (currentIndex >= 0 && currentIndex < roundMatches.size() - 1) {
            nextMatchId = roundMatches.get(currentIndex + 1).getId();
        }

        europeanCompetition(competition, model);

        model.addAttribute("activeEuropeanView", "match-details");
        model.addAttribute("selectedMatch", match);
        model.addAttribute("previousMatchId", previousMatchId);
        model.addAttribute("nextMatchId", nextMatchId);
        model.addAttribute("matchPosition", currentIndex + 1);
        model.addAttribute("matchesInRound", roundMatches.size());

        return "european_matches";
    }

    @GetMapping("/europe/match/{matchId}/events")
    @ResponseBody
    public List<EuropeanMatchEvent> getEuropeanMatchEvents(@PathVariable Long matchId) {
        EuropeanMatch match = europeanMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("European match not found: " + matchId));

        return europeanCompetitionService.getOrGenerateLeaguePhaseMatchEvents(match);
    }

    @GetMapping("/europe/match/{matchId}/lineups")
    @ResponseBody
    public MatchLineupDto getEuropeanMatchLineups(@PathVariable Long matchId) {
        EuropeanMatch match = europeanMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("European match not found: " + matchId));

        List<EuropeanMatchEvent> events =
                europeanCompetitionService.getOrGenerateLeaguePhaseMatchEvents(match);

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

        if (players.isEmpty() && clubSlug != null && !clubSlug.isBlank()) {
            players = generatedPlayerRepository.findByClubSlug(clubSlug);
        }

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

    @GetMapping("/europe/{competition}/knockout/match/{matchId}")
    public String europeanKnockoutMatchDetails(@PathVariable String competition,
                                               @PathVariable Long matchId,
                                               Model model) {

        EuropeanKnockoutMatch match = europeanKnockoutMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Knockout match not found: " + matchId));

        if (!SEASON.equals(match.getSeason()) || !competition.equals(match.getCompetitionSlug())) {
            throw new RuntimeException("Match does not belong to this competition: " + matchId);
        }

        europeanCompetition(competition, model);

        model.addAttribute("activeEuropeanView", "knockout-match-details");
        model.addAttribute("selectedKnockoutMatch", match);
        model.addAttribute("knockoutLegs", buildKnockoutLegDetails(match));

        return "european_matches";
    }

    @GetMapping("/europe/knockout/match/{matchId}/events")
    @ResponseBody
    public List<EuropeanMatchEvent> getEuropeanKnockoutMatchEvents(@PathVariable Long matchId,
                                                                   @RequestParam String leg) {
        EuropeanKnockoutMatch match = europeanKnockoutMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Knockout match not found: " + matchId));

        return europeanCompetitionService.getOrGenerateKnockoutMatchEvents(match, leg);
    }

    @GetMapping("/europe/knockout/match/{matchId}/lineups")
    @ResponseBody
    public MatchLineupDto getEuropeanKnockoutMatchLineups(@PathVariable Long matchId,
                                                          @RequestParam String leg) {
        EuropeanKnockoutMatch match = europeanKnockoutMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Knockout match not found: " + matchId));

        KnockoutLegDetailsView legDetails = buildKnockoutLegDetails(match)
                .stream()
                .filter(details -> details.getLegKey().equals(leg))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Knockout leg not found: " + leg));

        List<EuropeanMatchEvent> events =
                europeanCompetitionService.getOrGenerateKnockoutMatchEvents(match, leg);

        List<MatchLineupDto.PlayerDto> homePlayers = getEuropeanPlayersForTeam(
                legDetails.getHomeLeagueSlug(),
                legDetails.getHomeClubSlug(),
                legDetails.getHomeFormation(),
                events
        );

        List<MatchLineupDto.PlayerDto> awayPlayers = getEuropeanPlayersForTeam(
                legDetails.getAwayLeagueSlug(),
                legDetails.getAwayClubSlug(),
                legDetails.getAwayFormation(),
                events
        );

        return new MatchLineupDto(
                homePlayers.stream().limit(11).toList(),
                awayPlayers.stream().limit(11).toList(),
                homePlayers.stream().skip(11).limit(9).toList(),
                awayPlayers.stream().skip(11).limit(9).toList()
        );
    }

    private List<KnockoutLegDetailsView> buildKnockoutLegDetails(EuropeanKnockoutMatch match) {
        if ("FINAL".equals(match.getRound())) {
            return List.of(createKnockoutLegDetails(
                    match,
                    "FINAL",
                    "Final Match",
                    match.getHomeTeam(),
                    match.getAwayTeam(),
                    match.getHomeLeagueSlug(),
                    match.getAwayLeagueSlug(),
                    match.getHomeClubSlug(),
                    match.getAwayClubSlug(),
                    match.getHomeGoals(),
                    match.getAwayGoals(),
                    false
            ));
        }

        return List.of(
                createKnockoutLegDetails(
                        match,
                        "FIRST_LEG",
                        "First Match",
                        match.getHomeTeam(),
                        match.getAwayTeam(),
                        match.getHomeLeagueSlug(),
                        match.getAwayLeagueSlug(),
                        match.getHomeClubSlug(),
                        match.getAwayClubSlug(),
                        match.getFirstLegHomeGoals(),
                        match.getFirstLegAwayGoals(),
                        false
                ),
                createKnockoutLegDetails(
                        match,
                        "SECOND_LEG",
                        "Second Match",
                        match.getAwayTeam(),
                        match.getHomeTeam(),
                        match.getAwayLeagueSlug(),
                        match.getHomeLeagueSlug(),
                        match.getAwayClubSlug(),
                        match.getHomeClubSlug(),
                        match.getSecondLegHomeGoals(),
                        match.getSecondLegAwayGoals(),
                        true
                )
        );
    }

    private KnockoutLegDetailsView createKnockoutLegDetails(EuropeanKnockoutMatch match,
                                                            String legKey,
                                                            String label,
                                                            String homeTeam,
                                                            String awayTeam,
                                                            String homeLeagueSlug,
                                                            String awayLeagueSlug,
                                                            String homeClubSlug,
                                                            String awayClubSlug,
                                                            Integer homeGoals,
                                                            Integer awayGoals,
                                                            boolean secondLeg) {
        Random random = new Random((match.getId() == null ? 17L : match.getId()) * 31 + legKey.hashCode());

        int possessionHome = 42 + random.nextInt(17);
        int possessionAway = 100 - possessionHome;
        int shotsHome = 6 + random.nextInt(16);
        int shotsAway = 6 + random.nextInt(16);
        int yellowHome = random.nextInt(5);
        int yellowAway = random.nextInt(5);
        int redHome = random.nextInt(20) == 0 ? 1 : 0;
        int redAway = random.nextInt(20) == 0 ? 1 : 0;
        List<String> formations = List.of("4-4-2", "4-3-3", "4-2-3-1", "4-1-2-1-2", "3-5-2", "3-4-3", "3-4-2-1", "5-3-2", "5-4-1");

        return new KnockoutLegDetailsView(
                match.getId(),
                legKey,
                label,
                homeTeam,
                awayTeam,
                homeLeagueSlug,
                awayLeagueSlug,
                homeClubSlug,
                awayClubSlug,
                safeInteger(homeGoals),
                safeInteger(awayGoals),
                match.getStatus(),
                homeTeam + " Stadium",
                secondLeg ? match.getSecondLegReferee() : match.getFirstLegReferee(),
                secondLeg ? match.getSecondLegDate() : match.getFirstLegDate(),
                possessionHome,
                possessionAway,
                shotsHome,
                shotsAway,
                Math.min(shotsHome, 2 + random.nextInt(8)),
                Math.min(shotsAway, 2 + random.nextInt(8)),
                random.nextInt(9),
                random.nextInt(9),
                6 + random.nextInt(13),
                6 + random.nextInt(13),
                random.nextInt(5),
                random.nextInt(5),
                yellowHome,
                yellowAway,
                redHome,
                redAway,
                320 + random.nextInt(380),
                320 + random.nextInt(380),
                74 + random.nextInt(18),
                74 + random.nextInt(18),
                Math.round((0.4 + random.nextDouble() * 3.2) * 10.0) / 10.0,
                Math.round((0.4 + random.nextDouble() * 3.2) * 10.0) / 10.0,
                formations.get(random.nextInt(formations.size())),
                formations.get(random.nextInt(formations.size()))
        );
    }
    private Integer safeInteger(Integer value) {
        return value == null ? 0 : value;
    }


    private record EuropeanPlayerStatView(
            String playerName,
            String countryCode,
            String leagueSlug,
            String clubSlug,
            String clubName,
            Long value
    ) {}

    private EuropeanPlayerStatView buildPlayerStat(Long playerId,
                                                   String fallbackName,
                                                   Long value) {

        GeneratedPlayer player = null;

        if (playerId != null) {
            player = generatedPlayerRepository.findById(playerId).orElse(null);
        }

        if (player == null) {
            player = generatedPlayerRepository.findAll()
                    .stream()
                    .filter(p -> playerName(p).equalsIgnoreCase(fallbackName))
                    .findFirst()
                    .orElse(null);
        }

        if (player == null) {
            return new EuropeanPlayerStatView(
                    fallbackName,
                    null,
                    null,
                    null,
                    null,
                    value
            );
        }

        return new EuropeanPlayerStatView(
                playerName(player),
                player.getCountryCode(),
                player.getLeagueSlug(),
                player.getClubSlug(),
                player.getCurrentClub(),
                value
        );
    }

    private List<EuropeanPlayerStatView> buildEventPlayerStats(
            List<EuropeanMatchEvent> events,
            List<String> eventTypes,
            boolean useRelatedPlayer
    ) {

        return events.stream()
                .filter(e -> eventTypes.contains(e.getEventType()))
                .filter(e -> useRelatedPlayer
                        ? e.getRelatedPlayerId() != null
                        : e.getPlayerId() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> useRelatedPlayer
                                ? e.getRelatedPlayerId()
                                : e.getPlayerId(),
                        java.util.stream.Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {

                    EuropeanMatchEvent sample = events.stream()
                            .filter(e ->
                                    useRelatedPlayer
                                            ? entry.getKey().equals(e.getRelatedPlayerId())
                                            : entry.getKey().equals(e.getPlayerId()))
                            .findFirst()
                            .orElse(null);

                    return buildPlayerStat(
                            entry.getKey(),
                            sample == null
                                    ? "Unknown player"
                                    : (useRelatedPlayer
                                    ? sample.getRelatedPlayerName()
                                    : sample.getPlayerName()),
                            entry.getValue()
                    );
                })
                .sorted(java.util.Comparator.comparing(EuropeanPlayerStatView::value).reversed())
                .limit(10)
                .toList();
    }


    private List<EuropeanPlayerStatView> calculateEuropeanCleanSheets(
            List<EuropeanMatch> matches
    ) {

        Map<Long, Long> cleanSheets = new HashMap<>();

        for (EuropeanMatch match : matches) {

            if (safeInteger(match.getAwayGoals()) == 0) {

                GeneratedPlayer goalkeeper =
                        findMainGoalkeeper(
                                match.getHomeLeagueSlug(),
                                match.getHomeClubSlug()
                        );

                if (goalkeeper != null) {
                    cleanSheets.merge(goalkeeper.getId(), 1L, Long::sum);
                }
            }

            if (safeInteger(match.getHomeGoals()) == 0) {

                GeneratedPlayer goalkeeper =
                        findMainGoalkeeper(
                                match.getAwayLeagueSlug(),
                                match.getAwayClubSlug()
                        );

                if (goalkeeper != null) {
                    cleanSheets.merge(goalkeeper.getId(), 1L, Long::sum);
                }
            }
        }

        return cleanSheets.entrySet().stream()
                .map(entry ->
                        buildPlayerStat(
                                entry.getKey(),
                                "Unknown goalkeeper",
                                entry.getValue()
                        ))
                .sorted(Comparator.comparing(EuropeanPlayerStatView::value).reversed())
                .limit(10)
                .toList();
    }

    private GeneratedPlayer findMainGoalkeeper(String leagueSlug,
                                               String clubSlug) {

        return generatedPlayerRepository
                .findByLeagueSlugAndClubSlug(leagueSlug, clubSlug)
                .stream()
                .filter(player -> matchesPosition(player, "goalkeeper"))
                .sorted(Comparator.comparing(
                        GeneratedPlayer::getMarketValue,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .findFirst()
                .orElse(null);
    }
}