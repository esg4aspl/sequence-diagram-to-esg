package com.github.esg4aspl.seqdiag2esg.entity.sequencediagram;

import java.util.Objects;

public class Message extends Element{
    private SequenceDiagramObject source;
    private SequenceDiagramObject target;
    private String body;

    public Message(String source, String target, String body) {
        this();
        this.source = new SequenceDiagramObject(source);
        this.target = new SequenceDiagramObject(target);
        this.body = body;
    }

    public Message() {
        super(ElementType.MESSAGE);
    }

    public void setSource(SequenceDiagramObject source) {
        this.source = source;
    }

    public void setTarget(SequenceDiagramObject target) {
        this.target = target;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAsObjectInvocation() {
        return target.getName() + "." + getBody();
    }

    public SequenceDiagramObject getSource() {
        return source;
    }

    public SequenceDiagramObject getTarget() {
        return target;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(source, message.source) && Objects.equals(target, message.target) && Objects.equals(body, message.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, body);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", source=" + source +
                ", target=" + target +
                ", body='" + body + '\'' +
                '}';
    }
}
