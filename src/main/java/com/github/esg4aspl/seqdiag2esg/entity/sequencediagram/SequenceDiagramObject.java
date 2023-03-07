package com.github.esg4aspl.seqdiag2esg.entity.sequencediagram;

import java.util.Objects;

public class SequenceDiagramObject {
    private String name;

    public SequenceDiagramObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceDiagramObject that = (SequenceDiagramObject) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "SequenceDiagramObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
