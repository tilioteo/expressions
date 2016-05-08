/**
 * 
 */
package com.tilioteo.expressions;

import java.io.Serializable;

/**
 * @author Kamil Morong - Hypothesis
 *
 */
public interface HasReference extends Serializable {

	public void setReference(Primitive reference);

}
