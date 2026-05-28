package com.footballstats.repository;

import com.footballstats.model.EuropeanCompetitionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EuropeanCompetitionParticipantRepository
        extends JpaRepository<EuropeanCompetitionParticipant, Long> {

    List<EuropeanCompetitionParticipant> findBySeasonAndCompetitionSlugOrderByPotNumberAscUefaRankingPointsDesc(
            String season,
            String competitionSlug
    );

    boolean existsBySeasonAndCompetitionSlug(String season, String competitionSlug);
}