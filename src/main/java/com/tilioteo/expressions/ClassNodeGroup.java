package com.tilioteo.expressions;

/**
 * @author Kamil Morong - Hypothesis
 */
public class ClassNodeGroup extends OperatorNodeGroup {

    public ClassNodeGroup(int beginPosition, int endPosition, int level, OperatorNodeGroup parent) {
        super(beginPosition, endPosition, level, parent);
    }

    public MethodArgumentGroup getArgumentGroup() {
        for (HasOperatorNode obj : this) {
            if (obj instanceof MethodArgumentGroup)
                return (MethodArgumentGroup) obj;
        }
        return null;
    }

}
