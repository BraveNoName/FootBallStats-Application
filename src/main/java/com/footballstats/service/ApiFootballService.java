package com.footballstats.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApiFootballService {

    private final WebClient webClient;

    public ApiFootballService(
            WebClient.Builder webClientBuilder,
            @Value("${api.football.base-url}") String baseUrl,
            @Value("${api.football.key}") String apiKey
    ) {

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-key", apiKey)
                .build();
    }

    public Map<String, Object> getTeamFromPremierLeague(String teamName) {

        Map<String, Object> result = webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/teams")

                        .queryParam("league", 39)

                        .queryParam("season", 2024)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();

        List<Map<String, Object>> response =
                (List<Map<String, Object>>) result.get("response");

        return response.stream()

                .filter(teamWrapper -> {

                    Map<String, Object> team =
                            (Map<String, Object>) teamWrapper.get("team");

                    String apiName =
                            normalize(team.get("name").toString());

                    String localName =
                            normalize(teamName);

                    return apiName.equals(localName)
                            || apiName.contains(localName)
                            || localName.contains(apiName);
                })

                .findFirst()

                .orElse(null);
    }

    public Map<String, Object> getSquad(Integer teamId) {

        return webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/players/squads")

                        .queryParam("team", teamId)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();
    }

    public List<Map<String, Object>> getAllPlayersStats(Integer teamId) {

        List<Map<String, Object>> allPlayers = new ArrayList<>();

        int currentPage = 1;
        int totalPages = 1;

        do {

            final int page = currentPage;

            Map<String, Object> result = webClient.get()

                    .uri(uriBuilder -> uriBuilder

                            .path("/players")

                            .queryParam("team", teamId)

                            .queryParam("season", 2024)

                            .queryParam("page", page)

                            .build())

                    .retrieve()

                    .bodyToMono(Map.class)

                    .block();

            List<Map<String, Object>> response =
                    (List<Map<String, Object>>) result.get("response");

            if (response != null) {
                allPlayers.addAll(response);
            }

            Map<String, Object> paging =
                    (Map<String, Object>) result.get("paging");

            if (paging != null && paging.get("total") != null) {

                totalPages =
                        (Integer) paging.get("total");
            }

            currentPage++;

        } while (currentPage <= totalPages);

        return allPlayers;
    }

    public Map<String, Object> getPremierLeagueStandings() {

        return webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/standings")

                        .queryParam("league", 39)

                        .queryParam("season", 2024)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();
    }

    public Map<String, Object> getPremierLeagueTopScorers() {

        return webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/players/topscorers")

                        .queryParam("league", 39)

                        .queryParam("season", 2024)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();
    }

    public Map<String, Object> getPlayerTransfers(Integer playerId) {

        return webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/transfers")

                        .queryParam("player", playerId)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();
    }

    public Map<String, Object> getPlayerTrophies(Integer playerId) {

        return webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/trophies")

                        .queryParam("player", playerId)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();
    }

    public Map<String, Object> getPlayerInjuries(Integer playerId) {

        return webClient.get()

                .uri(uriBuilder -> uriBuilder

                        .path("/injuries")

                        .queryParam("player", playerId)

                        .build())

                .retrieve()

                .bodyToMono(Map.class)

                .block();
    }

    private String normalize(String name) {

        return name.toLowerCase()

                .replace("afc", "")

                .replace("fc", "")

                .replace("&", "and")

                .replace(".", "")

                .replace(" ", "")

                .trim();
    }
}