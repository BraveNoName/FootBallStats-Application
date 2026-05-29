package com.footballstats.repository;

import com.footballstats.model.EuropeanMatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EuropeanMatchEventRepository extends JpaRepository<EuropeanMatchEvent, Long> {

    List<EuropeanMatchEvent> findByMatchIdOrderByEventMinuteAsc(Long matchId);

    void deleteByMatchId(Long matchId);
}