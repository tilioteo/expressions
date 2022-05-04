package com.tilioteo.expressions;

import static com.tilioteo.expressions.StringConstants.ERROR_UNEXPECTED_CHARACTER_FORMAT;

/**
 * @author Kamil Morong - Hypothesis
 */
public class UnexpectedCharException extends ExpressionException {

    public UnexpectedCharException(int position) {
        super(String.format(ERROR_UNEXPECTED_CHARACTER_FORMAT, position));
    }
}
