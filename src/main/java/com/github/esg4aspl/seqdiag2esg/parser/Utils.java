package com.github.esg4aspl.seqdiag2esg.parser;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Message;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.SequenceDiagramObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Utils {

    private Utils() {
    }

    private static final Set<Pair<String, Boolean>> messageArrowHeads = new HashSet<>();

    private static final Set<String> messageEndModifiers = Set.of("o", "x", "");

    private static final Set<String> messageMidParts = Set.of("-", "--");

    private static final Map<String, Boolean> messageArrows;

    static {
        messageArrowHeads.add(new ImmutablePair<>(">", true));
        messageArrowHeads.add(new ImmutablePair<>("/", true));
        messageArrowHeads.add(new ImmutablePair<>("\\", true));
        messageArrowHeads.add(new ImmutablePair<>("<", false));
        messageArrowHeads.add(new ImmutablePair<>("/", false));
        messageArrowHeads.add(new ImmutablePair<>("\\", false));

        // sort arrows in the order of decreasing length
        // want to match the longest possible arrow, consuming all arrow chars
        messageArrows = new TreeMap<>(Comparator.comparing(String::length).reversed().thenComparing(String::compareTo));
        for (String midPart : messageMidParts) {
            for (String endModifier : messageEndModifiers) {
                for (Pair<String, Boolean> arrowHead : messageArrowHeads) {
                    if (Boolean.TRUE.equals(arrowHead.getRight())) {
                        messageArrows.put(midPart + arrowHead.getKey() + endModifier, true);
                        messageArrows.put(midPart + arrowHead.getKey() + arrowHead.getKey() + endModifier, true);
                    } else {
                        messageArrows.put(endModifier + arrowHead.getKey() + midPart, false);
                        messageArrows.put(endModifier + arrowHead.getKey() + arrowHead.getKey() + midPart, false);
                    }
                }
            }
        }
    }


    private static final Map<String, ElementType> frameIdentifiers = Arrays.stream(ElementType.values()).filter(type -> type.getText() != null).collect(Collectors.toMap(ElementType::getText, elementType -> elementType));

    static Message parseIntoMessage(String line) {
        line = sanitizeMessageLine(line);
        return parseIntoMessageFromSanitizedLine(line);
    }

    static Message parseIntoMessageFromSanitizedLine(String line) {
        line = line.trim();
        Message parsedMessage = new Message();
        String[] objectsAndBody = line.split(":");
        if (objectsAndBody.length > 1) {
            parsedMessage.setBody(objectsAndBody[1].trim());
        }
        // TODO: change split regex according to sanitizer implementation!
        String sourceAndTarget = objectsAndBody[0];
        Optional<Map.Entry<String, Boolean>> matchingEntry = messageArrows.entrySet().stream()
                .filter(entry -> sourceAndTarget.contains(entry.getKey())).findFirst();
        if (matchingEntry.isEmpty()) {
            return null;
        }
        String delimiter = matchingEntry.get().getKey();
        String[] leftAndRightOfDelimiter = sourceAndTarget.split(delimiter);
        String lhs = leftAndRightOfDelimiter[0].trim();
        lhs = removeMessageModifiersToRightOfParticipant(lhs);
        String rhs = leftAndRightOfDelimiter[1].trim();
        rhs = removeMessageModifiersToRightOfParticipant(rhs);

        if (Boolean.TRUE.equals(matchingEntry.get().getValue())) {
            parsedMessage.setSource(new SequenceDiagramObject(lhs));
            parsedMessage.setTarget(new SequenceDiagramObject(rhs));
        } else {
            parsedMessage.setSource(new SequenceDiagramObject(rhs));
            parsedMessage.setTarget(new SequenceDiagramObject(lhs));
        }
        return parsedMessage;
    }

    static String removeMessageModifiersToRightOfParticipant(String rhs) {
        if (rhs.isEmpty()) {
            return rhs;
        }
        if (rhs.startsWith("\"")) {
            int quoteEnd = rhs.lastIndexOf("\"");
            if (quoteEnd <= 0) {
                throw new IllegalArgumentException("Unmatched \" in participant:" + rhs);
            }
            return rhs.substring(1, quoteEnd);
        }
        String[] splitRhs = rhs.split("\\W");
        if(splitRhs.length == 0){
            return rhs;
        } else {
            return splitRhs[0];
        }
    }

    static String sanitizeMessageLine(String line) {
        line = removeColorModifiers(line);
        return line;
    }

    static String removeColorModifiers(String text) {
        // remove color modifiers in brackets: -[#ff00ff]>
        text = text.replaceAll("\\[#\\w*]", "");
        // remove color modifiers after lifeline activations: --#ff00ff
        text = text.replaceAll("#\\w*", "");
        return text;
    }

    static boolean isFrameBeginning(String line) {
        line = line.trim();
        return (frameIdentifiers.keySet().stream().filter(Objects::nonNull).anyMatch(line::startsWith));
    }

    static ElementType getFrameTypeFromFirstLine(String line) {
        line = line.trim();
        String[] words = line.split("\\s");
        ElementType type = frameIdentifiers.get(words[0]);
        if (type == null) {
            throw new IllegalArgumentException("Line does not match any known frame");
        }
        return type;
    }

    static boolean isFrameEnd(String line) {
        line = line.trim();
        // multiline notes end with 'end note'
        // exclude them
        // TODO: consider adding a new frame parser for multiline entities such as note that would consume related lines
        return line.startsWith("end") && !line.equals("end note") && !line.equals("end box");
    }

    static String getFrameGuard(ElementType type, String line) {
        if (type.getText() == null) {
            return null;
        }
        line = line.trim();
        if (!line.startsWith(type.getText())) {
            return null;
        }
        return line.substring(type.getText().length()).trim();
    }
}
