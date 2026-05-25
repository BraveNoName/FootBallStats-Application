package com.footballstats.repository;

import com.footballstats.model.GeneratedPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneratedPlayerRepository extends JpaRepository<GeneratedPlayer, Long> {

    List<GeneratedPlayer> findByLeagueSlugAndClubSlug(String leagueSlug, String clubSlug);

    boolean existsByLeagueSlugAndClubSlug(String leagueSlug, String clubSlug);

    boolean existsByClubSlug(String clubSlug);

    List<GeneratedPlayer> findByClubSlug(String clubSlug);
}