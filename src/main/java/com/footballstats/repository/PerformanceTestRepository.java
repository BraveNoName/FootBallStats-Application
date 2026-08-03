package com.footballstats.repository;

import com.footballstats.model.PerformanceTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceTestRepository extends JpaRepository<PerformanceTestEntity, Long> {
}


