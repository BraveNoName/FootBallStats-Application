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
import java.math.BigDecimal;
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
    private final MatchRepository matchRepository;
    private final LeagueTableService leagueTableService;
    private final GeneratedPlayerRepository generatedPlayerRepository;

    public EuropeanCompetitionService(
            EuropeanCompetitionParticipantRepository participantRepository,
            EuropeanMatchRepository europeanMatchRepository,
            MatchRepository matchRepository,
            LeagueTableService leagueTableService,
            GeneratedPlayerRepository generatedPlayerRepository
    ) {
        this.participantRepository = participantRepository;
        this.europeanMatchRepository = europeanMatchRepository;
        this.matchRepository = matchRepository;
        this.leagueTableService = leagueTableService;
        this.generatedPlayerRepository = generatedPlayerRepository;
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
        europeanMatchRepository.saveAll(matches);
    }

    private void simulateEuropeanMatches(List<EuropeanMatch> matches) {
        Random random = new Random();

        for (EuropeanMatch match : matches) {
            int homeGoals = random.nextInt(5);
            int awayGoals = random.nextInt(5);

            match.setHomeGoals(homeGoals);
            match.setAwayGoals(awayGoals);
            match.setStatus("FINISHED");
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



}