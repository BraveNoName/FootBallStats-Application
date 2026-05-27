package com.footballstats.dto;

import java.util.List;

public class MatchLineupDto {

    private List<PlayerDto> homeLineup;
    private List<PlayerDto> awayLineup;
    private List<PlayerDto> homeBench;
    private List<PlayerDto> awayBench;

    public MatchLineupDto(List<PlayerDto> homeLineup,
                          List<PlayerDto> awayLineup,
                          List<PlayerDto> homeBench,
                          List<PlayerDto> awayBench) {
        this.homeLineup = homeLineup;
        this.awayLineup = awayLineup;
        this.homeBench = homeBench;
        this.awayBench = awayBench;
    }

    public List<PlayerDto> getHomeLineup() { return homeLineup; }
    public List<PlayerDto> getAwayLineup() { return awayLineup; }
    public List<PlayerDto> getHomeBench() { return homeBench; }
    public List<PlayerDto> getAwayBench() { return awayBench; }

    public static class PlayerDto {

        private Long id;
        private String name;
        private String position;
        private Integer shirtNumber;
        private String imageUrl;
        private String countryCode;
        private List<EventIconDto> events;

        public PlayerDto(Long id,
                         String name,
                         String position,
                         Integer shirtNumber,
                         String imageUrl,
                         String countryCode,
                         List<EventIconDto> events) {
            this.id = id;
            this.name = name;
            this.position = position;
            this.shirtNumber = shirtNumber;
            this.imageUrl = imageUrl;
            this.countryCode = countryCode;
            this.events = events;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getPosition() { return position; }
        public Integer getShirtNumber() { return shirtNumber; }
        public String getImageUrl() { return imageUrl; }
        public String getCountryCode() { return countryCode; }
        public List<EventIconDto> getEvents() { return events; }
    }

    public static class EventIconDto {

        private String iconUrl;
        private String eventType;
        private Integer minute;

        public EventIconDto(String iconUrl, String eventType, Integer minute) {
            this.iconUrl = iconUrl;
            this.eventType = eventType;
            this.minute = minute;
        }

        public String getIconUrl() { return iconUrl; }
        public String getEventType() { return eventType; }
        public Integer getMinute() { return minute; }
    }
}