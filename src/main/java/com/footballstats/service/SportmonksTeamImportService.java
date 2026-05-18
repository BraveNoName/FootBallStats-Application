package com.footballstats.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SportmonksTeamImportService {

    @Value("${sportmonks.api-token}")
    private String apiToken;

    @Value("${sportmonks.base-url}")
    private String baseUrl;

    public void importTeamsToJson() throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        StringBuilder allTeamsJson = new StringBuilder();
        allTeamsJson.append("[\n");

        int page = 1;
        boolean hasMore = true;
        boolean firstObject = true;

        while (hasMore) {
            String url = baseUrl + "/teams?page=" + page + "&api_token=" + apiToken;

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                break;
            }

            String data = response.substring(
                    response.indexOf("\"data\":[") + 8,
                    response.indexOf("],\"pagination\"")
            );

            if (!data.isBlank()) {
                if (!firstObject) {
                    allTeamsJson.append(",\n");
                }

                allTeamsJson.append(data);
                firstObject = false;
            }

            hasMore = response.contains("\"has_more\":true");
            page++;
        }

        allTeamsJson.append("\n]");

        Path path = Path.of("src/main/resources/data/teams-seed.json");

        Files.createDirectories(path.getParent());
        Files.writeString(path, allTeamsJson.toString());
    }

}