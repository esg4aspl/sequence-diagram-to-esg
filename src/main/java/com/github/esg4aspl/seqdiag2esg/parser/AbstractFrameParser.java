package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;

import java.util.List;

public abstract class AbstractFrameParser implements FrameParser{

    private final List<String> lines;

    private final ElementType type;

    private final Frame frame;

    private final FrameParserFactory frameParserFactory;

    protected AbstractFrameParser(List<String> lines, ElementType type) {
        this.lines = lines;
        this.type = type;
        this.frame = new Frame(type);
        this.frameParserFactory = FrameParserFactory.getInstance();
    }

    protected List<String> getLines() {
        return lines;
    }

    public ElementType getType() {
        return type;
    }

    public Frame getFrame() {
        return frame;
    }

    public FrameParserFactory getFrameParserFactory() {
        return frameParserFactory;
    }
}
