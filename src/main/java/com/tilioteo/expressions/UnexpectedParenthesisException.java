/**
 * 
 */
package com.tilioteo.expressions;

/**
 * @author Kamil Morong - Hypothesis
 *
 */
@SuppressWarnings("serial")
public class UnexpectedParenthesisException extends ExpressionException {

	public UnexpectedParenthesisException(int position) {
		super(String.format(StringConstants.ERROR_UNEXP_PARENTHESIS_FMT, position));
	}

}
