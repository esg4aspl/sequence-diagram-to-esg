package com.github.esg4aspl.seqdiag2esg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.esg4aspl.seqdiag2esg.converter.FrameConverterFactory;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.esg2puml.ESG2GraphvizConverter;
import com.github.esg4aspl.seqdiag2esg.esgutils.EsgFlattener;
import com.github.esg4aspl.seqdiag2esg.esgutils.VertexRemover;
import com.github.esg4aspl.seqdiag2esg.parser.FrameParserFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import picocli.CommandLine;
import tr.edu.iyte.esg.eventsequence.EventSequence;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.webapi.TestGenerationAPI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@CommandLine.Command(name = "SequenceDiagram2ESG", mixinStandardHelpOptions = true, version = "1.0.SNAPSHOT", description = "Convert PlantUML Sequence Diagram to ESG.")
public class SequenceDiagram2ESG implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Sequence Diagram file.")
    private File inputFile;

    @CommandLine.Option(names = {"-f", "--export-frame"}, description = "Frame json output filename.", help = true)
    private File outputFile;

    @CommandLine.Option(names = {"-o", "--esg-output"}, defaultValue = "ESG.json", description = "ESG json output filename (ESG.json if not given).", help = true)
    private File esgOutputFile;

    @CommandLine.Option(names = {"-d", "--visual-dir"}, description = "Output directory for ESG visualizations.", help = true)
    private File esgVisualOutputDirectory;

    @CommandLine.Option(names = {"-p", "--pseudo-prefix"}, defaultValue = "", description = "Prefix for pseudo event messages", help = true)
    private String pseudoEventPrefix;

    @CommandLine.Option(names = {"--test-sequence-output"}, description = "Test sequence output filename.", help = true)
    private File testSequenceOutputFile;

    @CommandLine.Option(names = {"-t", "--tuple-length"}, description = "Test sequence generation tuple lengths", help = true)
    Set<Integer> tupleLengths = new HashSet<>();

    @CommandLine.Option(names = {"-x", "--timeout"}, defaultValue = "30", description = "Test sequence generation timeout duration in seconds", help = true)
    Integer testSeqGenTimeoutInSeconds = 30;

    private Frame frame;

    private ESG esg;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SequenceDiagram2ESG()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Hi!");
        frame = convertSequenceDiagramFileToFrame();
        if (outputFile != null) {
            exportFrameAsJson();
        }
        convertFrameToESG();
        if (esgOutputFile != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(esgOutputFile, esg);
        }
        if (esgVisualOutputDirectory != null) {
            convertESGToClassDiagram();
        }
        if (testSequenceOutputFile != null) {
            generateTestSequencesAndExportToFile();
        }
        return 0;
    }

    private Frame convertSequenceDiagramFileToFrame() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)))) {
            return FrameParserFactory.getInstance().getFrameParser(ElementType.SD_FRAME, reader.lines().collect(Collectors.toList())).parseFrame();
        }
    }

    private void exportFrameAsJson() throws IOException {
        ObjectWriter mapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = mapper.writeValueAsString(frame);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(json);
        }
    }

    private void convertFrameToESG() {
        esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0), pseudoEventPrefix).convert();
    }

    private void convertESGToClassDiagram() {
        ESG2GraphvizConverter classDiagramConverter = new ESG2GraphvizConverter(esgVisualOutputDirectory, esg);
        classDiagramConverter.convertAndWriteToFile();
    }

    private void generateTestSequencesAndExportToFile() throws IOException {
        EsgFlattener esgFlattener = new EsgFlattener(esg);
        esgFlattener.flatten();
        if (!pseudoEventPrefix.isEmpty()) {
            VertexRemover.removeByPredicate(esg, vertex -> vertex.getEvent().getName().startsWith(pseudoEventPrefix));
        }

        Map<Integer, Set<EventSequence>> eventSequencesByTupleLength = tupleLengths
                .stream()
                .collect(Collectors.toMap(Function.identity(),
                        l -> generateEventSequenceWithTimeout(l, esg, testSeqGenTimeoutInSeconds)));

        StringBuilder stringBuilder = new StringBuilder();
        eventSequencesByTupleLength.forEach((l, eventSequences) -> {
            int sequenceCount = eventSequences.size();
            int eventCount = eventSequences.stream().map(a -> a.getEventSequence().size()).reduce(0, Integer::sum);
            stringBuilder.append("TupleLength:").append(l).append(' ');
            stringBuilder.append("SequenceCount:").append(sequenceCount).append(' ');
            stringBuilder.append("EventCount:").append(eventCount).append('\n');
            final MutableInt sequenceNumber = new MutableInt(0);
            eventSequences.stream()
                    .map(es -> es.getEventSequence().stream().map(v -> v.getEvent().getName())
                            .collect(Collectors.joining(",")))
                    .forEach(esString -> stringBuilder.append("Sequence#")
                            .append(sequenceNumber.getAndIncrement()).append(':')
                            .append(esString).append('\n'));
        });

        try (FileWriter writer = new FileWriter(testSequenceOutputFile)) {
            writer.write(stringBuilder.toString());
        }
    }

    private Set<EventSequence> generateEventSequenceWithTimeout(int covLength, ESG esg, int timeoutInSeconds) {
        // to avoid having to wait indefinitely for arbitrarily long sequence generations, start the generation
        // in a second thread and wait for results with a timeout. Throw exc if task timeouts.
        TestGenerationAPI testGenerationAPI = new TestGenerationAPI();

        FutureTask<Set<EventSequence>> futureTask = new FutureTask<>(() -> testGenerationAPI.generateTestSequences(esg, covLength));
        Thread t = new Thread(futureTask);
        t.start();
        try {
            return futureTask.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            t.interrupt();
            throw new RuntimeException(e);
        }
    }

}
