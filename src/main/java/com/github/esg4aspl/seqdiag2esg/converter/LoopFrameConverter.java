package com.github.esg4aspl.seqdiag2esg.converter;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.apache.commons.lang3.mutable.MutableInt;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Vertex;

public class LoopFrameConverter extends AbstractFrameConverter {
    @Override
    public ESG convert() {

        Vertex beginVertex = createVertex(createEvent("["));
        getEsg().addVertex(beginVertex);
        Vertex endVertex = createVertex(createEvent("]"));
        getEsg().addVertex(endVertex);
        Vertex guardVertex = createVertex(createEvent(getPseudoEventPrefix() + getFrame().getGuard()));
        getEsg().addVertex(guardVertex);
        Vertex guardNotVertex = createVertex(createEvent(getPseudoEventPrefix() + "NOT " + getFrame().getGuard()));
        getEsg().addVertex(guardNotVertex);

        getEsg().addEdge(createEdge(beginVertex, guardVertex));
        getEsg().addEdge(createEdge(beginVertex, guardNotVertex));
        getEsg().addEdge(createEdge(guardNotVertex, endVertex));
        getFirstVertexInSubChain().ifPresent(vertex -> getEsg().addEdge(createEdge(guardVertex, vertex)));
        getLastVertexInSubChain().ifPresent(vertex -> getEsg().addEdge(createEdge(vertex, guardVertex)));
        getLastVertexInSubChain().ifPresent(vertex -> getEsg().addEdge(createEdge(vertex, guardNotVertex)));

        return getEsg();
    }

    public LoopFrameConverter(ElementType type, Frame frame, MutableInt esgCounter, String pseudoEventPrefix) {
        super(type, frame, esgCounter, pseudoEventPrefix);
    }
}
