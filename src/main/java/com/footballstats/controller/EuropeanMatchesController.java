package com.footballstats.controller;

import com.footballstats.service.europe.EuropeanCompetitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EuropeanMatchesController {

    private final EuropeanCompetitionService europeanCompetitionService;

    public EuropeanMatchesController(EuropeanCompetitionService europeanCompetitionService) {
        this.europeanCompetitionService = europeanCompetitionService;
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

        model.addAttribute("competition", competition);
        model.addAttribute("competitionName", competitionName);
        model.addAttribute("competitionLogo", logo);

        return "european_matches";
    }

    @PostMapping("/europe/generate-participants")
    public String generateEuropeanParticipants() {
        europeanCompetitionService.generateParticipantsFromDomesticTables("2025/2026");

        return "redirect:/matches";
    }

    @PostMapping("/europe/{competition}/generate-league-phase")
    public String generateEuropeanLeaguePhase(@PathVariable String competition) {
        europeanCompetitionService.generateLeaguePhaseMatches("2025/2026", competition);

        return "redirect:/matches/europe/" + competition;
    }
}