package com.footballstats.repository;

import com.footballstats.model.EuropeanMatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EuropeanMatchEventRepository extends JpaRepository<EuropeanMatchEvent, Long> {

    List<EuropeanMatchEvent> findByMatchIdOrderByEventMinuteAsc(Long matchId);

    List<EuropeanMatchEvent> findByKnockoutMatchIdOrderByEventMinuteAsc(Long knockoutMatchId);

    List<EuropeanMatchEvent> findByKnockoutMatchIdAndKnockoutLegOrderByEventMinuteAsc(Long knockoutMatchId, String knockoutLeg);

    void deleteByMatchIdIn(Collection<Long> matchIds);

    void deleteByKnockoutMatchIdIn(Collection<Long> knockoutMatchIds);
}