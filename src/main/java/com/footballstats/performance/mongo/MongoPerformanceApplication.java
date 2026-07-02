package com.footballstats.performance.mongo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.footballstats.performance.mongo")
@EnableMongoRepositories(basePackages = "com.footballstats.performance.mongo")
public class MongoPerformanceApplication {
}