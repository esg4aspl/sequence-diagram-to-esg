package com.github.esg4aspl.seqdiag2esg.entity.sequencediagram;

public abstract class Element {
    ElementType type;

    public ElementType getType() {
        return type;
    }

    protected Element(ElementType type) {
        this.type = type;
    }
}
