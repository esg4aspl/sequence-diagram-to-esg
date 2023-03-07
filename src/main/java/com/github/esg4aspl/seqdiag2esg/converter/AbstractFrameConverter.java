package com.github.esg4aspl.seqdiag2esg.converter;

import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Element;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Message;
import org.apache.commons.lang3.mutable.MutableInt;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.EdgeSimple;
import tr.edu.iyte.esg.model.Event;
import tr.edu.iyte.esg.model.EventSimple;
import tr.edu.iyte.esg.model.Vertex;
import tr.edu.iyte.esg.model.VertexRefinedByESG;
import tr.edu.iyte.esg.model.VertexSimple;

import java.util.Optional;

public abstract class AbstractFrameConverter implements FrameConverter {

    private Frame frame;

    private MutableInt counter;

    private FrameConverterFactory frameConverterFactory;

    private ElementType elementType;

    private ESG esg;

    private Optional<Vertex> firstVertexInSubChain = Optional.empty();

    private Optional<Vertex> lastVertexInSubChain = Optional.empty();

    private String pseudoEventPrefix = "";

    protected AbstractFrameConverter(ElementType type, Frame frame, MutableInt counter, String pseudoEventPrefix) {
        this.frame = frame;
        this.elementType = type;
        this.counter = counter;
        this.frameConverterFactory = FrameConverterFactory.getInstance();
        this.esg = new ESG(getCounter().getValue(), getEsgName());
        this.pseudoEventPrefix = pseudoEventPrefix;
        convertBodyToChain();
    }

    protected Frame getFrame() {
        return frame;
    }

    private String getEsgName() {
        return getElementType().toString() + counter.getAndIncrement();
    }

    protected MutableInt getCounter() {
        return counter;
    }

    protected FrameConverterFactory getFrameConverterFactory() {
        return frameConverterFactory;
    }

    public ElementType getElementType() {
        return elementType;
    }

    protected ESG getEsg() {
        return esg;
    }

    public String getPseudoEventPrefix() {
        return pseudoEventPrefix;
    }

    protected ESG convertSubFrame(Frame subFrame) {
        return getFrameConverterFactory().getFrameConverter(subFrame.getType(), subFrame, counter, pseudoEventPrefix).convert();
    }

    protected void convertBodyToChain() {
        Optional<Vertex> prevVertex = Optional.empty();
        for (Element element : getFrame().getElements()) {
            Vertex vertex;
            if (element.getType() == ElementType.MESSAGE) {
                Message message = (Message) element;
                Event event = createEvent(message.getAsObjectInvocation());
                vertex = createVertex(event);
                getEsg().addVertex(vertex);
            } else {
                ESG subESG = convertSubFrame((Frame) element);
                Event event = createEvent(element.toString());
                vertex = new VertexRefinedByESG(counter.getAndIncrement(), event, subESG);
                getEsg().addVertex(vertex);
            }
            prevVertex.ifPresent(vertex1 -> getEsg().addEdge(new EdgeSimple(5, vertex1, vertex)));
            prevVertex = Optional.of(vertex);
            if (firstVertexInSubChain.isEmpty()) {
                firstVertexInSubChain = Optional.of(vertex);
            }
            lastVertexInSubChain = Optional.of(vertex);
        }
    }

    protected Optional<Vertex> getFirstVertexInSubChain() {
        return firstVertexInSubChain;
    }

    protected Optional<Vertex> getLastVertexInSubChain() {
        return lastVertexInSubChain;
    }


    protected final EventSimple createEvent(String name) {
        return new EventSimple(counter.getAndIncrement(), name);
    }

    protected final VertexSimple createVertex(Event event) {
        return new VertexSimple(counter.getAndIncrement(), event);
    }

    protected final EdgeSimple createEdge(Vertex source, Vertex target) {
        return new EdgeSimple(counter.getAndIncrement(), source, target);
    }
}
