package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Element;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltFrameParserTest {

    List<String> altFrameFragmentBeginningWithAltAndEndingWithElse = List.of(
            "alt guard text",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "else rest of the next block",
            "more messages go here"
    );

    List<String> altFrameFragmentBeginningWithAltAndEndingWithEnd = List.of(
            "alt guard text",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "end",
            "lines from outside frame"
    );

    List<String> altFrameFragmentBeginningWithElseAndEndingWithAnotherElse = List.of(
            "else guard text",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "Bob -> Alice: Auth failed",
            "else other guard text",
            "lines from other else block"
    );

    List<String> altFrameWithOneElse = List.of(
            "alt guard text1",
            "PaymentServer -> MySQL: create topup statement",
            "PaymentServer --> User: topup successful",
            "else guard text2",
            "PaymentServer --> User: unauthorize",
            "end",
            "lines from other frames",
            "lines from other frames"
    );

    @Test
    void getLinesForNextBlock_whenAltFrameFragmentBeginningWithAltAndEndingWithElseIsGiven_shouldParseIntoFlatLoopFrame() {
        List<String> input = new ArrayList<>(altFrameFragmentBeginningWithAltAndEndingWithElse);

        AltFrameParser mockParser = Mockito.mock(AltFrameParser.class);
        when(mockParser.getLinesForNextBlock()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);

        List<String> result = mockParser.getLinesForNextBlock();

        int remainingLineCount = input.size();
        Assertions.assertEquals(2, remainingLineCount);
        Assertions.assertEquals("else guard text", result.get(0));
        Assertions.assertEquals(altFrameFragmentBeginningWithAltAndEndingWithElse.subList(1, 5), result.subList(1, 5));
    }

    @Test
    void getLinesForNextBlock_whenAltFrameFragmentBeginningWithAltAndEndingWithEndIsGiven_shouldParseIntoFlatLoopFrame() {

        List<String> input = new ArrayList<>(altFrameFragmentBeginningWithAltAndEndingWithEnd);

        AltFrameParser mockParser = Mockito.mock(AltFrameParser.class);
        when(mockParser.getLinesForNextBlock()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);

        List<String> result = mockParser.getLinesForNextBlock();

        int remainingLineCount = input.size();
        Assertions.assertEquals(1, remainingLineCount);
        Assertions.assertEquals("else guard text", result.get(0));
        Assertions.assertEquals(altFrameFragmentBeginningWithAltAndEndingWithEnd.subList(1, 5), result.subList(1, 5));
    }

    @Test
    void getLinesForNextBlock_whenAltFrameFragmentBeginningWithElseAndEndingWithAnotherElseIsGiven_shouldParseIntoFlatLoopFrame() {
        List<String> input = new ArrayList<>(altFrameFragmentBeginningWithElseAndEndingWithAnotherElse);

        AltFrameParser mockParser = Mockito.mock(AltFrameParser.class);
        when(mockParser.getLinesForNextBlock()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);

        List<String> result = mockParser.getLinesForNextBlock();

        int remainingLineCount = input.size();
        Assertions.assertEquals(2, remainingLineCount);
        Assertions.assertEquals(altFrameFragmentBeginningWithElseAndEndingWithAnotherElse.subList(0, 5), result);
    }

    @Test
    void getLinesForNextBlock_whenAltFrameWithOneElse_shouldCallElseParserTwice() {
        List<String> input = new ArrayList<>(altFrameWithOneElse);

        AltFrameParser mockParser = Mockito.mock(AltFrameParser.class);
        when(mockParser.getLinesForNextBlock()).thenCallRealMethod();
        when(mockParser.getLines()).thenReturn(input);

        List<String> result = mockParser.getLinesForNextBlock();

        int remainingLineCount = input.size();
        Assertions.assertEquals(5, remainingLineCount);
        Assertions.assertEquals("else guard text1", result.get(0));
        Assertions.assertEquals(altFrameWithOneElse.subList(1, 3), result.subList(1,3));

        result = mockParser.getLinesForNextBlock();

        remainingLineCount = input.size();
        Assertions.assertEquals(2, remainingLineCount);
        Assertions.assertEquals(altFrameWithOneElse.subList(3, 5), result);

        result = mockParser.getLinesForNextBlock();

        remainingLineCount = input.size();
        Assertions.assertEquals(2, remainingLineCount);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void parseFrame_whenExecuted_callAnotherParserUntilNextBlockReturnsEmpty() {

        AltFrameParser mockParser = Mockito.mock(AltFrameParser.class);
        List<String> l1 = List.of("1");
        List<String> l2 = List.of("2");
        List<String> l3 = List.of("3");


        when(mockParser.getLinesForNextBlock()).thenReturn(l1).thenReturn(l2).thenReturn(l3).thenReturn(Collections.emptyList());

        Frame mockFrame = Mockito.mock(Frame.class);

        @SuppressWarnings("unchecked")
        List<Element> mockFrameElements = (List<Element>) Mockito.mock(List.class);
        when(mockFrame.getElements()).thenReturn(mockFrameElements);
        when(mockParser.getFrame()).thenReturn(mockFrame);
        when(mockParser.parseFrame()).thenCallRealMethod();

        FrameParserFactory mockParserFactory = Mockito.mock(FrameParserFactory.class);
        when(mockParser.getFrameParserFactory()).thenReturn(mockParserFactory);
        FrameParser mockInnerParser = Mockito.mock(FrameParser.class);
        when(mockParserFactory.getFrameParser(any(), any())).thenReturn(mockInnerParser);
        Frame mockInnerFrame = Mockito.mock(Frame.class);
        when(mockInnerParser.parseFrame()).thenReturn(mockInnerFrame);

        Frame result = mockParser.parseFrame();

        verify(mockParser, times(4)).getLinesForNextBlock();
        verify(mockFrameElements, times(3)).add(mockInnerFrame);
        verify(mockParserFactory).getFrameParser(ElementType.ELSE_FRAME, l1);
        verify(mockParserFactory).getFrameParser(ElementType.ELSE_FRAME, l2);
        verify(mockParserFactory).getFrameParser(ElementType.ELSE_FRAME, l3);
        Assertions.assertEquals(mockFrame, result);
    }
}
