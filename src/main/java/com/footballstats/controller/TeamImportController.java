package com.footballstats.controller;

import com.footballstats.service.SportmonksTeamImportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TeamImportController {

    private final SportmonksTeamImportService sportmonksTeamImportService;

    public TeamImportController(SportmonksTeamImportService sportmonksTeamImportService) {
        this.sportmonksTeamImportService = sportmonksTeamImportService;
    }

    @GetMapping("/import/teams")
    public String importTeams() throws IOException {
        sportmonksTeamImportService.importTeamsToJson();
        return "Teams imported to src/main/resources/data/teams-seed.json";
    }
}