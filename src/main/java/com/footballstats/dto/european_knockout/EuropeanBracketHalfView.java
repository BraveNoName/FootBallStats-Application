package com.footballstats.dto.european_knockout;

import com.footballstats.model.EuropeanKnockoutMatch;

public class EuropeanBracketHalfView {

    private EuropeanBracketQuarterView upperQuarter;
    private EuropeanBracketQuarterView lowerQuarter;
    private EuropeanKnockoutMatch semiFinalMatch;

    public EuropeanBracketHalfView(EuropeanBracketQuarterView upperQuarter,
                                   EuropeanBracketQuarterView lowerQuarter,
                                   EuropeanKnockoutMatch semiFinalMatch) {
        this.upperQuarter = upperQuarter;
        this.lowerQuarter = lowerQuarter;
        this.semiFinalMatch = semiFinalMatch;
    }

    public EuropeanBracketQuarterView getUpperQuarter() {
        return upperQuarter;
    }

    public EuropeanBracketQuarterView getLowerQuarter() {
        return lowerQuarter;
    }

    public EuropeanKnockoutMatch getSemiFinalMatch() {
        return semiFinalMatch;
    }
}
