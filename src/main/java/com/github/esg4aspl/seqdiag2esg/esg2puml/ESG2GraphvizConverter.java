package com.github.esg4aspl.seqdiag2esg.esg2puml;

import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.Vertex;
import tr.edu.iyte.esg.model.VertexRefinedByESG;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ESG2GraphvizConverter {
    private File dir;

    private ESG esg;

    public ESG2GraphvizConverter(File dir, ESG esg) {
        this.dir = dir;
        this.esg = esg;
    }


    public void convertAndWriteToFile() {
        File file = new File(dir.getAbsolutePath() + File.separator + esg.getName() + ".gv");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph{\n");
        esg.getVertexList().forEach(vertex -> stringBuilder.append(vertex.getID()).append(" [label=\"")
                .append(this.getDisplayName(vertex)).append("\"]\n"));
        esg.getEdgeList().forEach(edge -> this.appendEdge(stringBuilder, edge).append('\n'));
        stringBuilder.append("}\n");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            // do not want to handle exception in the lambda below...
            throw new IllegalArgumentException(e.getMessage());
        }

        esg.getVertexList().stream().filter(VertexRefinedByESG.class::isInstance).map(VertexRefinedByESG.class::cast)
                .map(vertexRefinedByESG -> new ESG2GraphvizConverter(dir, vertexRefinedByESG.getSubESG()))
                .forEach(ESG2GraphvizConverter::convertAndWriteToFile);
    }

    private StringBuilder appendEdge(StringBuilder stringBuilder, Edge edge) {
        return stringBuilder.append(edge.getSource().getID()).append(" -> ").append(edge.getTarget().getID());
    }

    private String getDisplayName(Vertex esg) {
        if (esg instanceof VertexRefinedByESG) {
            return ((VertexRefinedByESG) esg).getSubESG().getName();
        } else {
            return esg.getEvent().getName();
        }
    }
}
