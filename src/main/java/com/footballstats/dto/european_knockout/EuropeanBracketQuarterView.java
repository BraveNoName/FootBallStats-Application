package com.footballstats.dto.european_knockout;

import com.footballstats.model.EuropeanKnockoutMatch;

public class EuropeanBracketQuarterView {

    private EuropeanBracketPairView upperPair;
    private EuropeanBracketPairView lowerPair;
    private EuropeanKnockoutMatch quarterFinalMatch;

    public EuropeanBracketQuarterView(EuropeanBracketPairView upperPair,
                                      EuropeanBracketPairView lowerPair,
                                      EuropeanKnockoutMatch quarterFinalMatch) {
        this.upperPair = upperPair;
        this.lowerPair = lowerPair;
        this.quarterFinalMatch = quarterFinalMatch;
    }

    public EuropeanBracketPairView getUpperPair() {
        return upperPair;
    }

    public EuropeanBracketPairView getLowerPair() {
        return lowerPair;
    }

    public EuropeanKnockoutMatch getQuarterFinalMatch() {
        return quarterFinalMatch;
    }
}
