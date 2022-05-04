package com.tilioteo.expressions;

import java.io.Serializable;

/**
 * @author Kamil Morong - Hypothesis
 */
interface HasOperatorNode extends Serializable {
    OperatorNode getOperatorNode();
}
