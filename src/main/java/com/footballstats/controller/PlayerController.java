package com.footballstats.controller;
import com.footballstats.service.PlayerGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlayerController {

    private final PlayerGenerator playerGenerator = new PlayerGenerator();

    @GetMapping("/players/real")
    public String realPlayers(Model model) {

        model.addAttribute("players", playerGenerator.generatePlayers(25));
        model.addAttribute("currentPage", "real");

        return "players";
    }

    @GetMapping("/players/generated")
    public String generatedPlayers(Model model) {

        model.addAttribute("players", playerGenerator.generatePlayers(1000));
        model.addAttribute("currentPage", "generated");

        return "players";
    }


}
