package com.footballstats.controller;

import com.footballstats.model.EuropeanCompetitionParticipant;
import com.footballstats.model.EuropeanMatch;
import com.footballstats.repository.EuropeanMatchRepository;
import com.footballstats.service.europe.EuropeanCompetitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class EuropeanMatchesController {

    private static final String SEASON = "2025/2026";

    private final EuropeanCompetitionService europeanCompetitionService;
    private final EuropeanMatchRepository europeanMatchRepository;

    public EuropeanMatchesController(EuropeanCompetitionService europeanCompetitionService,
                                     EuropeanMatchRepository europeanMatchRepository) {
        this.europeanCompetitionService = europeanCompetitionService;
        this.europeanMatchRepository = europeanMatchRepository;
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
        return europeanCompetition(competition, model);
    }

    @GetMapping("/europe/{competition}/knockout")
    public String knockout(@PathVariable String competition, Model model) {
        return europeanCompetition(competition, model);
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