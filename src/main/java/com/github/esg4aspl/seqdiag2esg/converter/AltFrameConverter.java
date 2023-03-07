package com.github.esg4aspl.seqdiag2esg.converter;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.apache.commons.lang3.mutable.MutableInt;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.Vertex;

public class AltFrameConverter extends AbstractFrameConverter {
    @Override
    public ESG convert() {

        Vertex beginVertex = createVertex(createEvent("["));
        getEsg().addVertex(beginVertex);
        Vertex endVertex = createVertex(createEvent("]"));
        getEsg().addVertex(endVertex);

        getFrame().getElements().stream()
                .filter(element -> element.getType() == ElementType.ELSE_FRAME) // must only have else frames by construction, check anyway
                .map(elseElement -> FrameConverterFactory.getInstance().getFrameConverter(ElementType.ELSE_FRAME, (Frame) elseElement, getCounter(), getPseudoEventPrefix()))
                .map(FrameConverter::convert)
                .forEach(esg -> {
                    // refine alt by elses:
                    // for each else frame in the surrounding alt frame, dump all their components into the alt frame's ESG
                    esg.getVertexList().stream().filter(vertex -> !isPseudoVertex(vertex))
                            .forEach(vertex -> getEsg().addVertex(vertex));
                    esg.getEdgeList().stream().filter(edge -> !isEdgeConnectedToPsuedoVertex(edge))
                            .forEach(edge -> getEsg().addEdge(edge));
                    // and connect start and end vertices to alt
                    esg.getEntryVertexSet().forEach(entryVertex -> getEsg().addEdge(createEdge(beginVertex, entryVertex)));
                    esg.getExitVertexSet().forEach(exitVertex -> getEsg().addEdge(createEdge(exitVertex, endVertex)));
                });

        return getEsg();
    }

    @Override
    protected void convertBodyToChain() {
        // override and disable it. Will do it manually at convert()
    }

    public AltFrameConverter(ElementType type, Frame frame, MutableInt esgCounter, String pseudoEventPrefix) {
        super(type, frame, esgCounter, pseudoEventPrefix);
    }

    private boolean isEdgeConnectedToPsuedoVertex(Edge edge) {
        return isPseudoVertex(edge.getSource()) || isPseudoVertex(edge.getTarget());
    }

    private boolean isPseudoVertex(Vertex vertex) {
        return vertex.isPseudoEndVertex() || vertex.isPseudoStartVertex();
    }
}
