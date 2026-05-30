package com.footballstats.repository;

import com.footballstats.model.EuropeanKnockoutMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EuropeanKnockoutMatchRepository extends JpaRepository<EuropeanKnockoutMatch, Long> {

    boolean existsBySeasonAndCompetitionSlug(String season, String competitionSlug);

    List<EuropeanKnockoutMatch> findBySeasonAndCompetitionSlugOrderByBracketOrderAsc(
            String season,
            String competitionSlug
    );

    List<EuropeanKnockoutMatch> findBySeasonAndCompetitionSlugOrderByRoundOrderAscBracketOrderAsc(
            String season,
            String competitionSlug
    );

    void deleteBySeasonAndCompetitionSlug(String season, String competitionSlug);
}