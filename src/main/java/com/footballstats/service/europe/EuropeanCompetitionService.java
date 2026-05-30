package com.footballstats.service.europe;

import com.footballstats.dto.EuropeanLeaguePhaseRoundView;
import com.footballstats.dto.LeagueTableRow;
import com.footballstats.model.EuropeanCompetitionParticipant;
import com.footballstats.model.EuropeanMatch;
import com.footballstats.model.GeneratedPlayer;
import com.footballstats.model.Match;
import com.footballstats.repository.EuropeanCompetitionParticipantRepository;
import com.footballstats.repository.EuropeanMatchRepository;
import com.footballstats.repository.GeneratedPlayerRepository;
import com.footballstats.repository.MatchRepository;
import com.footballstats.service.LeagueTableService;
import org.springframework.stereotype.Service;
import com.footballstats.dto.EuropeanLeagueTableRow;
import com.footballstats.model.EuropeanMatchEvent;
import com.footballstats.repository.EuropeanMatchEventRepository;
import java.math.BigDecimal;
import com.footballstats.service.MatchGeneratorService;
import org.springframework.transaction.annotation.Transactional;
import com.footballstats.model.Referee;
import com.footballstats.repository.RefereeRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EuropeanCompetitionService {

    private static final String CHAMPIONS_LEAGUE = "champions-league";
    private static final String EUROPA_LEAGUE = "europa-league";
    private static final String CONFERENCE_LEAGUE = "conference-league";

    private final EuropeanCompetitionParticipantRepository participantRepository;
    private final EuropeanMatchRepository europeanMatchRepository;
    private final EuropeanMatchEventRepository europeanMatchEventRepository;
    private final MatchRepository matchRepository;
    private final LeagueTableService leagueTableService;
    private final GeneratedPlayerRepository generatedPlayerRepository;
    private final MatchGeneratorService matchGeneratorService;
    private final RefereeRepository refereeRepository;

    public EuropeanCompetitionService(
            EuropeanCompetitionParticipantRepository participantRepository,
            EuropeanMatchRepository europeanMatchRepository,
            MatchRepository matchRepository,
            LeagueTableService leagueTableService,
            GeneratedPlayerRepository generatedPlayerRepository,
            EuropeanMatchEventRepository europeanMatchEventRepository,
            MatchGeneratorService matchGeneratorService,
            RefereeRepository refereeRepository
    )  {
        this.participantRepository = participantRepository;
        this.europeanMatchRepository = europeanMatchRepository;
        this.europeanMatchEventRepository = europeanMatchEventRepository;
        this.matchRepository = matchRepository;
        this.leagueTableService = leagueTableService;
        this.generatedPlayerRepository = generatedPlayerRepository;
        this.matchGeneratorService = matchGeneratorService;
        this.refereeRepository = refereeRepository;
    }

    public List<EuropeanCompetitionParticipant> getParticipants(String season, String competitionSlug) {
        return participantRepository
                .findBySeasonAndCompetitionSlugOrderByPotNumberAscUefaRankingPointsDesc(season, competitionSlug);
    }

    public void generateParticipantsFromDomesticTables(String season) {

        if (participantRepository.findAll().stream().anyMatch(p -> season.equals(p.getSeason()))) {
            return;
        }

        List<String> domesticLeagues = List.of(
                "england", "spain", "italy", "germany", "france", "netherlands",
                "portugal", "belgium", "turkiye", "scotland", "austria",
                "switzerland", "greece", "denmark", "poland", "sweden",
                "norway", "croatia", "serbia", "romania", "bulgaria",
                "ukraine", "czech-republic", "israel"
        );

        List<EuropeanCompetitionParticipant> allParticipants = new ArrayList<>();

        List<EuropeanCompetitionParticipant> directLM =
                getDirectTeams(season, domesticLeagues, CHAMPIONS_LEAGUE);

        List<EuropeanCompetitionParticipant> qualificationLM =
                new ArrayList<>(getQualificationTeams(season, domesticLeagues, CHAMPIONS_LEAGUE));

        qualificationLM.sort(Comparator.comparingInt(
                EuropeanCompetitionParticipant::getUefaRankingPoints).reversed());

        List<EuropeanCompetitionParticipant> championsLeague = new ArrayList<>(directLM);
        List<EuropeanCompetitionParticipant> droppedToEuropa = new ArrayList<>();

        for (EuropeanCompetitionParticipant participant : qualificationLM) {
            if (championsLeague.size() < 36 &&
                    canAddTeamFromLeague(championsLeague, CHAMPIONS_LEAGUE, participant)) {
                participant.setCompetitionSlug(CHAMPIONS_LEAGUE);
                championsLeague.add(participant);
            } else {
                droppedToEuropa.add(participant);
            }
        }

        trimTo36(championsLeague);
        assignPots(championsLeague);
        allParticipants.addAll(championsLeague);

        List<EuropeanCompetitionParticipant> directLE =
                getDirectTeams(season, domesticLeagues, EUROPA_LEAGUE);

        List<EuropeanCompetitionParticipant> qualificationLE =
                new ArrayList<>(getQualificationTeams(season, domesticLeagues, EUROPA_LEAGUE));

        qualificationLE.sort(Comparator.comparingInt(
                EuropeanCompetitionParticipant::getUefaRankingPoints).reversed());

        List<EuropeanCompetitionParticipant> europaLeague = new ArrayList<>(directLE);

        for (EuropeanCompetitionParticipant participant : droppedToEuropa) {
            if (canAddTeamFromLeague(europaLeague, EUROPA_LEAGUE, participant)) {
                participant.setCompetitionSlug(EUROPA_LEAGUE);
                europaLeague.add(participant);
            }
        }

        List<EuropeanCompetitionParticipant> droppedToConference = new ArrayList<>();

        for (EuropeanCompetitionParticipant participant : qualificationLE) {
            if (europaLeague.size() < 36 &&
                    canAddTeamFromLeague(europaLeague, EUROPA_LEAGUE, participant)) {
                participant.setCompetitionSlug(EUROPA_LEAGUE);
                europaLeague.add(participant);
            } else {
                droppedToConference.add(participant);
            }
        }

        trimTo36(europaLeague);
        assignPots(europaLeague);
        allParticipants.addAll(europaLeague);

        List<EuropeanCompetitionParticipant> directLK =
                getDirectTeams(season, domesticLeagues, CONFERENCE_LEAGUE);

        List<EuropeanCompetitionParticipant> qualificationLK =
                new ArrayList<>(getQualificationTeams(season, domesticLeagues, CONFERENCE_LEAGUE));

        qualificationLK.sort(Comparator.comparingInt(
                EuropeanCompetitionParticipant::getUefaRankingPoints).reversed());

        List<EuropeanCompetitionParticipant> conferenceLeague = new ArrayList<>(directLK);

        for (EuropeanCompetitionParticipant participant : droppedToConference) {
            if (canAddTeamFromLeague(conferenceLeague, CONFERENCE_LEAGUE, participant)) {
                participant.setCompetitionSlug(CONFERENCE_LEAGUE);
                conferenceLeague.add(participant);
            }
        }

        for (EuropeanCompetitionParticipant participant : qualificationLK) {
            if (conferenceLeague.size() < 36 &&
                    canAddTeamFromLeague(conferenceLeague, CONFERENCE_LEAGUE, participant)) {
                participant.setCompetitionSlug(CONFERENCE_LEAGUE);
                conferenceLeague.add(participant);
            }
        }

        trimTo36(conferenceLeague);

        fillMissingTeamsFromFreeClubs(
                conferenceLeague,
                season,
                CONFERENCE_LEAGUE,
                domesticLeagues,
                allParticipants
        );

        trimTo36(conferenceLeague);
        assignPots(conferenceLeague);
        allParticipants.addAll(conferenceLeague);

        participantRepository.saveAll(allParticipants);
    }

    private List<EuropeanCompetitionParticipant> getDirectTeams(
            String season,
            List<String> domesticLeagues,
            String competitionSlug
    ) {
        return domesticLeagues.stream()
                .flatMap(leagueSlug -> {
                    List<Match> matches = matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(leagueSlug);

                    if (matches.isEmpty()) {
                        return java.util.stream.Stream.empty();
                    }

                    List<LeagueTableRow> table = leagueTableService.calculateTable(matches, leagueSlug);

                    return table.stream()
                            .filter(row -> isDirectZone(row, competitionSlug))
                            .map(row -> createParticipantFromTableRow(season, competitionSlug, leagueSlug, row));
                })
                .toList();
    }

    private List<EuropeanCompetitionParticipant> getQualificationTeams(
            String season,
            List<String> domesticLeagues,
            String competitionSlug
    ) {
        return domesticLeagues.stream()
                .flatMap(leagueSlug -> {
                    List<Match> matches = matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(leagueSlug);

                    if (matches.isEmpty()) {
                        return java.util.stream.Stream.empty();
                    }

                    List<LeagueTableRow> table = leagueTableService.calculateTable(matches, leagueSlug);

                    return table.stream()
                            .filter(row -> isQualificationZone(row, competitionSlug))
                            .map(row -> createParticipantFromTableRow(season, competitionSlug, leagueSlug, row));
                })
                .toList();
    }

    private boolean isDirectZone(LeagueTableRow row, String competitionSlug) {
        String zone = row.getZone();

        if (zone == null) {
            return false;
        }

        return switch (competitionSlug) {
            case CHAMPIONS_LEAGUE -> zone.equals("CHAMPIONS_LEAGUE");
            case EUROPA_LEAGUE -> zone.equals("EUROPA_LEAGUE");
            case CONFERENCE_LEAGUE -> zone.equals("CONFERENCE_LEAGUE");
            default -> false;
        };
    }

    private boolean isQualificationZone(LeagueTableRow row, String competitionSlug) {
        String zone = row.getZone();

        if (zone == null) {
            return false;
        }

        return switch (competitionSlug) {
            case CHAMPIONS_LEAGUE -> zone.equals("CHAMPIONS_LEAGUE_QUALIFIERS");
            case EUROPA_LEAGUE -> zone.equals("EUROPA_LEAGUE_QUALIFIERS");
            case CONFERENCE_LEAGUE -> zone.equals("CONFERENCE_LEAGUE_QUALIFIERS");
            default -> false;
        };
    }

    private int getMaxTeamsPerLeague(String competitionSlug) {
        return switch (competitionSlug) {
            case CHAMPIONS_LEAGUE -> 5;
            case EUROPA_LEAGUE -> 4;
            case CONFERENCE_LEAGUE -> 3;
            default -> 3;
        };
    }

    private boolean canAddTeamFromLeague(
            List<EuropeanCompetitionParticipant> current,
            String competitionSlug,
            EuropeanCompetitionParticipant candidate
    ) {
        long count = current.stream()
                .filter(p -> p.getLeagueSlug().equals(candidate.getLeagueSlug()))
                .count();

        return count < getMaxTeamsPerLeague(competitionSlug);
    }

    private void trimTo36(List<EuropeanCompetitionParticipant> participants) {
        participants.sort(Comparator.comparingInt(
                EuropeanCompetitionParticipant::getUefaRankingPoints).reversed());

        while (participants.size() > 36) {
            participants.remove(participants.size() - 1);
        }
    }

    private void assignPots(List<EuropeanCompetitionParticipant> participants) {
        participants.sort(Comparator.comparingInt(
                EuropeanCompetitionParticipant::getUefaRankingPoints).reversed());

        for (int i = 0; i < participants.size(); i++) {
            participants.get(i).setPotNumber((i / 9) + 1);
        }

        if (!participants.isEmpty()) {
            participants.get(0).setTitleHolder(true);
            participants.get(0).setPotNumber(1);
        }
    }

    private EuropeanCompetitionParticipant createParticipantFromTableRow(
            String season,
            String competitionSlug,
            String leagueSlug,
            LeagueTableRow row
    ) {
        String clubName = row.getTeam();
        String clubSlug = createSlug(clubName);

        int rankingPoints = calculateClubStrength(leagueSlug, clubSlug);

        EuropeanCompetitionParticipant participant = new EuropeanCompetitionParticipant();

        participant.setSeason(season);
        participant.setCompetitionSlug(competitionSlug);
        participant.setClubName(clubName);
        participant.setClubSlug(clubSlug);
        participant.setLeagueSlug(leagueSlug);
        participant.setCountryCode(resolveCountryCodeByLeague(leagueSlug));
        participant.setUefaRankingPoints(rankingPoints);
        participant.setTitleHolder(false);

        return participant;
    }

    private String resolveCountryCodeByLeague(String leagueSlug) {
        return switch (leagueSlug) {
            case "england" -> "gb-eng";
            case "spain" -> "es";
            case "italy" -> "it";
            case "germany" -> "de";
            case "france" -> "fr";
            case "netherlands" -> "nl";
            case "portugal" -> "pt";
            case "belgium" -> "be";
            case "turkiye" -> "tr";
            case "scotland" -> "gb-sct";
            case "austria" -> "at";
            case "switzerland" -> "ch";
            case "greece" -> "gr";
            case "denmark" -> "dk";
            case "poland" -> "pl";
            case "sweden" -> "se";
            case "norway" -> "no";
            case "croatia" -> "hr";
            case "serbia" -> "rs";
            case "romania" -> "ro";
            case "bulgaria" -> "bg";
            case "ukraine" -> "ua";
            case "czech-republic" -> "cz";
            case "israel" -> "il";
            default -> null;
        };
    }

    private int calculateClubStrength(String leagueSlug, String clubSlug) {
        return generatedPlayerRepository
                .findByLeagueSlugAndClubSlug(leagueSlug, clubSlug)
                .stream()
                .map(GeneratedPlayer::getMarketValue)
                .filter(Objects::nonNull)
                .mapToInt(BigDecimal::intValue)
                .sum();
    }

    private String createSlug(String input) {
        if (input == null) {
            return "";
        }

        return input.toLowerCase()
                .replace("ą", "a")
                .replace("ć", "c")
                .replace("ę", "e")
                .replace("ł", "l")
                .replace("ń", "n")
                .replace("ó", "o")
                .replace("ś", "s")
                .replace("ź", "z")
                .replace("ż", "z")
                .replace("&", "and")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public List<EuropeanLeagueTableRow> calculateLeaguePhaseTable(String season, String competitionSlug) {
        List<EuropeanCompetitionParticipant> participants = getParticipants(season, competitionSlug);

        List<EuropeanMatch> matches = europeanMatchRepository
                .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(season, competitionSlug);

        Map<String, EuropeanLeagueTableRow> table = new LinkedHashMap<>();

        for (EuropeanCompetitionParticipant participant : participants) {
            String key = participant.getLeagueSlug() + "|" + participant.getClubSlug();
            table.put(key, new EuropeanLeagueTableRow(participant.getClubName(), participant.getLeagueSlug()));
        }

        for (EuropeanMatch match : matches) {
            if (!"FINISHED".equalsIgnoreCase(match.getStatus()) && !"PLAYED".equalsIgnoreCase(match.getStatus())) {
                continue;
            }

            String homeKey = match.getHomeLeagueSlug() + "|" + match.getHomeClubSlug();
            String awayKey = match.getAwayLeagueSlug() + "|" + match.getAwayClubSlug();

            EuropeanLeagueTableRow home = table.get(homeKey);
            EuropeanLeagueTableRow away = table.get(awayKey);

            if (home == null || away == null) {
                continue;
            }

            int homeGoals = match.getHomeGoals();
            int awayGoals = match.getAwayGoals();

            if (homeGoals > awayGoals) {
                home.addWin(homeGoals, awayGoals);
                away.addLoss(awayGoals, homeGoals);
            } else if (homeGoals < awayGoals) {
                home.addLoss(homeGoals, awayGoals);
                away.addWin(awayGoals, homeGoals);
            } else {
                home.addDraw(homeGoals, awayGoals);
                away.addDraw(awayGoals, homeGoals);
            }
        }

        List<EuropeanLeagueTableRow> rows = table.values()
                .stream()
                .sorted(Comparator
                        .comparingInt(EuropeanLeagueTableRow::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(EuropeanLeagueTableRow::getGoalDifference).reversed())
                        .thenComparing(Comparator.comparingInt(EuropeanLeagueTableRow::getGoalsFor).reversed())
                        .thenComparing(EuropeanLeagueTableRow::getTeam))
                .toList();

        for (int i = 0; i < rows.size(); i++) {
            if (i < 8) {
                rows.get(i).setZone("1/8");
            } else if (i < 24) {
                rows.get(i).setZone("PLAY_OFF");
            } else {
                rows.get(i).setZone("OUT");
            }
        }

        return rows;
    }

    private List<EuropeanLeagueTableRow> calculateLeaguePhaseTableFromMatches(
            String season,
            String competitionSlug,
            List<EuropeanMatch> matches
    ) {
        List<EuropeanCompetitionParticipant> participants = getParticipants(season, competitionSlug);

        Map<String, EuropeanLeagueTableRow> table = new LinkedHashMap<>();

        for (EuropeanCompetitionParticipant participant : participants) {
            String key = participant.getLeagueSlug() + "|" + participant.getClubSlug();
            table.put(key, new EuropeanLeagueTableRow(
                    participant.getClubName(),
                    participant.getLeagueSlug()
            ));
        }

        for (EuropeanMatch match : matches) {
            if (!"FINISHED".equalsIgnoreCase(match.getStatus()) &&
                    !"PLAYED".equalsIgnoreCase(match.getStatus())) {
                continue;
            }

            String homeKey = match.getHomeLeagueSlug() + "|" + match.getHomeClubSlug();
            String awayKey = match.getAwayLeagueSlug() + "|" + match.getAwayClubSlug();

            EuropeanLeagueTableRow home = table.get(homeKey);
            EuropeanLeagueTableRow away = table.get(awayKey);

            if (home == null || away == null) {
                continue;
            }

            int homeGoals = match.getHomeGoals();
            int awayGoals = match.getAwayGoals();

            if (homeGoals > awayGoals) {
                home.addWin(homeGoals, awayGoals);
                away.addLoss(awayGoals, homeGoals);
            } else if (homeGoals < awayGoals) {
                home.addLoss(homeGoals, awayGoals);
                away.addWin(awayGoals, homeGoals);
            } else {
                home.addDraw(homeGoals, awayGoals);
                away.addDraw(awayGoals, homeGoals);
            }
        }

        List<EuropeanLeagueTableRow> rows = table.values()
                .stream()
                .sorted(Comparator
                        .comparingInt(EuropeanLeagueTableRow::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(EuropeanLeagueTableRow::getGoalDifference).reversed())
                        .thenComparing(Comparator.comparingInt(EuropeanLeagueTableRow::getGoalsFor).reversed())
                        .thenComparing(EuropeanLeagueTableRow::getTeam))
                .toList();

        for (int i = 0; i < rows.size(); i++) {
            if (i < 8) {
                rows.get(i).setZone("1/8");
            } else if (i < 24) {
                rows.get(i).setZone("PLAY_OFF");
            } else {
                rows.get(i).setZone("OUT");
            }
        }

        return rows;
    }

    public List<EuropeanLeaguePhaseRoundView> buildLeaguePhaseRoundViews(
            String season,
            String competitionSlug
    ) {
        List<EuropeanMatch> allMatches = europeanMatchRepository
                .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(
                        season,
                        competitionSlug
                );

        List<Integer> rounds = allMatches.stream()
                .map(EuropeanMatch::getRoundNumber)
                .distinct()
                .sorted()
                .toList();

        List<EuropeanLeaguePhaseRoundView> roundViews = new ArrayList<>();

        for (Integer roundNumber : rounds) {

            List<EuropeanMatch> matchesUpToRound = allMatches.stream()
                    .filter(match -> match.getRoundNumber() <= roundNumber)
                    .toList();

            List<EuropeanMatch> matchesInRound = allMatches.stream()
                    .filter(match -> match.getRoundNumber() == roundNumber)
                    .toList();

            List<EuropeanLeagueTableRow> tableAfterRound =
                    calculateLeaguePhaseTableFromMatches(
                            season,
                            competitionSlug,
                            matchesUpToRound
                    );

            roundViews.add(
                    new EuropeanLeaguePhaseRoundView(
                            roundNumber,
                            tableAfterRound,
                            matchesInRound
                    )
            );
        }

        return roundViews;
    }

    public void generateLeaguePhaseMatches(String season, String competitionSlug) {
        if (europeanMatchRepository.existsBySeasonAndCompetitionSlug(season, competitionSlug)) {
            return;
        }

        List<EuropeanCompetitionParticipant> teams = getParticipants(season, competitionSlug);

        if (teams.size() != 36) {
            throw new RuntimeException("Competition must have exactly 36 teams. Found: " + teams.size());
        }

        List<EuropeanMatch> matches = createLeaguePhaseDraw(teams, season, competitionSlug);
        simulateEuropeanMatches(matches);

        List<EuropeanMatch> savedMatches = europeanMatchRepository.saveAll(matches);

        for (EuropeanMatch match : savedMatches) {
            europeanMatchEventRepository.saveAll(generateEuropeanMatchEvents(match));
        }
    }


    private List<EuropeanMatchEvent> generateEuropeanMatchEvents(EuropeanMatch match) {
        Random random = new Random(match.getId() == null ? System.nanoTime() : match.getId());

        EuropeanTeamMatchState homeState = createEuropeanTeamState(match, true);
        EuropeanTeamMatchState awayState = createEuropeanTeamState(match, false);

        List<EuropeanPlannedEvent> plannedEvents = new ArrayList<>();

        addEuropeanGoalPlans(plannedEvents, safeInt(match.getHomeGoals()), EuropeanPlannedEventType.HOME_GOAL, random);
        addEuropeanGoalPlans(plannedEvents, safeInt(match.getAwayGoals()), EuropeanPlannedEventType.AWAY_GOAL, random);

        addEuropeanCardPlans(plannedEvents, safeInt(match.getYellowCardsHome()), EuropeanPlannedEventType.HOME_YELLOW, random);
        addEuropeanCardPlans(plannedEvents, safeInt(match.getYellowCardsAway()), EuropeanPlannedEventType.AWAY_YELLOW, random);

        addEuropeanCardPlans(plannedEvents, safeInt(match.getRedCardsHome()), EuropeanPlannedEventType.HOME_RED, random);
        addEuropeanCardPlans(plannedEvents, safeInt(match.getRedCardsAway()), EuropeanPlannedEventType.AWAY_RED, random);

        addEuropeanSubstitutionPlans(plannedEvents, generateEuropeanSubstitutionsCount(random), EuropeanPlannedEventType.HOME_SUBSTITUTION, random);
        addEuropeanSubstitutionPlans(plannedEvents, generateEuropeanSubstitutionsCount(random), EuropeanPlannedEventType.AWAY_SUBSTITUTION, random);

        if (random.nextInt(100) < 18) {
            plannedEvents.add(new EuropeanPlannedEvent(1 + random.nextInt(90), EuropeanPlannedEventType.INJURY));
        }

        if (random.nextInt(100) < 15) {
            EuropeanPlannedEventType type = random.nextBoolean()
                    ? EuropeanPlannedEventType.HOME_PENALTY_MISSED
                    : EuropeanPlannedEventType.AWAY_PENALTY_MISSED;
            plannedEvents.add(new EuropeanPlannedEvent(1 + random.nextInt(90), type));
        }

        plannedEvents.sort(Comparator.comparingInt(EuropeanPlannedEvent::minute));

        List<EuropeanMatchEvent> events = new ArrayList<>();

        for (EuropeanPlannedEvent plannedEvent : plannedEvents) {
            handleEuropeanPlannedEvent(match, plannedEvent, homeState, awayState, events, random);
        }

        events.sort(Comparator
                .comparing(EuropeanMatchEvent::getEventMinute)
                .thenComparing(event -> event.getId() == null ? 0L : event.getId()));

        return events;
    }

    private EuropeanTeamMatchState createEuropeanTeamState(EuropeanMatch match, boolean homeTeam) {
        List<GeneratedPlayer> players = generateLineupForEuropeanMatch(
                homeTeam ? match.getHomeLeagueSlug() : match.getAwayLeagueSlug(),
                homeTeam ? match.getHomeClubSlug() : match.getAwayClubSlug(),
                homeTeam ? match.getHomeFormation() : match.getAwayFormation()
        );

        List<GeneratedPlayer> startingPlayers = players.stream().limit(11).toList();
        List<GeneratedPlayer> benchPlayers = players.stream().skip(11).limit(9).toList();

        return new EuropeanTeamMatchState(homeTeam, startingPlayers, benchPlayers);
    }

    private void addEuropeanGoalPlans(List<EuropeanPlannedEvent> events,
                                      int goals,
                                      EuropeanPlannedEventType type,
                                      Random random) {
        for (int i = 0; i < goals; i++) {
            int minute;
            int guard = 0;

            do {
                minute = 1 + random.nextInt(90);
                guard++;
            } while (hasEuropeanGoalInSameMinute(events, minute) && guard < 200);

            events.add(new EuropeanPlannedEvent(minute, type));
        }
    }

    private boolean hasEuropeanGoalInSameMinute(List<EuropeanPlannedEvent> events, int minute) {
        return events.stream()
                .anyMatch(event -> event.minute() == minute
                        && (event.type() == EuropeanPlannedEventType.HOME_GOAL
                        || event.type() == EuropeanPlannedEventType.AWAY_GOAL));
    }

    private void addEuropeanCardPlans(List<EuropeanPlannedEvent> events,
                                      int cards,
                                      EuropeanPlannedEventType type,
                                      Random random) {
        for (int i = 0; i < cards; i++) {
            events.add(new EuropeanPlannedEvent(generateEuropeanCardMinute(random), type));
        }
    }

    private void addEuropeanSubstitutionPlans(List<EuropeanPlannedEvent> events,
                                              int substitutions,
                                              EuropeanPlannedEventType type,
                                              Random random) {
        for (int i = 0; i < substitutions; i++) {
            events.add(new EuropeanPlannedEvent(generateEuropeanSubstitutionMinute(random), type));
        }
    }

    private void handleEuropeanPlannedEvent(EuropeanMatch match,
                                            EuropeanPlannedEvent event,
                                            EuropeanTeamMatchState homeState,
                                            EuropeanTeamMatchState awayState,
                                            List<EuropeanMatchEvent> events,
                                            Random random) {
        switch (event.type()) {
            case HOME_GOAL -> handleEuropeanGoal(match, event.minute(), homeState, awayState, events, random);
            case AWAY_GOAL -> handleEuropeanGoal(match, event.minute(), awayState, homeState, events, random);
            case HOME_YELLOW -> handleEuropeanYellowCard(match, event.minute(), homeState, events, random);
            case AWAY_YELLOW -> handleEuropeanYellowCard(match, event.minute(), awayState, events, random);
            case HOME_RED -> handleEuropeanRedCard(match, event.minute(), homeState, events, random);
            case AWAY_RED -> handleEuropeanRedCard(match, event.minute(), awayState, events, random);
            case HOME_SUBSTITUTION -> handleEuropeanTacticalSubstitution(match, event.minute(), homeState, events, random);
            case AWAY_SUBSTITUTION -> handleEuropeanTacticalSubstitution(match, event.minute(), awayState, events, random);
            case HOME_PENALTY_MISSED -> handleEuropeanPenaltyMissed(match, event.minute(), homeState, events, random);
            case AWAY_PENALTY_MISSED -> handleEuropeanPenaltyMissed(match, event.minute(), awayState, events, random);
            case INJURY -> handleEuropeanInjury(match, event.minute(), random.nextBoolean() ? homeState : awayState, events, random);
        }
    }

    private void handleEuropeanGoal(EuropeanMatch match,
                                    int minute,
                                    EuropeanTeamMatchState scoringState,
                                    EuropeanTeamMatchState opponentState,
                                    List<EuropeanMatchEvent> events,
                                    Random random) {
        int roll = random.nextInt(100);
        String eventType;
        String description;
        String iconUrl;
        GeneratedPlayer scorer;

        if (roll < 8) {
            eventType = "PENALTY_GOAL";
            description = "Penalty scored";
            iconUrl = "/Images/match_events/penalty.png";
            scorer = randomEuropeanFieldPlayerOnPitch(scoringState, random);
        } else if (roll < 12) {
            eventType = "OWN_GOAL";
            description = "Own goal";
            iconUrl = "/Images/match_events/own_goal.png";
            scorer = randomEuropeanFieldPlayerOnPitch(opponentState, random);
        } else {
            eventType = "GOAL";
            description = "Goal";
            iconUrl = "/Images/match_events/goal.png";
            scorer = randomEuropeanFieldPlayerOnPitch(scoringState, random);
        }

        if (scorer == null) {
            return;
        }

        EuropeanMatchEvent event = new EuropeanMatchEvent();
        event.setMatchId(match.getId());
        event.setEventMinute(minute);
        event.setEventType(eventType);
        event.setTeamSide(scoringState.teamSide);
        event.setPlayerId(scorer.getId());
        event.setPlayerName(playerName(scorer));
        event.setIconUrl(iconUrl);

        if ("GOAL".equals(eventType) && random.nextInt(100) < 75) {
            GeneratedPlayer assistant = randomEuropeanDifferentFieldPlayerOnPitch(scoringState, scorer, random);

            if (assistant != null) {
                event.setRelatedPlayerId(assistant.getId());
                event.setRelatedPlayerName(playerName(assistant));
                event.setDescription("Goal, assist");
            } else {
                event.setDescription(description);
            }
        } else {
            event.setDescription(description);
        }

        events.add(event);
    }

    private void handleEuropeanYellowCard(EuropeanMatch match,
                                          int minute,
                                          EuropeanTeamMatchState state,
                                          List<EuropeanMatchEvent> events,
                                          Random random) {
        GeneratedPlayer player = chooseEuropeanPlayerForYellow(state, random);

        if (player == null || player.getId() == null) {
            return;
        }

        int currentYellowCards = state.yellowCards.getOrDefault(player.getId(), 0);

        if (currentYellowCards >= 1) {
            handleEuropeanSecondYellow(match, minute, state, player, events);
            return;
        }

        state.yellowCards.put(player.getId(), 1);

        events.add(createEuropeanEvent(
                match,
                minute,
                "YELLOW_CARD",
                state.teamSide,
                player,
                null,
                "Yellow card",
                "/Images/match_events/yellow_card.png"
        ));
    }

    private GeneratedPlayer chooseEuropeanPlayerForYellow(EuropeanTeamMatchState state, Random random) {
        List<GeneratedPlayer> playersWithOneYellow = europeanPlayersOnPitch(state).stream()
                .filter(player -> player.getId() != null)
                .filter(player -> state.yellowCards.getOrDefault(player.getId(), 0) == 1)
                .toList();

        List<GeneratedPlayer> playersWithoutYellow = europeanPlayersOnPitch(state).stream()
                .filter(player -> player.getId() != null)
                .filter(player -> state.yellowCards.getOrDefault(player.getId(), 0) == 0)
                .toList();

        if (!playersWithOneYellow.isEmpty() && random.nextInt(100) < 25) {
            return randomPlayer(playersWithOneYellow, random);
        }

        if (!playersWithoutYellow.isEmpty()) {
            return randomPlayer(playersWithoutYellow, random);
        }

        return randomPlayer(playersWithOneYellow, random);
    }

    private void handleEuropeanSecondYellow(EuropeanMatch match,
                                            int minute,
                                            EuropeanTeamMatchState state,
                                            GeneratedPlayer player,
                                            List<EuropeanMatchEvent> events) {
        events.add(createEuropeanEvent(
                match,
                minute,
                "SECOND_YELLOW_RED_CARD",
                state.teamSide,
                player,
                null,
                "Second yellow card",
                "/Images/match_events/two_yellows_to_red_card.png"
        ));

        state.statuses.put(player.getId(), EuropeanPlayerMatchStatus.SENT_OFF);

        if (matchesPosition(player, "goalkeeper")) {
            handleEuropeanGoalkeeperProblem(match, minute, state, "Goalkeeper sent off", events, new Random(match.getId() + minute));
        }
    }

    private void handleEuropeanRedCard(EuropeanMatch match,
                                       int minute,
                                       EuropeanTeamMatchState state,
                                       List<EuropeanMatchEvent> events,
                                       Random random) {
        GeneratedPlayer player = randomEuropeanPlayerOnPitch(state, random);

        if (player == null || player.getId() == null) {
            return;
        }

        events.add(createEuropeanEvent(
                match,
                minute,
                "RED_CARD",
                state.teamSide,
                player,
                null,
                "Red card",
                "/Images/match_events/red_card.png"
        ));

        state.statuses.put(player.getId(), EuropeanPlayerMatchStatus.SENT_OFF);

        if (matchesPosition(player, "goalkeeper")) {
            handleEuropeanGoalkeeperProblem(match, minute, state, "Goalkeeper sent off", events, random);
        }
    }

    private void handleEuropeanInjury(EuropeanMatch match,
                                      int minute,
                                      EuropeanTeamMatchState state,
                                      List<EuropeanMatchEvent> events,
                                      Random random) {
        GeneratedPlayer injuredPlayer = randomEuropeanPlayerOnPitch(state, random);

        if (injuredPlayer == null || injuredPlayer.getId() == null) {
            return;
        }

        events.add(createEuropeanEvent(
                match,
                minute,
                "INJURY",
                state.teamSide,
                injuredPlayer,
                null,
                "Injury",
                "/Images/match_events/injury.png"
        ));

        state.statuses.put(injuredPlayer.getId(), EuropeanPlayerMatchStatus.INJURED_OFF);

        if (matchesPosition(injuredPlayer, "goalkeeper")) {
            handleEuropeanGoalkeeperProblem(match, minute, state, "Goalkeeper injury", events, random);
        } else {
            handleEuropeanForcedInjurySubstitution(match, minute, state, injuredPlayer, events, random);
        }
    }

    private void handleEuropeanTacticalSubstitution(EuropeanMatch match,
                                                    int minute,
                                                    EuropeanTeamMatchState state,
                                                    List<EuropeanMatchEvent> events,
                                                    Random random) {
        if (state.substitutionsUsed >= 5) {
            return;
        }

        if (!state.tacticalSubstitutionMinutes.contains(minute) && state.tacticalSubstitutionMinutes.size() >= 3) {
            return;
        }

        GeneratedPlayer playerOut = randomEuropeanFieldPlayerOnPitch(state, random);
        GeneratedPlayer playerIn = randomEuropeanBenchPlayer(state, false, random);

        if (playerOut == null || playerIn == null) {
            return;
        }

        makeEuropeanSubstitution(match, minute, state, playerOut, playerIn, "Substitution", events, false);
        state.tacticalSubstitutionMinutes.add(minute);
    }

    private void handleEuropeanForcedInjurySubstitution(EuropeanMatch match,
                                                        int minute,
                                                        EuropeanTeamMatchState state,
                                                        GeneratedPlayer injuredPlayer,
                                                        List<EuropeanMatchEvent> events,
                                                        Random random) {
        if (state.substitutionsUsed >= 5) {
            return;
        }

        GeneratedPlayer playerIn = randomEuropeanBenchPlayer(state, false, random);

        if (playerIn == null) {
            return;
        }

        makeEuropeanSubstitution(match, Math.min(minute + 1, 90), state, injuredPlayer, playerIn,
                "Substitution due to injury", events, true);
    }

    private void handleEuropeanGoalkeeperProblem(EuropeanMatch match,
                                                 int minute,
                                                 EuropeanTeamMatchState state,
                                                 String reason,
                                                 List<EuropeanMatchEvent> events,
                                                 Random random) {
        if (state.substitutionsUsed < 5) {
            GeneratedPlayer benchGoalkeeper = randomEuropeanBenchPlayer(state, true, random);

            if (benchGoalkeeper != null) {
                GeneratedPlayer fieldPlayerOut = randomEuropeanFieldPlayerOnPitch(state, random);

                if (fieldPlayerOut != null) {
                    makeEuropeanSubstitution(match, Math.min(minute + 1, 90), state, fieldPlayerOut, benchGoalkeeper,
                            reason + " - goalkeeper substitution", events, false);
                    return;
                }
            }
        }

        GeneratedPlayer emergencyGoalkeeper = randomEuropeanFieldPlayerOnPitch(state, random);

        if (emergencyGoalkeeper == null || emergencyGoalkeeper.getId() == null) {
            return;
        }

        state.hasEmergencyGoalkeeper = true;

        events.add(createEuropeanEvent(
                match,
                Math.min(minute + 1, 90),
                "EMERGENCY_GOALKEEPER",
                state.teamSide,
                emergencyGoalkeeper,
                null,
                "Emergency goalkeeper after " + reason,
                "/Images/match_events/change.png"
        ));
    }

    private void handleEuropeanPenaltyMissed(EuropeanMatch match,
                                             int minute,
                                             EuropeanTeamMatchState state,
                                             List<EuropeanMatchEvent> events,
                                             Random random) {
        GeneratedPlayer player = randomEuropeanFieldPlayerOnPitch(state, random);

        if (player == null) {
            return;
        }

        events.add(createEuropeanEvent(
                match,
                minute,
                "PENALTY_MISSED",
                state.teamSide,
                player,
                null,
                "Penalty missed",
                "/Images/match_events/penalty_missed.png"
        ));
    }

    private void makeEuropeanSubstitution(EuropeanMatch match,
                                          int minute,
                                          EuropeanTeamMatchState state,
                                          GeneratedPlayer playerOut,
                                          GeneratedPlayer playerIn,
                                          String description,
                                          List<EuropeanMatchEvent> events,
                                          boolean playerOutAlreadyOff) {
        if (state.substitutionsUsed >= 5) {
            return;
        }

        if (playerOut == null || playerIn == null || playerOut.getId() == null || playerIn.getId() == null) {
            return;
        }

        if (state.statuses.get(playerIn.getId()) != EuropeanPlayerMatchStatus.BENCH) {
            return;
        }

        if (!playerOutAlreadyOff && state.statuses.get(playerOut.getId()) != EuropeanPlayerMatchStatus.ON_PITCH) {
            return;
        }

        if (!playerOutAlreadyOff) {
            state.statuses.put(playerOut.getId(), EuropeanPlayerMatchStatus.SUBSTITUTED);
        }

        state.statuses.put(playerIn.getId(), EuropeanPlayerMatchStatus.ON_PITCH);
        state.substitutionsUsed++;

        EuropeanMatchEvent event = createEuropeanEvent(
                match,
                minute,
                "SUBSTITUTION",
                state.teamSide,
                playerIn,
                playerOut,
                description,
                "/Images/match_events/change.png"
        );

        event.setDescription("In: " + playerName(playerIn) + " / Out: " + playerName(playerOut));
        events.add(event);
    }

    private EuropeanMatchEvent createEuropeanEvent(EuropeanMatch match,
                                                   int minute,
                                                   String eventType,
                                                   String teamSide,
                                                   GeneratedPlayer player,
                                                   GeneratedPlayer relatedPlayer,
                                                   String description,
                                                   String iconUrl) {
        EuropeanMatchEvent event = new EuropeanMatchEvent();
        event.setMatchId(match.getId());
        event.setEventMinute(minute);
        event.setEventType(eventType);
        event.setTeamSide(teamSide);
        event.setPlayerId(player != null ? player.getId() : null);
        event.setPlayerName(playerName(player));
        event.setRelatedPlayerId(relatedPlayer != null ? relatedPlayer.getId() : null);
        event.setRelatedPlayerName(relatedPlayer != null ? playerName(relatedPlayer) : null);
        event.setDescription(description);
        event.setIconUrl(iconUrl);
        return event;
    }

    private List<GeneratedPlayer> europeanPlayersOnPitch(EuropeanTeamMatchState state) {
        return state.statuses.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == EuropeanPlayerMatchStatus.ON_PITCH)
                .map(Map.Entry::getKey)
                .map(id -> findEuropeanPlayerById(state.startingPlayers, state.benchPlayers, id))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<GeneratedPlayer> europeanFieldPlayersOnPitch(EuropeanTeamMatchState state) {
        return europeanPlayersOnPitch(state)
                .stream()
                .filter(player -> !matchesPosition(player, "goalkeeper"))
                .toList();
    }

    private GeneratedPlayer randomEuropeanPlayerOnPitch(EuropeanTeamMatchState state, Random random) {
        return randomPlayer(europeanPlayersOnPitch(state), random);
    }

    private GeneratedPlayer randomEuropeanFieldPlayerOnPitch(EuropeanTeamMatchState state, Random random) {
        List<GeneratedPlayer> fieldPlayers = europeanFieldPlayersOnPitch(state);

        if (fieldPlayers.isEmpty()) {
            return randomEuropeanPlayerOnPitch(state, random);
        }

        return randomPlayer(fieldPlayers, random);
    }

    private GeneratedPlayer randomEuropeanDifferentFieldPlayerOnPitch(EuropeanTeamMatchState state,
                                                                      GeneratedPlayer excluded,
                                                                      Random random) {
        List<GeneratedPlayer> fieldPlayers = europeanFieldPlayersOnPitch(state)
                .stream()
                .filter(player -> excluded == null || !Objects.equals(player.getId(), excluded.getId()))
                .toList();

        return randomPlayer(fieldPlayers, random);
    }

    private GeneratedPlayer randomEuropeanBenchPlayer(EuropeanTeamMatchState state,
                                                      boolean goalkeeperOnly,
                                                      Random random) {
        List<GeneratedPlayer> availableBench = state.benchPlayers.stream()
                .filter(player -> player.getId() != null)
                .filter(player -> state.statuses.get(player.getId()) == EuropeanPlayerMatchStatus.BENCH)
                .filter(player -> !goalkeeperOnly || matchesPosition(player, "goalkeeper"))
                .toList();

        return randomPlayer(availableBench, random);
    }

    private GeneratedPlayer findEuropeanPlayerById(List<GeneratedPlayer> startingPlayers,
                                                   List<GeneratedPlayer> benchPlayers,
                                                   Long id) {
        if (id == null) {
            return null;
        }

        return java.util.stream.Stream.concat(startingPlayers.stream(), benchPlayers.stream())
                .filter(player -> id.equals(player.getId()))
                .findFirst()
                .orElse(null);
    }

    private int generateEuropeanSubstitutionsCount(Random random) {
        int roll = random.nextInt(100);

        if (roll < 3) return 0;
        if (roll < 8) return 1;
        if (roll < 18) return 2;
        if (roll < 35) return 3;
        if (roll < 65) return 4;

        return 5;
    }

    private int generateEuropeanCardMinute(Random random) {
        int roll = random.nextInt(100);

        if (roll < 8) return 1 + random.nextInt(30);
        if (roll < 35) return 31 + random.nextInt(30);
        if (roll < 75) return 61 + random.nextInt(20);

        return 81 + random.nextInt(10);
    }

    private int generateEuropeanSubstitutionMinute(Random random) {
        int roll = random.nextInt(100);

        if (roll < 5) return 1 + random.nextInt(45);
        if (roll < 35) return 46 + random.nextInt(20);
        if (roll < 75) return 66 + random.nextInt(15);

        return 81 + random.nextInt(10);
    }

    private enum EuropeanPlayerMatchStatus {
        ON_PITCH,
        BENCH,
        SUBSTITUTED,
        SENT_OFF,
        INJURED_OFF
    }

    private enum EuropeanPlannedEventType {
        HOME_GOAL,
        AWAY_GOAL,
        HOME_YELLOW,
        AWAY_YELLOW,
        HOME_RED,
        AWAY_RED,
        HOME_SUBSTITUTION,
        AWAY_SUBSTITUTION,
        HOME_PENALTY_MISSED,
        AWAY_PENALTY_MISSED,
        INJURY
    }

    private record EuropeanPlannedEvent(int minute, EuropeanPlannedEventType type) {
    }

    private static class EuropeanTeamMatchState {
        private final boolean homeTeam;
        private final String teamSide;
        private final List<GeneratedPlayer> startingPlayers;
        private final List<GeneratedPlayer> benchPlayers;
        private final Map<Long, EuropeanPlayerMatchStatus> statuses = new HashMap<>();
        private final Map<Long, Integer> yellowCards = new HashMap<>();
        private final Set<Integer> tacticalSubstitutionMinutes = new HashSet<>();
        private int substitutionsUsed = 0;
        private boolean hasEmergencyGoalkeeper = false;

        private EuropeanTeamMatchState(boolean homeTeam,
                                       List<GeneratedPlayer> startingPlayers,
                                       List<GeneratedPlayer> benchPlayers) {
            this.homeTeam = homeTeam;
            this.teamSide = homeTeam ? "HOME" : "AWAY";
            this.startingPlayers = startingPlayers;
            this.benchPlayers = benchPlayers;

            for (GeneratedPlayer player : startingPlayers) {
                if (player.getId() != null) {
                    statuses.put(player.getId(), EuropeanPlayerMatchStatus.ON_PITCH);
                }
            }

            for (GeneratedPlayer player : benchPlayers) {
                if (player.getId() != null) {
                    statuses.put(player.getId(), EuropeanPlayerMatchStatus.BENCH);
                }
            }
        }
    }

    private List<GeneratedPlayer> generateLineupForEuropeanMatch(String leagueSlug,
                                                                 String clubSlug,
                                                                 String formation) {
        List<GeneratedPlayer> players =
                generatedPlayerRepository.findByLeagueSlugAndClubSlug(leagueSlug, clubSlug);

        return generateLineup(players, formation);
    }

    private List<GeneratedPlayer> generateLineup(List<GeneratedPlayer> players,
                                                 String formation) {
        if (players == null || players.isEmpty()) {
            return List.of();
        }

        int defenders = 4;
        int midfielders = 3;
        int forwards = 3;

        if (formation != null && !formation.isBlank()) {
            String[] split = formation.split("-");

            defenders = Integer.parseInt(split[0]);
            forwards = Integer.parseInt(split[split.length - 1]);
            midfielders = 10 - defenders - forwards;
        }

        List<GeneratedPlayer> starting = new ArrayList<>();

        starting.addAll(bestPlayers(players, "goalkeeper", 1));
        starting.addAll(bestPlayers(players, "defender", defenders));
        starting.addAll(bestPlayers(players, "midfielder", midfielders));
        starting.addAll(bestPlayers(players, "forward", forwards));

        List<GeneratedPlayer> bench = players.stream()
                .filter(player -> !starting.contains(player))
                .sorted(Comparator.comparing(
                        GeneratedPlayer::getMarketValue,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(9)
                .toList();

        List<GeneratedPlayer> result = new ArrayList<>();
        result.addAll(starting.stream().distinct().limit(11).toList());
        result.addAll(bench.stream().distinct().limit(9).toList());

        return result;
    }

    private List<GeneratedPlayer> bestPlayers(List<GeneratedPlayer> players,
                                              String type,
                                              int limit) {
        return players.stream()
                .filter(player -> matchesPosition(player, type))
                .sorted(Comparator.comparing(
                        GeneratedPlayer::getMarketValue,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(limit)
                .toList();
    }

    private boolean matchesPosition(GeneratedPlayer player, String type) {
        String position = player.getPosition() == null
                ? ""
                : player.getPosition().toLowerCase();

        return switch (type) {
            case "goalkeeper" ->
                    position.contains("goalkeeper")
                            || position.contains("keeper")
                            || position.contains("bramkarz");

            case "defender" ->
                    position.contains("back")
                            || position.contains("defender")
                            || position.contains("obrońca")
                            || position.contains("obronca");

            case "midfielder" ->
                    position.contains("midfield")
                            || position.contains("midfielder")
                            || position.contains("pomocnik");

            case "forward" ->
                    position.contains("forward")
                            || position.contains("striker")
                            || position.contains("winger")
                            || position.contains("napastnik");

            default -> false;
        };
    }

    private GeneratedPlayer randomPlayer(List<GeneratedPlayer> players, Random random) {
        if (players == null || players.isEmpty()) {
            return null;
        }

        return players.get(random.nextInt(players.size()));
    }

    private String playerName(GeneratedPlayer player) {
        if (player == null) {
            return "Unknown player";
        }

        return player.getFirstName() + " " + player.getLastName();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private void simulateEuropeanMatches(List<EuropeanMatch> matches) {
        Random random = new Random();

        List<Referee> referees = refereeRepository.findAll();

        if (referees.isEmpty()) {
            throw new RuntimeException("No referees found in database.");
        }

        Map<java.time.LocalDate, List<Referee>> availableRefereesByDate = new HashMap<>();

        List<String> formations = List.of(
                "4-4-2", "4-3-3", "4-2-3-1", "4-1-2-1-2",
                "3-5-2", "3-4-3", "3-4-2-1",
                "5-3-2", "5-4-1"
        );

        for (EuropeanMatch match : matches) {
            int homeGoals = random.nextInt(5);
            int awayGoals = random.nextInt(5);

            match.setHomeGoals(homeGoals);
            match.setAwayGoals(awayGoals);

            match.setHalfTimeHomeGoals(Math.min(homeGoals, random.nextInt(homeGoals + 1)));
            match.setHalfTimeAwayGoals(Math.min(awayGoals, random.nextInt(awayGoals + 1)));

            match.setStatus("FINISHED");

            match.setHomeFormation(formations.get(random.nextInt(formations.size())));
            match.setAwayFormation(formations.get(random.nextInt(formations.size())));

            java.time.LocalDate matchDay = match.getMatchDate().toLocalDate();

            List<Referee> availableReferees = availableRefereesByDate.computeIfAbsent(
                    matchDay,
                    day -> {
                        List<Referee> copy = new ArrayList<>(referees);
                        Collections.shuffle(copy, random);
                        return copy;
                    }
            );

            if (availableReferees.isEmpty()) {
                throw new RuntimeException("Not enough referees for match day: " + matchDay);
            }

            Referee referee = availableReferees.remove(0);

            match.setStadium(match.getHomeTeam() + " Stadium");
            match.setReferee(referee.getName());

            int possessionHome = 42 + random.nextInt(17);
            int possessionAway = 100 - possessionHome;

            match.setPossessionHome(possessionHome);
            match.setPossessionAway(possessionAway);

            int shotsHome = 6 + random.nextInt(16);
            int shotsAway = 6 + random.nextInt(16);

            match.setShotsHome(shotsHome);
            match.setShotsAway(shotsAway);

            match.setShotsOnTargetHome(Math.min(shotsHome, 2 + random.nextInt(8)));
            match.setShotsOnTargetAway(Math.min(shotsAway, 2 + random.nextInt(8)));

            match.setCornersHome(random.nextInt(9));
            match.setCornersAway(random.nextInt(9));

            match.setFoulsHome(6 + random.nextInt(13));
            match.setFoulsAway(6 + random.nextInt(13));

            match.setOffsidesHome(random.nextInt(5));
            match.setOffsidesAway(random.nextInt(5));

            match.setYellowCardsHome(random.nextInt(5));
            match.setYellowCardsAway(random.nextInt(5));

            match.setRedCardsHome(random.nextInt(20) == 0 ? 1 : 0);
            match.setRedCardsAway(random.nextInt(20) == 0 ? 1 : 0);

            match.setPassesHome(320 + random.nextInt(380));
            match.setPassesAway(320 + random.nextInt(380));

            match.setPassAccuracyHome(74 + random.nextInt(18));
            match.setPassAccuracyAway(74 + random.nextInt(18));

            match.setXgHome(Math.round((0.4 + random.nextDouble() * 3.2) * 10.0) / 10.0);
            match.setXgAway(Math.round((0.4 + random.nextDouble() * 3.2) * 10.0) / 10.0);
        }
    }

    private List<EuropeanMatch> createLeaguePhaseDraw(
            List<EuropeanCompetitionParticipant> teams,
            String season,
            String competitionSlug
    ) {
        return createSimpleLeaguePhaseSchedule(teams, season, competitionSlug);
    }

    private List<EuropeanMatch> createSimpleLeaguePhaseSchedule(
            List<EuropeanCompetitionParticipant> teams,
            String season,
            String competitionSlug
    ) {
        List<EuropeanMatch> matches = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.now().plusDays(7);

        for (int round = 1; round <= 8; round++) {
            List<EuropeanCompetitionParticipant> roundTeams = new ArrayList<>(teams);
            Collections.shuffle(roundTeams);

            for (int i = 0; i < roundTeams.size(); i += 2) {
                EuropeanCompetitionParticipant home = roundTeams.get(i);
                EuropeanCompetitionParticipant away = roundTeams.get(i + 1);

                EuropeanMatch match = new EuropeanMatch();

                match.setSeason(season);
                match.setCompetitionSlug(competitionSlug);

                match.setHomeTeam(home.getClubName());
                match.setAwayTeam(away.getClubName());

                match.setHomeClubSlug(home.getClubSlug());
                match.setAwayClubSlug(away.getClubSlug());

                match.setHomeLeagueSlug(home.getLeagueSlug());
                match.setAwayLeagueSlug(away.getLeagueSlug());

                match.setHomeGoals(0);
                match.setAwayGoals(0);
                match.setStatus("SCHEDULED");

                match.setRoundNumber(round);
                match.setMatchDate(startDate.plusWeeks(round - 1));

                matches.add(match);
            }
        }

        return matches;
    }

    private List<EuropeanMatch> createSingleLeaguePhaseDraw(
            List<EuropeanCompetitionParticipant> teams,
            String season,
            String competitionSlug
    ) {
        List<EuropeanMatch> matches = new ArrayList<>();

        Map<Long, Set<Long>> opponents = new HashMap<>();
        Map<Long, Map<Integer, Integer>> potCounter = new HashMap<>();
        Map<Long, Map<String, Integer>> countryCounter = new HashMap<>();
        Map<Long, Integer> homeCounter = new HashMap<>();
        Map<Long, Integer> awayCounter = new HashMap<>();

        for (EuropeanCompetitionParticipant team : teams) {
            opponents.put(team.getId(), new HashSet<>());
            potCounter.put(team.getId(), new HashMap<>());
            countryCounter.put(team.getId(), new HashMap<>());
            homeCounter.put(team.getId(), 0);
            awayCounter.put(team.getId(), 0);
        }

        List<EuropeanCompetitionParticipant> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams);

        for (EuropeanCompetitionParticipant team : shuffledTeams) {
            for (int pot = 1; pot <= 4; pot++) {
                final int currentPot = pot;
                while (potCounter.get(team.getId()).getOrDefault(currentPot, 0) < 2) {

                    List<EuropeanCompetitionParticipant> candidates = teams.stream()
                            .filter(candidate -> !candidate.getId().equals(team.getId()))
                            .filter(candidate -> candidate.getPotNumber() == currentPot)
                            .filter(candidate -> !opponents.get(team.getId()).contains(candidate.getId()))
                            .filter(candidate -> potCounter.get(candidate.getId())
                                    .getOrDefault(team.getPotNumber(), 0) < 2)
                            .filter(candidate -> countryCounter.get(team.getId())
                                    .getOrDefault(candidate.getCountryCode(), 0) < 2)
                            .filter(candidate -> countryCounter.get(candidate.getId())
                                    .getOrDefault(team.getCountryCode(), 0) < 2)
                            .filter(candidate ->
                                    (homeCounter.get(team.getId()) < 4 && awayCounter.get(candidate.getId()) < 4)
                                            ||
                                            (homeCounter.get(candidate.getId()) < 4 && awayCounter.get(team.getId()) < 4)
                            )
                            .collect(Collectors.toList());

                    if (candidates.isEmpty()) {
                        throw new RuntimeException("Draw attempt failed.");
                    }

                    Collections.shuffle(candidates);

                    EuropeanCompetitionParticipant rival = candidates.get(0);

                    boolean teamCanHome = homeCounter.get(team.getId()) < 4
                            && awayCounter.get(rival.getId()) < 4;

                    boolean rivalCanHome = homeCounter.get(rival.getId()) < 4
                            && awayCounter.get(team.getId()) < 4;

                    EuropeanCompetitionParticipant home;
                    EuropeanCompetitionParticipant away;

                    if (teamCanHome && rivalCanHome) {
                        if (homeCounter.get(team.getId()) <= homeCounter.get(rival.getId())) {
                            home = team;
                            away = rival;
                        } else {
                            home = rival;
                            away = team;
                        }
                    } else if (teamCanHome) {
                        home = team;
                        away = rival;
                    } else if (rivalCanHome) {
                        home = rival;
                        away = team;
                    } else {
                        throw new RuntimeException("Draw attempt failed - home/away blocked.");
                    }

                    EuropeanMatch match = new EuropeanMatch();
                    match.setSeason(season);
                    match.setCompetitionSlug(competitionSlug);
                    match.setHomeTeam(home.getClubName());
                    match.setAwayTeam(away.getClubName());
                    match.setHomeGoals(0);
                    match.setAwayGoals(0);
                    match.setStatus("SCHEDULED");

                    match.setHomeClubSlug(home.getClubSlug());
                    match.setAwayClubSlug(away.getClubSlug());
                    match.setHomeLeagueSlug(home.getLeagueSlug());
                    match.setAwayLeagueSlug(away.getLeagueSlug());

                    matches.add(match);

                    opponents.get(team.getId()).add(rival.getId());
                    opponents.get(rival.getId()).add(team.getId());

                    potCounter.get(team.getId()).merge(rival.getPotNumber(), 1, Integer::sum);
                    potCounter.get(rival.getId()).merge(team.getPotNumber(), 1, Integer::sum);

                    countryCounter.get(team.getId()).merge(rival.getCountryCode(), 1, Integer::sum);
                    countryCounter.get(rival.getId()).merge(team.getCountryCode(), 1, Integer::sum);

                    homeCounter.merge(home.getId(), 1, Integer::sum);
                    awayCounter.merge(away.getId(), 1, Integer::sum);
                }
            }
        }

        if (matches.size() != 144) {
            throw new RuntimeException("Invalid league phase draw. Expected 144 matches, found: " + matches.size());
        }

        for (EuropeanCompetitionParticipant team : teams) {
            Long id = team.getId();

            if (opponents.get(id).size() != 8) {
                throw new RuntimeException("Invalid opponent count for: " + team.getClubName());
            }

            if (homeCounter.get(id) != 4 || awayCounter.get(id) != 4) {
                throw new RuntimeException("Invalid home/away balance for: " + team.getClubName());
            }

            for (int pot = 1; pot <= 4; pot++) {
                if (potCounter.get(id).getOrDefault(pot, 0) != 2) {
                    throw new RuntimeException("Invalid pot balance for: " + team.getClubName());
                }
            }
        }

        LocalDateTime startDate = LocalDateTime.now().plusDays(7);
        assignLeaguePhaseRounds(matches, startDate);
        return matches;
    }

    private void assignLeaguePhaseRounds(List<EuropeanMatch> matches, LocalDateTime startDate) {
        List<EuropeanMatch> remainingMatches = new ArrayList<>(matches);

        for (int round = 1; round <= 8; round++) {
            List<EuropeanMatch> roundMatches = new ArrayList<>();
            Set<String> usedTeams = new HashSet<>();

            Iterator<EuropeanMatch> iterator = remainingMatches.iterator();

            while (iterator.hasNext() && roundMatches.size() < 18) {
                EuropeanMatch match = iterator.next();

                String homeKey = match.getHomeLeagueSlug() + "|" + match.getHomeClubSlug();
                String awayKey = match.getAwayLeagueSlug() + "|" + match.getAwayClubSlug();

                if (!usedTeams.contains(homeKey) && !usedTeams.contains(awayKey)) {
                    match.setRoundNumber(round);
                    match.setMatchDate(startDate.plusWeeks(round - 1));

                    usedTeams.add(homeKey);
                    usedTeams.add(awayKey);

                    roundMatches.add(match);
                    iterator.remove();
                }
            }

            if (roundMatches.size() != 18) {
                throw new RuntimeException("Could not build full round " + round + ". Matches: " + roundMatches.size());
            }
        }

        if (!remainingMatches.isEmpty()) {
            throw new RuntimeException("Unassigned European matches: " + remainingMatches.size());
        }
    }

    private void fillMissingTeamsFromFreeClubs(
            List<EuropeanCompetitionParticipant> targetCompetition,
            String season,
            String competitionSlug,
            List<String> domesticLeagues,
            List<EuropeanCompetitionParticipant> alreadyUsed
    ) {
        if (targetCompetition.size() >= 36) {
            return;
        }

        Set<String> usedKeys = alreadyUsed.stream()
                .map(p -> p.getLeagueSlug() + "|" + p.getClubSlug())
                .collect(Collectors.toSet());

        List<EuropeanCompetitionParticipant> freeClubs = domesticLeagues.stream()
                .flatMap(leagueSlug -> {
                    List<Match> matches = matchRepository.findByLeagueOrderByRoundNumberAscMatchDateAsc(leagueSlug);

                    if (matches.isEmpty()) {
                        return java.util.stream.Stream.empty();
                    }

                    List<LeagueTableRow> table = leagueTableService.calculateTable(matches, leagueSlug);

                    return table.stream()
                            .map(row -> createParticipantFromTableRow(season, competitionSlug, leagueSlug, row));
                })
                .filter(p -> !usedKeys.contains(p.getLeagueSlug() + "|" + p.getClubSlug()))
                .sorted(Comparator.comparingInt(EuropeanCompetitionParticipant::getUefaRankingPoints).reversed())
                .toList();

        for (EuropeanCompetitionParticipant club : freeClubs) {
            if (targetCompetition.size() >= 36) {
                break;
            }

            if (canAddTeamFromLeague(targetCompetition, competitionSlug, club)) {
                club.setCompetitionSlug(competitionSlug);
                targetCompetition.add(club);
                usedKeys.add(club.getLeagueSlug() + "|" + club.getClubSlug());
            }
        }
    }

    public boolean areAllDomesticLeaguesGenerated() {
        List<String> domesticLeagues = List.of(
                "england", "spain", "italy", "germany", "france", "netherlands",
                "portugal", "belgium", "turkiye", "scotland", "austria",
                "switzerland", "greece", "denmark", "poland", "sweden",
                "norway", "croatia", "serbia", "romania", "bulgaria",
                "ukraine", "czech-republic", "israel"
        );

        return domesticLeagues.stream()
                .allMatch(matchRepository::existsByLeague);
    }

    public boolean isEuropeanCompetitionGenerated(String season, String competitionSlug) {
        return europeanMatchRepository.existsBySeasonAndCompetitionSlug(season, competitionSlug);
    }

    private static final List<String> DOMESTIC_LEAGUES = List.of(
            "england", "spain", "italy", "germany", "france", "netherlands",
            "portugal", "belgium", "turkiye", "scotland", "austria",
            "switzerland", "greece", "denmark", "poland", "sweden",
            "norway", "croatia", "serbia", "romania", "bulgaria",
            "ukraine", "czech-republic", "israel"
    );

    public Map<String, Object> getGenerationStatus(String season, String competitionSlug) {
        boolean competitionGenerated =
                europeanMatchRepository.existsBySeasonAndCompetitionSlug(season, competitionSlug);

        List<String> missingLeagues = getMissingDomesticLeagues();

        return Map.of(
                "competitionGenerated", competitionGenerated,
                "allDomesticLeaguesGenerated", missingLeagues.isEmpty(),
                "needsDomesticRegeneration", !missingLeagues.isEmpty(),
                "missingLeaguesCount", missingLeagues.size()
        );
    }

    private List<String> getMissingDomesticLeagues() {
        return DOMESTIC_LEAGUES.stream()
                .filter(league -> !matchRepository.existsByLeague(league))
                .toList();
    }

    @Transactional
    public void generateOrRegenerateEuropeanCompetition(String season, String competitionSlug) {
        List<String> missingLeagues = getMissingDomesticLeagues();
// TODO: tutaj jeśli wszystkie ligi krajowe są już wygenerowane → nie rusza lig krajowych, jeśli brakuje choć jednej ligi → generuje wszystkie ligi krajowe od nowa.
        if (!missingLeagues.isEmpty()) {
            for (String league : DOMESTIC_LEAGUES) {
                matchGeneratorService.generateLeagueSeason(league);
            }

            participantRepository.deleteBySeason(season);
        }

        if (!participantRepository.existsBySeasonAndCompetitionSlug(season, competitionSlug)) {
            participantRepository.deleteBySeason(season);
            generateParticipantsFromDomesticTables(season);
        }

        deleteEuropeanCompetitionData(season, competitionSlug);
        generateLeaguePhaseMatches(season, competitionSlug);
    }

    private void deleteEuropeanCompetitionData(String season, String competitionSlug) {
        List<Long> matchIds = europeanMatchRepository
                .findBySeasonAndCompetitionSlugOrderByRoundNumberAscMatchDateAsc(season, competitionSlug)
                .stream()
                .map(EuropeanMatch::getId)
                .toList();

        if (!matchIds.isEmpty()) {
            europeanMatchEventRepository.deleteByMatchIdIn(matchIds);
        }

        europeanMatchRepository.deleteBySeasonAndCompetitionSlug(season, competitionSlug);
    }
}
