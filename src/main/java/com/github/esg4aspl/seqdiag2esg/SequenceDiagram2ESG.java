package com.github.esg4aspl.seqdiag2esg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.esg4aspl.seqdiag2esg.converter.FrameConverterFactory;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.esg2puml.ESG2GraphvizConverter;
import com.github.esg4aspl.seqdiag2esg.parser.FrameParserFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import picocli.CommandLine;
import tr.edu.iyte.esg.model.ESG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
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
        if(esgOutputFile != null){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(esgOutputFile, esg);
        }
        if (esgVisualOutputDirectory != null) {
            convertESGToClassDiagram();
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
        esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0)).convert();
    }

    private void convertESGToClassDiagram() {
        ESG2GraphvizConverter classDiagramConverter = new ESG2GraphvizConverter(esgVisualOutputDirectory, esg);
        classDiagramConverter.convertAndWriteToFile();
    }
}
