package com.footballstats.repository;

import com.footballstats.model.RealPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RealPlayerRepository extends JpaRepository<RealPlayer, Integer> {

    List<RealPlayer> findByLeagueSlugAndClubSlug(String leagueSlug, String clubSlug);
}