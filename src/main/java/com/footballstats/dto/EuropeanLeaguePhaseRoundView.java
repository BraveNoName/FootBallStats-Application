package com.footballstats.dto;

import com.footballstats.model.EuropeanMatch;
import java.util.List;

public class EuropeanLeaguePhaseRoundView {

    private int roundNumber;
    private List<EuropeanLeagueTableRow> tableRows;
    private List<EuropeanMatch> matches;

    public EuropeanLeaguePhaseRoundView(int roundNumber,
                                        List<EuropeanLeagueTableRow> tableRows,
                                        List<EuropeanMatch> matches) {
        this.roundNumber = roundNumber;
        this.tableRows = tableRows;
        this.matches = matches;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public List<EuropeanLeagueTableRow> getTableRows() {
        return tableRows;
    }

    public List<EuropeanMatch> getMatches() {
        return matches;
    }
}