package com.footballstats.config;

import com.footballstats.model.Competition;
import com.footballstats.model.CompetitionType;
import com.footballstats.repository.CompetitionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile({"postgres", "mysql", "mssql", "oracle"})
public class CompetitionDataLoader implements CommandLineRunner {

    private final CompetitionRepository competitionRepository;

    public CompetitionDataLoader(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    @Override
    public void run(String... args) {
        addCompetition("Premier League", "england", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("La Liga", "spain", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Bundesliga", "germany", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Serie A", "italy", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Ligue 1", "france", "2025/2026", CompetitionType.LEAGUE);

        addCompetition("Liga Portugal", "portugal", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Eredivisie", "netherlands", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Jupiler Pro League", "belgium", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Scottish Premiership", "scotland", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Super Lig", "turkiye", "2025/2026", CompetitionType.LEAGUE);

        addCompetition("Ekstraklasa", "poland", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Bundesliga Austria", "austria", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("SuperSport HNL", "croatia", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Chance Liga", "czech-republic", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Superliga", "denmark", "2025/2026", CompetitionType.LEAGUE);

        addCompetition("Super League Greece", "greece", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Eliteserien", "norway", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Allsvenskan", "sweden", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Swiss Super League", "switzerland", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Super Liga Srbije", "serbia", "2025/2026", CompetitionType.LEAGUE);

        addCompetition("SuperLiga Romania", "romania", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("efbet Liga", "bulgaria", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Premier Liga Ukraine", "ukraine", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Premier Liga Russia", "russia", "2025/2026", CompetitionType.LEAGUE);
        addCompetition("Ligat HaAl", "israel", "2025/2026", CompetitionType.LEAGUE);

        addCompetition("UEFA Champions League", "champions-league", "2025/2026", CompetitionType.CHAMPIONS_LEAGUE);
        addCompetition("UEFA Europa League", "europa-league", "2025/2026", CompetitionType.EUROPA_LEAGUE);
        addCompetition("UEFA Conference League", "conference-league", "2025/2026", CompetitionType.CONFERENCE_LEAGUE);
    }

    private void addCompetition(String name, String slug, String season, CompetitionType type) {
        if (!competitionRepository.existsBySlug(slug)) {
            competitionRepository.save(new Competition(name, slug, season, type));
        }
    }
}