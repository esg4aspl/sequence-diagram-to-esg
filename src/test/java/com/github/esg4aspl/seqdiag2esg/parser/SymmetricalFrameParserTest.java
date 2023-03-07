package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Element;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SymmetricalFrameParserTest {

    @Test
    void parseFrame_whenFlatSdFrameIsGiven_shouldParseIntoFlatFrame() {
        List<String> input = new ArrayList<>(List.of(
                "@startuml",
                "Alice->Bob:Hi",
                "Bob->Alice:Hey back",
                "@enduml"
        ));

        SymmetricalFrameParser mockParser = Mockito.mock(SymmetricalFrameParser.class);
        when(mockParser.parseFrame()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);
        when(mockParser.getType()).thenReturn(ElementType.SD_FRAME);
        when(mockParser.getFrame()).thenReturn(new Frame(ElementType.SD_FRAME));

        Frame result = mockParser.parseFrame();

        Assertions.assertEquals(ElementType.SD_FRAME, result.getType());
        Assertions.assertEquals(2, result.getElements().size());
        Assertions.assertTrue(result.getElements().stream().allMatch(element -> element.getType() == ElementType.MESSAGE));
        int remainingLineCount = input.size();
        Assertions.assertEquals(0, remainingLineCount);
    }

    @Test
    void parseFrame_whenFlatLoopFrameIsGiven_shouldParseIntoFlatLoopFrame() {
        List<String> input = new ArrayList<>(List.of(
                "loop 1000 times",
                "   Alice->Bob:Hi",
                "   Bob->Alice:Hey back",
                "end",
                "lines",
                "from",
                "outer",
                "frame"
        ));

        SymmetricalFrameParser mockParser = Mockito.mock(SymmetricalFrameParser.class);
        when(mockParser.parseFrame()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);
        when(mockParser.getType()).thenReturn(ElementType.LOOP_FRAME);
        when(mockParser.getFrame()).thenReturn(new Frame(ElementType.LOOP_FRAME));

        Frame result = mockParser.parseFrame();

        Assertions.assertEquals(ElementType.LOOP_FRAME, result.getType());
        Assertions.assertEquals("1000 times", result.getGuard());
        Assertions.assertEquals(2, result.getElements().size());
        Assertions.assertTrue(result.getElements().stream().allMatch(element -> element.getType() == ElementType.MESSAGE));
        int remainingLineCount = input.size();
        Assertions.assertEquals(4, remainingLineCount);
    }

    @Test
    void parseFrame_whenNestedSdFrameIsGiven_shouldParseIntoNestedFram2e() {
        List<String> input = new ArrayList<>(List.of(
                "@startuml",
                "Alice->Bob:Hi",
                "Bob->Alice:Hey back",
                "loop 1000 times",
                "   Alice->Bob: This will get noisy",
                "   Bob->Alice: I know!",
                "end",
                "@enduml"
        ));

        SymmetricalFrameParser mockParser = Mockito.mock(SymmetricalFrameParser.class);
        when(mockParser.parseFrame()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);
        when(mockParser.getType()).thenReturn(ElementType.SD_FRAME);
        when(mockParser.getFrame()).thenReturn(new Frame(ElementType.SD_FRAME));

        FrameParserFactory mockParserFactory = Mockito.mock(FrameParserFactory.class);
        when(mockParser.getFrameParserFactory()).thenReturn(mockParserFactory);

        FrameParser innerMockParser = Mockito.mock(FrameParser.class);
        Frame plantedInnerFrame = new Frame(ElementType.LOOP_FRAME);
        when(mockParserFactory.getFrameParser(ElementType.LOOP_FRAME, input)).thenReturn(innerMockParser);
        when(innerMockParser.parseFrame()).thenAnswer((Answer<Frame>) invocationOnMock -> {
            // remove loop frame's 4 lines
            IntStream.range(0, 4).forEach(i -> input.remove(0));
            return plantedInnerFrame;
        });

        Frame result = mockParser.parseFrame();

        Assertions.assertEquals(ElementType.SD_FRAME, result.getType());
        Assertions.assertEquals(3, result.getElements().size());
        Assertions.assertEquals(2, result.getElements().stream().filter(Objects::nonNull).filter(element -> element.getType() == ElementType.MESSAGE).count());

        Element loopElement = result.getElements().get(2);
        Assertions.assertSame(plantedInnerFrame, loopElement);
        int remainingLineCount = input.size();
        Assertions.assertEquals(0, remainingLineCount);
    }

    @Test
    void parseFrame_whenElseBlockIsGiven_shouldParseIntoFlatElseFrame() {
        List<String> input = new ArrayList<>(List.of(
                "else guard clause",
                "   Alice->Bob:Hi",
                "   Bob->Alice:Hey back"
        ));

        SymmetricalFrameParser mockParser = Mockito.mock(SymmetricalFrameParser.class);
        when(mockParser.parseFrame()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);
        when(mockParser.getType()).thenReturn(ElementType.ELSE_FRAME);
        when(mockParser.getFrame()).thenReturn(new Frame(ElementType.ELSE_FRAME));

        Frame result = mockParser.parseFrame();

        Assertions.assertEquals(ElementType.ELSE_FRAME, result.getType());
        Assertions.assertEquals("guard clause", result.getGuard());
        Assertions.assertEquals(2, result.getElements().size());
        Assertions.assertTrue(result.getElements().stream().allMatch(element -> element.getType() == ElementType.MESSAGE));
        int remainingLineCount = input.size();
        Assertions.assertEquals(0, remainingLineCount);
    }
}
