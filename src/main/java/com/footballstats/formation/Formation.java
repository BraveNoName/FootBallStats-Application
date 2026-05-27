package com.footballstats.formation;

import java.util.List;

public enum Formation {

    F442(List.of(4,4,2)),
    F433(List.of(4,3,3)),
    F4231(List.of(4,2,3,1)),
    F41212(List.of(4,1,2,1,2)),
    F451(List.of(4,5,1)),
    F4321(List.of(4,3,2,1)),
    F4222(List.of(4,2,2,2)),
    F4141(List.of(4,1,4,1)),

    F352(List.of(3,5,2)),
    F343(List.of(3,4,3)),
    F3412(List.of(3,4,1,2)),
    F3421(List.of(3,4,2,1)),
    F3142(List.of(3,1,4,2)),
    F361(List.of(3,6,1)),

    F532(List.of(5,3,2)),
    F541(List.of(5,4,1)),
    F523(List.of(5,2,3)),
    F5212(List.of(5,2,1,2)),
    F5122(List.of(5,1,2,2));

    private final List<Integer> lines;

    Formation(List<Integer> lines) {
        this.lines = lines;
    }

    public List<Integer> getLines() {
        return lines;
    }
}