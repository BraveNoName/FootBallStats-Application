package com.footballstats.repository;

import com.footballstats.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByLeagueOrderByRoundNumberAscMatchDateAsc(String league);

    boolean existsByLeague(String league);

    void deleteByLeague(String league);
}