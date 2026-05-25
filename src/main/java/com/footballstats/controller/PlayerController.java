package com.footballstats.controller;

import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.Player;
import com.footballstats.model.RealTeam;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.repository.RealTeamRepository;
import com.footballstats.service.PlayerGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PlayerController {

    private final PlayerGenerator playerGenerator = new PlayerGenerator();
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final RealTeamRepository realTeamRepository;

    public PlayerController(
            GeneratedPlayerRepository generatedPlayerRepository,
            RealTeamRepository realTeamRepository
    ) {
        this.generatedPlayerRepository = generatedPlayerRepository;
        this.realTeamRepository = realTeamRepository;
    }

    @GetMapping("/players/real")
    public String realPlayers(Model model) {
        model.addAttribute("players", List.of());
        model.addAttribute("currentPage", "real");
        return "players";
    }

    @GetMapping("/players/generated")
    public String generatedPlayers(Model model) {
        model.addAttribute("players", generatedPlayerRepository.findAll());
        model.addAttribute("currentPage", "generated");
        return "players";
    }

    @GetMapping("/players/generated/generate-for-real-clubs")
    public String generateForRealClubs(
            @RequestParam(defaultValue = "25") int count
    ) {
        List<RealTeam> teams = realTeamRepository.findAll();

        for (RealTeam team : teams) {

            boolean alreadyGenerated =
                    generatedPlayerRepository.existsByLeagueSlugAndClubSlug(
                            team.getLeagueSlug(),
                            team.getClubSlug()
                    );

            if (alreadyGenerated) {
                continue;
            }

            List<Player> generated = playerGenerator.generatePlayers(count);

            for (Player player : generated) {
                GeneratedPlayer gp = new GeneratedPlayer();

                gp.setFirstName(player.getFirstName());
                gp.setLastName(player.getLastName());
                gp.setBirthDate(player.getBirthDate());
                gp.setAge(player.getAge());
                gp.setHeight(player.getHeight());
                gp.setNationality(player.getNationality());
                gp.setCountryCode(player.getCountryCode());
                gp.setPosition(player.getPosition());
                gp.setPreferredFoot(player.getPreferredFoot());
                gp.setShirtNumber(player.getShirtNumber());

                gp.setCurrentClub(team.getApiTeamName());
                gp.setClubSlug(team.getClubSlug());
                gp.setLeagueSlug(team.getLeagueSlug());
                gp.setTeamId(team.getApiTeamId());

                gp.setJoinedClubDate(player.getJoinedClubDate());
                gp.setContractUntil(player.getContractUntil());
                gp.setLastContractExtension(player.getLastContractExtension());
                gp.setMarketValue(player.getMarketValue());
                gp.setActive(player.isActive());
                gp.setImageUrl(player.getImageUrl());

                generatedPlayerRepository.save(gp);
            }
        }

        return "redirect:/players/generated";
    }

    @GetMapping("/players/generated/regenerate")
    public String regenerateGeneratedPlayers(
            @RequestParam(defaultValue = "25") int count
    ) {
        generatedPlayerRepository.deleteAll();
        return "redirect:/players/generated/generate-for-real-clubs?count=" + count;
    }
}