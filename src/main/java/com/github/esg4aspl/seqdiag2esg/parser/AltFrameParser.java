package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AltFrameParser extends AbstractFrameParser {

    private boolean isEndStatementSeen = false;

    public AltFrameParser(ElementType elementType, List<String> lines) {
        super(lines, elementType);
        if (elementType != ElementType.ALT_FRAME) {
            throw new IllegalArgumentException("Unexpected type:" + elementType);
        }
    }

    @Override
    public Frame parseFrame() {
        while (true) {
            List<String> linesForNextBlock = getLinesForNextBlock();
            if (linesForNextBlock.isEmpty()) {
                break;
            }
            FrameParser innerFrameParser = getFrameParserFactory().getFrameParser(ElementType.ELSE_FRAME, linesForNextBlock);
            getFrame().getElements().add(innerFrameParser.parseFrame());
        }
        return getFrame();
    }

    List<String> getLinesForNextBlock() {
        if (isEndStatementSeen) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        while (!getLines().isEmpty()) {
            String line = getLines().get(0);
            line = line.trim();
            if (!result.isEmpty() && line.startsWith("else")) {
                break;
            }
            getLines().remove(0);
            if (line.startsWith("end")) {
                isEndStatementSeen = true;
                break;
            }
            result.add(line);
        }
        if (!result.isEmpty() && result.get(0).startsWith(ElementType.ALT_FRAME.getText())) {
            String replaceFirstLineAltWithElse = result.get(0).replaceFirst(ElementType.ALT_FRAME.getText(), ElementType.ELSE_FRAME.getText());
            result.set(0, replaceFirstLineAltWithElse);
        }
        return result;
    }

}
