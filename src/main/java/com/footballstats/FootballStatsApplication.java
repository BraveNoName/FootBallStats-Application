package com.footballstats;

import com.footballstats.performance.mongo.MongoPerformanceApplication;
import com.footballstats.performance.redis.RedisPerformanceApplication;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;

public class FootballStatsApplication {

	public static void main(String[] args) {
		String activeProfile = Arrays.stream(args)
				.filter(arg -> arg.startsWith("--spring.profiles.active="))
				.findFirst()
				.map(arg -> arg.replace("--spring.profiles.active=", ""))
				.orElse(System.getenv("SPRING_PROFILES_ACTIVE"));

		if ("mongodb".equalsIgnoreCase(activeProfile)) {
			SpringApplication.run(MongoPerformanceApplication.class, args);
		} else if ("redis".equalsIgnoreCase(activeProfile)) {
			SpringApplication.run(RedisPerformanceApplication.class, args);
		} else {
			SpringApplication.run(FootballStatsRelationalApplication.class, args);
		}
	}
}