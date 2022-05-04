package com.tilioteo.expressions;

import static com.tilioteo.expressions.StringConstants.ERROR_UNEXPECTED_PARENTHESIS_FORMAT;

/**
 * @author Kamil Morong - Hypothesis
 */
public class UnexpectedParenthesisException extends ExpressionException {

    public UnexpectedParenthesisException(int position) {
        super(String.format(ERROR_UNEXPECTED_PARENTHESIS_FORMAT, position));
    }

}
