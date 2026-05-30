package com.footballstats.dto.european_knockout;

import com.footballstats.model.EuropeanKnockoutMatch;

public class EuropeanBracketTreeView {

    private EuropeanBracketHalfView leftHalf;
    private EuropeanBracketHalfView rightHalf;
    private EuropeanKnockoutMatch finalMatch;

    public EuropeanBracketTreeView(EuropeanBracketHalfView leftHalf,
                                   EuropeanBracketHalfView rightHalf,
                                   EuropeanKnockoutMatch finalMatch) {
        this.leftHalf = leftHalf;
        this.rightHalf = rightHalf;
        this.finalMatch = finalMatch;
    }

    public EuropeanBracketHalfView getLeftHalf() {
        return leftHalf;
    }

    public EuropeanBracketHalfView getRightHalf() {
        return rightHalf;
    }

    public EuropeanKnockoutMatch getFinalMatch() {
        return finalMatch;
    }
}
