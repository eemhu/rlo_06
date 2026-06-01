package com.teragrep.rlo_06.refactored;

public enum ResultName {
    CHAR("CHAR"),
    DIGIT("DIGIT"),
    REPEATABLE("REPEATABLE"),
    PRI("PRI"),
    VERSION("VERSION"),
    HEADER("HEADER"),
    MSG("MSG"),
    RFC5424("RFC5424"),
    SD("SD");

    private final String name;

    ResultName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
