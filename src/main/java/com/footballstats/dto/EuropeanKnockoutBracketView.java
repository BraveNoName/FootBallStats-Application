package com.footballstats.dto;

import java.util.List;

public class EuropeanKnockoutBracketView {

    private List<KnockoutTieView> playoffs;
    private List<KnockoutTieView> roundOf16;
    private List<KnockoutTieView> quarterFinals;
    private List<KnockoutTieView> semiFinals;
    private KnockoutTieView finalTie;
    private String champion;

    public EuropeanKnockoutBracketView(List<KnockoutTieView> playoffs,
                                       List<KnockoutTieView> roundOf16,
                                       List<KnockoutTieView> quarterFinals,
                                       List<KnockoutTieView> semiFinals,
                                       KnockoutTieView finalTie,
                                       String champion) {
        this.playoffs = playoffs;
        this.roundOf16 = roundOf16;
        this.quarterFinals = quarterFinals;
        this.semiFinals = semiFinals;
        this.finalTie = finalTie;
        this.champion = champion;
    }

    public List<KnockoutTieView> getPlayoffs() {
        return playoffs;
    }

    public List<KnockoutTieView> getRoundOf16() {
        return roundOf16;
    }

    public List<KnockoutTieView> getQuarterFinals() {
        return quarterFinals;
    }

    public List<KnockoutTieView> getSemiFinals() {
        return semiFinals;
    }

    public KnockoutTieView getFinalTie() {
        return finalTie;
    }

    public String getChampion() {
        return champion;
    }
}