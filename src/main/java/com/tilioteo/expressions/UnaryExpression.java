package com.tilioteo.expressions;

/**
 * @author Kamil Morong - Hypothesis
 */
public class UnaryExpression extends Expression {

    protected Primitive rightSide;

    public UnaryExpression(Expression parent) {
        super(parent);
    }

    private void clearRightSide() {
        if (rightSide != null) {
            if (rightSide instanceof Variable) {
                Variable variable = (Variable) rightSide;
                if (variable.decRefCount() == 0) {
                    variables.remove(variable);
                }
            }

            rightSide = null;
        }
    }

    public Primitive getRightSide() {
        return rightSide;
    }

    public void setRightSide(Primitive rightSide) {
        if (this.rightSide != rightSide) {
            clearRightSide();

            this.rightSide = rightSide;

            if (this.rightSide != null) {
                if (this.rightSide instanceof Variable) {
                    variables.add((Variable) this.rightSide);
                } else if (this.rightSide instanceof Expression) {
                    Expression expression = (Expression) this.rightSide;
                }
            }
        }
    }

    @Override
    public void clear() {
        clearRightSide();
        super.clear();
    }

    @Override
    public Object getValue() {
        if (rightSide != null) {
            Object rightValue = rightSide.getValue();
            if (rightValue != null && operator.isUnary()) {
                // apply unary operators
                switch (operator) {
                    case NOT:
                        if (rightValue instanceof Boolean) {
                            return !(Boolean) rightValue;
                        }
                        break;
                    case MINUS:
                        if (rightValue instanceof Boolean)
                            return !(Boolean) rightValue;
                        else if (rightValue instanceof Byte) {
                            return -(Byte) rightValue;
                        } else if (rightValue instanceof Short) {
                            return -(Short) rightValue;
                        } else if (rightValue instanceof Integer) {
                            return -(Integer) rightValue;
                        } else if (rightValue instanceof Long) {
                            return -(Long) rightValue;
                        } else if (rightValue instanceof Float) {
                            return -(Float) rightValue;
                        } else if (rightValue instanceof Double) {
                            return -(Double) rightValue;
                        }
                        break;

                    default:
                        return rightValue;
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (Operator.NOT.equals(operator) || Operator.MINUS.equals(operator)) {
            builder.append("(").append(operator).append(rightSide.toString()).append(")");
        } else {
            builder.append(rightSide.toString());
        }
        return builder.toString();
    }
}
