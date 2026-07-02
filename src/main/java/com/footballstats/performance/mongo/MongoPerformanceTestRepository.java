package com.footballstats.performance.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

@Profile("mongodb")
public interface MongoPerformanceTestRepository extends MongoRepository<PerformanceTestMongoEntity, String> {
}
