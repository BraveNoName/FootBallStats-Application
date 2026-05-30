package com.footballstats.dto.european_knockout;

import com.footballstats.model.EuropeanKnockoutMatch;

public class EuropeanBracketPairView {

    private EuropeanKnockoutMatch playOffMatch;
    private EuropeanKnockoutMatch roundOf16Match;

    public EuropeanBracketPairView(EuropeanKnockoutMatch playOffMatch,
                                   EuropeanKnockoutMatch roundOf16Match) {
        this.playOffMatch = playOffMatch;
        this.roundOf16Match = roundOf16Match;
    }

    public EuropeanKnockoutMatch getPlayOffMatch() {
        return playOffMatch;
    }

    public EuropeanKnockoutMatch getRoundOf16Match() {
        return roundOf16Match;
    }
}
