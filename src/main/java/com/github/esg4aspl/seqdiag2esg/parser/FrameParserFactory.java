package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;

import java.util.List;

public class FrameParserFactory {

    static FrameParserFactory singletonInstance;

    public static FrameParserFactory getInstance(){
        if(singletonInstance == null){
            singletonInstance = new FrameParserFactory();
        }
        return singletonInstance;
    }

    public FrameParser getFrameParser(ElementType type, List<String> lines){
        switch(type){
            case SD_FRAME:
            case OPT_FRAME:
            case LOOP_FRAME:
            case ELSE_FRAME:
                return new SymmetricalFrameParser(type, lines);
            case ALT_FRAME:
                return new AltFrameParser(type, lines);
            default:
                throw new IllegalArgumentException("Unknown type for FrameParser construction:" + type);
        }
    }
}
