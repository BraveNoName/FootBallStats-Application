package com.footballstats.model;

import jakarta.persistence.*;

@Entity
@Table(name = "competitions")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String slug;
    private String season;

    @Enumerated(EnumType.STRING)
    private CompetitionType type;

    public Competition() {
    }

    public Competition(String name, String slug, String season, CompetitionType type) {
        this.name = name;
        this.slug = slug;
        this.season = season;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getSeason() {
        return season;
    }

    public CompetitionType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setType(CompetitionType type) {
        this.type = type;
    }
}