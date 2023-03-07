package com.github.esg4aspl.seqdiag2esg.entity.sequencediagram;

public enum ElementType {

    MESSAGE(null),
    SD_FRAME(null),
    ALT_FRAME("alt"),
    ELSE_FRAME("else"),
    OPT_FRAME("opt"),
    LOOP_FRAME("loop");

    private String text;

    ElementType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
