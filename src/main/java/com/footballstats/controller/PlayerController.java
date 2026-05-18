package com.footballstats.controller;

import com.footballstats.model.Player;
import com.footballstats.service.PlayerGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PlayerController {

    private final PlayerGenerator playerGenerator = new PlayerGenerator();

    private List<Player> generatedPlayers;

    @GetMapping("/players/real")
    public String realPlayers(Model model) {

        model.addAttribute("players", new ArrayList<>());
        model.addAttribute("currentPage", "real");

        return "players";
    }

    @GetMapping("/players/generated")
    public String generatedPlayers(Model model) {

        if (generatedPlayers == null) {
            generatedPlayers = playerGenerator.generatePlayers(250);
        }

        model.addAttribute("players", generatedPlayers);
        model.addAttribute("currentPage", "generated");

        return "players";
    }

    @GetMapping("/players/generated/regenerate")
    public String regenerateGeneratedPlayers(
            @RequestParam(defaultValue = "250") int count
    ) {

        generatedPlayers = playerGenerator.generatePlayers(count);

        return "redirect:/players/generated";
    }
}