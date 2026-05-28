package com.footballstats.repository;

import com.footballstats.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    Optional<Competition> findBySlug(String slug);

    boolean existsBySlug(String slug);
}