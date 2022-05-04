package com.tilioteo.expressions;

/**
 * @author Kamil Morong - Hypothesis
 */
public class MethodArgument implements HasOperatorNode {

    private final String text;

    public MethodArgument(String text) {
        this.text = text;
    }

    // not used
    public OperatorNode getOperatorNode() {
        return null;
    }

    public String getText() {
        return text;
    }

}
