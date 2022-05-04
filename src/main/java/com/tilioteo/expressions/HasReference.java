package com.tilioteo.expressions;

import java.io.Serializable;

/**
 * @author Kamil Morong - Hypothesis
 */
public interface HasReference extends Serializable {

    void setReference(Primitive reference);

}
