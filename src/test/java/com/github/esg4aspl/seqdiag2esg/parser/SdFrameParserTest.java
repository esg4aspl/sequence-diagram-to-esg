package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.converter.FrameConverterFactory;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Vertex;
import tr.edu.iyte.esg.model.VertexRefinedByESG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

class SdFrameParserTest {
    @Test
    void whenPseudoEventPrefixGiven_AltFramesGuardShouldBeginWithPrefix() throws IOException {
        final String PSEUDO_PREFIX = "PSEUDO_EVENT";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("11craftAnAgent.puml").getFile());
        Frame frame;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            frame = FrameParserFactory.getInstance().getFrameParser(ElementType.SD_FRAME, reader.lines().collect(Collectors.toList())).parseFrame();
        }
        ESG esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0), PSEUDO_PREFIX).convert();
        Vertex altVertex = esg.getVertexList().stream().filter(vertex -> vertex.getEvent().getName().contains("ALT_FRAME")).findFirst().get();
        Vertex altVertexGuard = ((VertexRefinedByESG) altVertex).getSubESG().getVertexList().stream()
                .filter(vertex -> vertex.getEvent().getName().endsWith("craftable")).findFirst().get();
        Assertions.assertTrue(altVertexGuard.getEvent().getName().startsWith(PSEUDO_PREFIX));
    }
}
