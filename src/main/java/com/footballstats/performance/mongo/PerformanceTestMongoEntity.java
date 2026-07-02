package com.footballstats.performance.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "performance_test")
public class PerformanceTestMongoEntity {

    @Id
    private String id;

    private String testName;
    private Integer testValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Integer getTestValue() {
        return testValue;
    }

    public void setTestValue(Integer testValue) {
        this.testValue = testValue;
    }
}
