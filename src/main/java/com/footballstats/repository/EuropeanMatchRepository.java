package com.footballstats.repository;

import com.footballstats.model.EuropeanMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EuropeanMatchRepository extends JpaRepository<EuropeanMatch, Long> {

    boolean existsBySeasonAndCompetitionSlug(String season, String competitionSlug);

    List<EuropeanMatch> findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(
            String season,
            String competitionSlug
    );

    List<EuropeanMatch> findBySeasonAndCompetitionSlugAndRoundNumberOrderByMatchDateAsc(
            String season,
            String competitionSlug,
            Integer roundNumber
    );

    void deleteBySeasonAndCompetitionSlug(String season, String competitionSlug);
}