package com.footballstats.controller;

import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.PlayerView;
import com.footballstats.model.RealTeam;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.repository.RealPlayerRepository;
import com.footballstats.repository.RealTeamRepository;
import com.footballstats.service.PlayerGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Profile("!mongodb")
public class PlayerController {

    private final PlayerGenerator playerGenerator = new PlayerGenerator();
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final RealPlayerRepository realPlayerRepository;
    private final RealTeamRepository realTeamRepository;

    public PlayerController(
            GeneratedPlayerRepository generatedPlayerRepository,
            RealPlayerRepository realPlayerRepository,
            RealTeamRepository realTeamRepository
    ) {
        this.generatedPlayerRepository = generatedPlayerRepository;
        this.realPlayerRepository = realPlayerRepository;
        this.realTeamRepository = realTeamRepository;
    }

    @GetMapping("/players/real")
    public String realPlayers(Model model,
                              @RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "") String league,
                              @RequestParam(defaultValue = "") String club,
                              @RequestParam(defaultValue = "") String position,
                              @RequestParam(defaultValue = "") String nationality,
                              @RequestParam(defaultValue = "nameAsc") String sort) {

        List<PlayerView> players = realPlayerRepository.findAll()
                .stream()
                .map(PlayerView::fromReal)
                .toList();

        addPlayersModel(model, players, "real", q, league, club, position, nationality, sort);
        return "players";
    }

    @GetMapping("/players/generated")
    public String generatedPlayers(Model model,
                                   @RequestParam(defaultValue = "") String q,
                                   @RequestParam(defaultValue = "") String league,
                                   @RequestParam(defaultValue = "") String club,
                                   @RequestParam(defaultValue = "") String position,
                                   @RequestParam(defaultValue = "") String nationality,
                                   @RequestParam(defaultValue = "nameAsc") String sort) {

        List<PlayerView> players = generatedPlayerRepository.findAll()
                .stream()
                .map(PlayerView::fromGenerated)
                .toList();

        addPlayersModel(model, players, "generated", q, league, club, position, nationality, sort);
        return "players";
    }

    @GetMapping("/players/generated/generate-for-real-clubs")
    public String generateForRealClubs() {
        List<RealTeam> teams = realTeamRepository.findAll();

        for (RealTeam team : teams) {
            boolean alreadyGenerated = generatedPlayerRepository.existsByLeagueSlugAndClubSlug(
                    team.getLeagueSlug(),
                    team.getClubSlug()
            );

            if (alreadyGenerated) continue;

            List<GeneratedPlayer> squad = playerGenerator.generateSquadForTeam(team);
            generatedPlayerRepository.saveAll(squad);
        }

        return "redirect:/players/generated";
    }

    @GetMapping("/players/generated/regenerate")
    public String regenerateGeneratedPlayers() {
        generatedPlayerRepository.deleteAll();
        return "redirect:/players/generated/generate-for-real-clubs";
    }

    private void addPlayersModel(Model model,
                                 List<PlayerView> allPlayers,
                                 String currentPage,
                                 String q,
                                 String league,
                                 String club,
                                 String position,
                                 String nationality,
                                 String sort) {

        boolean hasFilters =
                !isBlank(q) ||
                        !isBlank(league) ||
                        !isBlank(club) ||
                        !isBlank(position) ||
                        !isBlank(nationality);

        List<PlayerView> filtered = hasFilters
                ? allPlayers.stream()
                .filter(p -> isBlank(q) || contains(p.getFullName(), q))
                .filter(p -> isBlank(league) || equalsValue(p.getLeagueSlug(), league))
                .filter(p -> isBlank(club) || equalsValue(p.getClubSlug(), club))
                .filter(p -> isBlank(position) || equalsValue(p.getPosition(), position))
                .filter(p -> isBlank(nationality) || equalsValue(p.getNationality(), nationality))
                .sorted(getPlayerGroupComparator(club, league, sort))
                .toList()
                : List.of();

        model.addAttribute("players", filtered);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("q", q);
        model.addAttribute("league", league);
        model.addAttribute("club", club);
        model.addAttribute("position", position);
        model.addAttribute("nationality", nationality);
        model.addAttribute("sort", sort);
        model.addAttribute("leagues", distinct(allPlayers.stream()
                .map(PlayerView::getLeagueSlug)
                .sorted()
                .toList()));
        model.addAttribute("clubs", distinct(allPlayers.stream()
                .map(PlayerView::getClubSlug)
                .sorted()
                .toList()));
        model.addAttribute("positions", distinct(allPlayers.stream().map(PlayerView::getPosition).toList()));
        model.addAttribute("nationalities", distinct(allPlayers.stream().map(PlayerView::getNationality).toList()));
        model.addAttribute("players", filtered);

        Map<String, String> clubLeagueMap = allPlayers.stream()
                .collect(Collectors.toMap(
                        PlayerView::getClubSlug,
                        PlayerView::getLeagueSlug,
                        (a, b) -> a
                ));

        if ("real".equals(currentPage)) {
            model.addAttribute("allPlayersSearch", allPlayers);
        }
        model.addAttribute("clubLeagueMap", clubLeagueMap);
        model.addAttribute("hasGeneratedPlayers", generatedPlayerRepository.count() > 0);
    }

    private Comparator<PlayerView> getComparator(String sort) {
        return switch (sort) {
            case "ageAsc" -> Comparator.comparing(p -> nullSafeInt(p.getAge()));
            case "ageDesc" -> Comparator.comparing((PlayerView p) -> nullSafeInt(p.getAge())).reversed();
            case "valueAsc" -> Comparator.comparing(p -> nullSafeMoney(p.getMarketValue()));
            case "valueDesc" -> Comparator.comparing((PlayerView p) -> nullSafeMoney(p.getMarketValue())).reversed();
            case "clubAsc" -> Comparator.comparing(p -> nullSafeString(p.getCurrentClub()));
            case "clubDesc" -> Comparator.comparing((PlayerView p) -> nullSafeString(p.getCurrentClub())).reversed();
            case "nameDesc" -> Comparator.comparing((PlayerView p) -> nullSafeString(p.getFullName())).reversed();
            default -> Comparator.comparing(p -> nullSafeString(p.getFullName()));
        };
    }

    private Comparator<PlayerView> getPlayerGroupComparator(String club, String league, String sort) {

        Comparator<PlayerView> defaultComparator = getComparator(sort);

        boolean hasClub = !isBlank(club);
        boolean hasLeague = !isBlank(league);

        if (hasClub) {
            return Comparator
                    .comparingInt((PlayerView p) -> positionOrder(p.getPosition()))
                    .thenComparing(defaultComparator);
        }

        if (hasLeague) {
            return Comparator
                    .comparing(PlayerView::getClubSlug, String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(p -> positionOrder(p.getPosition()))
                    .thenComparing(defaultComparator);
        }

        return defaultComparator;
    }

    private int positionOrder(String position) {
        if (position == null) return 99;
        String p = position.toLowerCase();

        if (p.contains("goalkeeper")) return 1;
        if (p.contains("back") || p.contains("defender") || p.contains("centre-back")) return 2;
        if (p.contains("midfield")) return 3;
        if (p.contains("winger") || p.contains("forward") || p.contains("striker") || p.contains("attack")) return 4;

        return 99;
    }

    private List<String> distinct(List<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .filter(v -> !v.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q.toLowerCase());
    }

    private boolean equalsValue(String value, String expected) {
        return value != null && value.equals(expected);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String nullSafeString(String value) {
        return value == null ? "" : value;
    }

    private int nullSafeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal nullSafeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}