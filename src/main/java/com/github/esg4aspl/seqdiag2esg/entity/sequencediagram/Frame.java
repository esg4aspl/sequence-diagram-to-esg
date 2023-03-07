package com.github.esg4aspl.seqdiag2esg.entity.sequencediagram;

import java.util.ArrayList;
import java.util.List;

public class Frame extends Element{
    private final List<Element> elements = new ArrayList<>();

    private String guard;

    public Frame(ElementType type) {
        super(type);
    }

    public List<Element> getElements() {
        return elements;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "type=" + type +
                ", guard='" + guard + '\'' +
                '}';
    }
}
