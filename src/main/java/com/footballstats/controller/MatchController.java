package com.footballstats.controller;

import com.footballstats.model.Match;
import com.footballstats.repository.MatchRepository;
import com.footballstats.service.MatchGeneratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/matches")
public class MatchController {

    private final MatchRepository matchRepository;
    private final MatchGeneratorService matchGeneratorService;

    public MatchController(MatchRepository matchRepository,
                           MatchGeneratorService matchGeneratorService) {
        this.matchRepository = matchRepository;
        this.matchGeneratorService = matchGeneratorService;
    }

    @GetMapping
    public String matchesPage(
            @RequestParam(defaultValue = "poland") String league,
            Model model) {

        List<Match> matches =
                matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(league);

        model.addAttribute("matches", matches);
        model.addAttribute("league", league);
        model.addAttribute("leagues", getAvailableLeagues());

        return "matches";
    }

    @PostMapping("/generate")
    public String generateSeason(@RequestParam String league) {

        matchGeneratorService.generateLeagueSeason(league);

        return "redirect:/matches?league=" + league;
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