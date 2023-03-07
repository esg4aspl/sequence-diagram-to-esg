package com.github.esg4aspl.seqdiag2esg.converter;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import org.apache.commons.lang3.mutable.MutableInt;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Vertex;

public class SdFrameConverter extends AbstractFrameConverter {
    @Override
    public ESG convert() {

        Vertex beginVertex = createVertex(createEvent("["));
        getEsg().addVertex(beginVertex);
        Vertex endVertex = createVertex(createEvent("]"));
        getEsg().addVertex(endVertex);

        getFirstVertexInSubChain().ifPresent(vertex -> getEsg().addEdge(createEdge(beginVertex, vertex)));
        getLastVertexInSubChain().ifPresentOrElse(vertex -> getEsg().addEdge(createEdge(vertex, endVertex)),
                () -> getEsg().addEdge(createEdge(beginVertex, endVertex)));

        return getEsg();
    }

    public SdFrameConverter(ElementType type, Frame frame, MutableInt esgCounter, String pseudoEventPrefix) {
        super(type, frame, esgCounter, pseudoEventPrefix);
    }
}
