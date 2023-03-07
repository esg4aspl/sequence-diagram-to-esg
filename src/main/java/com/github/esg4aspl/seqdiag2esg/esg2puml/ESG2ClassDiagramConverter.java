package com.github.esg4aspl.seqdiag2esg.esg2puml;

import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.VertexRefinedByESG;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ESG2ClassDiagramConverter {
    private File dir;

    private ESG esg;

    public ESG2ClassDiagramConverter(File dir, ESG esg) {
        this.dir = dir;
        this.esg = esg;
    }

    public void convertAndWriteToFile() {
        File file = new File(dir.getAbsolutePath() + File.separator + esg.getName() + ".puml");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@startuml\n");
        esg.getVertexList().forEach(vertex -> stringBuilder.append("class \"").append(vertex.getEvent().getName()).append("\"\n"));
        esg.getEdgeList().forEach(edge -> this.appendEdge(stringBuilder, edge).append('\n'));
        stringBuilder.append("@enduml\n");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            // do not want to handle exception in the lambda below...
            throw new IllegalArgumentException(e.getMessage());
        }

        esg.getVertexList().stream().filter(VertexRefinedByESG.class::isInstance).map(VertexRefinedByESG.class::cast).map(vertexRefinedByESG -> new ESG2ClassDiagramConverter(dir, vertexRefinedByESG.getSubESG())).forEach(ESG2ClassDiagramConverter::convertAndWriteToFile);
    }

    private StringBuilder appendEdge(StringBuilder stringBuilder, Edge edge) {
        return stringBuilder.append("\"").append(edge.getSource().getEvent().getName()).append("\"").append(" -> ").append("\"").append(edge.getTarget().getEvent().getName()).append("\"");
    }
}
