package com.footballstats.repository;

import com.footballstats.model.RealTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RealTeamRepository extends JpaRepository<RealTeam, Long> {

    Optional<RealTeam> findByLeagueSlugAndClubSlug(String leagueSlug, String clubSlug);

    boolean existsByLeagueSlugAndClubSlug(String leagueSlug, String clubSlug);
}