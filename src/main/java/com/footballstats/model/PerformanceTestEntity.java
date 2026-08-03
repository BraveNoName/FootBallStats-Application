package com.footballstats.model;
import jakarta.persistence.*;

@Entity
@Table(name = "performance_test_entity")
public class PerformanceTestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String testName;
    private Integer testValue;

    public Long getId() {return id;}
    public String getTestName() {return testName;}
    public void setTestName(String testName) {
        this.testName = testName;
    }
    public Integer getTestValue() {return testValue;}
    public void setTestValue(Integer testValue) {this.testValue = testValue;}
}


