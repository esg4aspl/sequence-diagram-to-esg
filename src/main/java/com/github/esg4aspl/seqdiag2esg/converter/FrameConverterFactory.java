package com.github.esg4aspl.seqdiag2esg.converter;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.apache.commons.lang3.mutable.MutableInt;

public class FrameConverterFactory {
    static FrameConverterFactory singletonInstance;

    public static FrameConverterFactory getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new FrameConverterFactory();
        }
        return singletonInstance;
    }

    public FrameConverter getFrameConverter(ElementType type, Frame frame, MutableInt esgCounter, String pseudoEventPrefix) {
        switch (type) {
            case SD_FRAME:
                return new SdFrameConverter(type, frame, esgCounter, pseudoEventPrefix);
            case OPT_FRAME:
                return new OptFrameConverter(type, frame, esgCounter, pseudoEventPrefix);
            case LOOP_FRAME:
                return new LoopFrameConverter(type, frame, esgCounter, pseudoEventPrefix);
            case ALT_FRAME:
                return new AltFrameConverter(type, frame, esgCounter, pseudoEventPrefix);
            case ELSE_FRAME:
                return new ElseFrameConverter(type, frame, esgCounter, pseudoEventPrefix);
            default:
                throw new IllegalArgumentException("Unknown type for FrameParser construction:" + type);
        }
    }

    public FrameConverter getFrameConverter(ElementType type, Frame frame, MutableInt esgCounter) {
        return getFrameConverter(type, frame, esgCounter, "");
    }
}
