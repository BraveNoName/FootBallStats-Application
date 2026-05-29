package com.footballstats.controller;

import com.footballstats.model.EuropeanCompetitionParticipant;
import com.footballstats.model.EuropeanMatch;
import com.footballstats.repository.EuropeanMatchRepository;
import com.footballstats.service.europe.EuropeanCompetitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        return "european_matches";
    }

    @PostMapping("/europe/{competition}/generate")
    public String generateEuropeanCompetition(@PathVariable String competition) {
        europeanCompetitionService.generateParticipantsFromDomesticTables(SEASON);
        europeanCompetitionService.generateLeaguePhaseMatches(SEASON, competition);

        return "redirect:/matches/europe/" + competition;
    }
}