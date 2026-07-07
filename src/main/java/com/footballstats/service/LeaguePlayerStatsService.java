package com.footballstats.service;

import com.footballstats.dto.LeaguePlayerStatsRow;
import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.Match;
import com.footballstats.model.MatchEvent;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.repository.MatchEventRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeaguePlayerStatsService {

    private final MatchEventRepository matchEventRepository;
    private final GeneratedPlayerRepository generatedPlayerRepository;

    public LeaguePlayerStatsService(MatchEventRepository matchEventRepository,
                                    GeneratedPlayerRepository generatedPlayerRepository) {

        this.matchEventRepository = matchEventRepository;
        this.generatedPlayerRepository = generatedPlayerRepository;
    }

    public List<LeaguePlayerStatsRow> calculate(List<Match> matches) {

        Map<Long, LeaguePlayerStatsRow> stats = new HashMap<>();

        for (Match match : matches) {

            List<MatchEvent> events =
                    matchEventRepository.findByMatchIdOrderByEventMinuteAsc(match.getId());

            for (MatchEvent event : events) {

                if ("GOAL".equals(event.getEventType())
                        || "PENALTY_GOAL".equals(event.getEventType())) {

                    addGoal(stats, event.getPlayerId());
                }

                if ("GOAL".equals(event.getEventType())
                        && event.getRelatedPlayerId() != null) {

                    addAssist(stats, event.getRelatedPlayerId());
                }
            }
        }

        return stats.values().stream()
                .sorted(Comparator
                        .comparingInt(LeaguePlayerStatsRow::getGoals)
                        .thenComparingInt(LeaguePlayerStatsRow::getAssists)
                        .reversed()
                )
                .toList();
    }

    private void addGoal(Map<Long, LeaguePlayerStatsRow> stats,
                         Long playerId) {

        if (playerId == null) {
            return;
        }

        LeaguePlayerStatsRow row =
                stats.computeIfAbsent(playerId, this::createRow);

        row.addGoal();
    }

    private void addAssist(Map<Long, LeaguePlayerStatsRow> stats,
                           Long playerId) {

        if (playerId == null) {
            return;
        }

        LeaguePlayerStatsRow row =
                stats.computeIfAbsent(playerId, this::createRow);

        row.addAssist();
    }

    private LeaguePlayerStatsRow createRow(Long playerId) {

        GeneratedPlayer player =
                generatedPlayerRepository.findById(playerId)
                        .orElse(null);

        if (player == null) {
            return new LeaguePlayerStatsRow(
                    playerId,
                    "Unknown",
                    "Unknown",
                    ""
            );
        }

        return new LeaguePlayerStatsRow(
                playerId,
                player.getFirstName() + " " + player.getLastName(),
                formatClubName(player.getClubSlug()),
                player.getCountryCode()
        );
    }

    private String formatClubName(String slug) {

        if (slug == null || slug.isBlank()) {
            return "Unknown";
        }

        String[] parts = slug.replace("-", " ").split(" ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {

            if (!part.isBlank()) {

                result.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}