package com.footballstats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EuropeanMatchesController {

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

        return "european-matches";
    }
}