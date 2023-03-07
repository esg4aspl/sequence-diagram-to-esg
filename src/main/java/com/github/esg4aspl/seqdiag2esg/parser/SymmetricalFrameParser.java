package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Message;

import java.util.List;

// parse frames starting with a keyword and ending with 'end'
public class SymmetricalFrameParser extends AbstractFrameParser {

    public SymmetricalFrameParser(ElementType elementType, List<String> lines) {
        super(lines, elementType);

        if (elementType != ElementType.SD_FRAME && elementType != ElementType.OPT_FRAME &&
                elementType != ElementType.LOOP_FRAME && elementType != ElementType.ELSE_FRAME) {
            throw new IllegalArgumentException("Unexpected type:" + elementType);
        }
    }

    @Override
    public Frame parseFrame() {
        if (getLines().isEmpty()) {
            throw new IllegalArgumentException("Empty frame body");
        }
        // extract the guard clause from the frame begin
        // sd frame does not have a begin line, check whether that is the case before consuming the line
        String frameGuard = Utils.getFrameGuard(getType(), getLines().get(0));
        if (frameGuard != null) {
            getFrame().setGuard(frameGuard);
            getLines().remove(0);
        }
        while (!getLines().isEmpty()) {
            // iterate until end of file or end of frame
            String line = getLines().get(0);
            if (Utils.isFrameEnd(line)) {
                // remove self end
                getLines().remove(0);
                return getFrame();
            }
            // each frame parser will remove its lines
            if (Utils.isFrameBeginning(line)) {
                ElementType type = Utils.getFrameTypeFromFirstLine(line);
                FrameParser innerFrameParser = getFrameParserFactory().getFrameParser(type, getLines());
                getFrame().getElements().add(innerFrameParser.parseFrame());
                continue;
            }
            // remove self lines
            getLines().remove(0);
            Message message = Utils.parseIntoMessage(line);
            if (message != null) {
                getFrame().getElements().add(message);
            }
        }
        return getFrame();
    }
}
