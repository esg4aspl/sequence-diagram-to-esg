package com.github.esg4aspl.seqdiag2esg.parser;


import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class UtilsTest {


    private static Stream<Arguments> provideStringsForColorCleaner() {
        return Stream.of(
                Arguments.of("@plantuml", "@plantuml"),
                Arguments.of("Alice -> Bob: Authentication Request", "Alice -> Bob: Authentication Request"),
                Arguments.of("    Bob <[#blue]- Alice    ", "    Bob <- Alice    "),
                Arguments.of("return asdf", "return asdf"),
                Arguments.of("bob -> bib ++  #005500 : hello", "bob -> bib ++   : hello"),
                Arguments.of("@plantuml", "@plantuml")

        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForColorCleaner")
    void removeColorModifiers_whenTextHasColorModifiers_shouldRemoveColorModifiers(String input, String expected) {
        Assertions.assertEquals(expected, Utils.removeColorModifiers(input));
    }

    private static Stream<Arguments> provideStringsForParseIntoTextFromSanitizedLine() {
        return Stream.of(
                Arguments.of("@plantuml", null),
                Arguments.of("Alice -> Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice -> Bob:", new Message("Alice", "Bob", null)),
                Arguments.of("    Bob -> Alice    ", new Message("Bob", "Alice", null)),
                Arguments.of("return asdf", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForParseIntoTextFromSanitizedLine")
    void parseIntoMessageFromSanitizedLine_whenTextHasActivation_shouldRemoveActivation(String input, Message expected) {
        Assertions.assertEquals(expected, Utils.parseIntoMessageFromSanitizedLine(input));
    }

    private static Stream<Arguments> provideStringsForParseIntoMessage() {
        return Stream.of(
                Arguments.of("@plantuml", null),
                Arguments.of("Alice -> Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice ->o Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice --> Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice -->o Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice -->> Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice -->>o Bob: Authentication Request", new Message("Alice", "Bob", "Authentication Request")),
                Arguments.of("Alice <- Bob: Authentication Request", new Message("Bob", "Alice", "Authentication Request")),
                Arguments.of("Alice <- Bob with many unknown modifiers: Authentication Request", new Message("Bob", "Alice", "Authentication Request")),
                Arguments.of("Alice <- \"Bob with spaces\": Authentication Request", new Message("Bob with spaces", "Alice", "Authentication Request")),
                Arguments.of("\" Alice with spaces \" <- \"Bob with spaces\": Authentication Request", new Message("Bob with spaces", " Alice with spaces ", "Authentication Request")),
                Arguments.of("    Bob <[#blue]- Alice    ", new Message("Alice", "Bob", null)),
                Arguments.of("return asdf", null),
                Arguments.of("bob -> bib ++  #005500 : hello", new Message("bob", "bib", "hello")),
                Arguments.of("bob -> bib --++  #005500 : hello", new Message("bob", "bib", "hello")),
                Arguments.of("bob -> bib --  #005500 : hello", new Message("bob", "bib", "hello")),
                Arguments.of("bob -> george ** : create", new Message("bob", "george", "create")),
                Arguments.of("bob -> george !! : delete", new Message("bob", "george", "delete")),
                Arguments.of("@plantuml", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForParseIntoMessage")
    void parseIntoMessage_parameterizedTest(String input, Message expected) {
        Assertions.assertEquals(expected, Utils.parseIntoMessage(input));
    }

    @Test
    void removeMessageModifiersToRightOfParticipant_whenUnbalancedQuoteMarks_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> Utils.removeMessageModifiersToRightOfParticipant("\" unbalanced"));
    }

    private static Stream<Arguments> provideStringsForRemoveMessageModifiersToRightOfParticipant_withQuotes() {
        return Stream.of(
                Arguments.of("\"Alice \"", "Alice "),
                Arguments.of("\"Alice 123 long!\" ++--", "Alice 123 long!")
                );
    }


    @ParameterizedTest
    @MethodSource("provideStringsForRemoveMessageModifiersToRightOfParticipant_withQuotes")
    void removeMessageModifiersToRightOfParticipant_whenWithQuotes_shouldReturnInsideQuotes(String input, String expected) {
        Assertions.assertEquals(expected, Utils.removeMessageModifiersToRightOfParticipant(input));
    }

    private static Stream<Arguments> provideStringsForRemoveMessageModifiersToRightOfParticipant_noQuotes() {
        return Stream.of(
                Arguments.of("Alice ", "Alice"),
                Arguments.of("Alice ++", "Alice"),
                Arguments.of("Alice --", "Alice"),
                Arguments.of("Alice ++--", "Alice"),
                Arguments.of("Alice123 a lot of stuff", "Alice123")
        );
    }


    @ParameterizedTest
    @MethodSource("provideStringsForRemoveMessageModifiersToRightOfParticipant_noQuotes")
    void removeMessageModifiersToRightOfParticipant_whenNoQuotes_shouldReturnFirstWord(String input, String expected) {
        Assertions.assertEquals(expected, Utils.removeMessageModifiersToRightOfParticipant(input));
    }
}
