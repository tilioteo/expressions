package com.tilioteo.expressions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Stream;

import static com.tilioteo.expressions.Operator.CHARS;
import static com.tilioteo.expressions.Operator.DECLASS;
import static com.tilioteo.expressions.StringConstants.*;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Kamil Morong - Hypothesis
 */
public class ExpressionFactory implements Serializable {

    private static final Set<Character> PROHIBITED = unmodifiableSet(Stream.of(CHR_REV_QUOTE, CHR_AT, CHR_PERCENT,
                    CHR_SQUARE_BRACE_OPEN, CHR_SQUARE_BRACE_CLOSE, CHR_CURLY_BRACE_OPEN, CHR_CURLY_BRACE_CLOSE,
                    CHR_COLON, CHR_SEMI_COLON, CHR_QUOTE, CHR_BCK_SLASH, CHR_QUESTION)
            .collect(toSet())
    );
    private static final Set<Character> SEPARATORS = unmodifiableSet(Stream.of(CHR_TAB, CHR_SPACE)
            .collect(toSet())
    );
    private static final Set<Character> NUMBERS = unmodifiableSet(Stream.of(CHR_0, CHR_1, CHR_2, CHR_3, CHR_4, CHR_5,
                    CHR_6, CHR_7, CHR_8, CHR_9)
            .collect(toSet())
    );
    private static final Logger log = LogManager.getLogger(ExpressionFactory.class);

    public static Expression parseString(final String text) {
        Expression expression = null;
        try {
            expression = buildExpression(text);
        } catch (Throwable e) {
            log.error(e.getMessage());
        }

        return expression;
    }

    private static Expression buildExpression(final String text) throws ExpressionException {
        OperatorNodeGroup operatorNodeGroup = getOperatorNodeGroup(text);
        updateUnaryOperators(text, operatorNodeGroup);
        operatorNodeGroup = updateParenthesisGroups(text, operatorNodeGroup);

        OperatorNode operatorTree = operatorNodeGroup.getOperatorNode();

        return expressionFromNode(text, operatorTree);
    }

    private static OperatorNodeGroup getOperatorNodeGroup(final String text) throws ExpressionException {
        OperatorNodeGroup operatorNodeGroup = new OperatorNodeGroup();

        int operatorIndex = 0;
        boolean stringBegun = false;
        String operatorString = STR_EMPTY;

        // TODO REFACTOR!

        for (int i = 0; i < text.length(); ++i) {
            if (CHR_STRING_SEPARATOR == text.charAt(i)) {
                stringBegun = !stringBegun;
                if (stringBegun) {
                    operatorIndex = 0;
                    operatorString = STR_EMPTY;
                }
                continue;
            }
            if (!stringBegun) {
                if (CHARS.contains(text.charAt(i))) {
                    operatorString += text.charAt(i);

                    if (operatorIndex == 0) {
                        operatorIndex = i;

                        OperatorNode operatorNode = new OperatorNode(i, Operator.get(operatorString));
                        operatorNodeGroup.add(operatorNode);

                    } else if (i == operatorIndex + 1) {
                        Operator operator = Operator.get(operatorString);
                        if (operator != null) {
                            HasOperatorNode obj = operatorNodeGroup.getLast();
                            if (obj instanceof OperatorNode
                                    && ((OperatorNode) obj).getOperator() != operator) {
                                ((OperatorNode) obj).setOperator(operator);
                            }

                            operatorIndex = 0;
                            operatorString = STR_EMPTY;
                        } else {
                            operatorIndex = i;
                            operatorString = STR_EMPTY + text.charAt(i);

                            OperatorNode operatorNode = new OperatorNode(i, Operator.get(operatorString));
                            operatorNodeGroup.add(operatorNode);
                        }
                    }
                } else {
                    if (PROHIBITED.contains(text.charAt(i))) {
                        throw new UnexpectedCharException(i);
                    }

                    if (operatorString.length() > 0 && (operatorIndex >= 0 || i == text.length() - 1)) {
                        Operator operator = Operator.get(operatorString);
                        if (operator != null) {
                            HasOperatorNode obj = operatorNodeGroup.getLast();
                            if (obj instanceof OperatorNode) {
                                ((OperatorNode) obj).setOperator(operator);
                            }

                            operatorIndex = 0;
                            operatorString = STR_EMPTY;
                        } else {
                            operatorIndex = i;
                            operatorString = STR_EMPTY + text.charAt(i);

                            OperatorNode operatorNode = new OperatorNode(i, Operator.get(operatorString));
                            operatorNodeGroup.add(operatorNode);
                        }
                    }
                }
            }
        }
        return operatorNodeGroup;
    }

    private static void updateUnaryOperators(final String text, OperatorNodeGroup operatorNodeGroup) {
        for (HasOperatorNode obj : operatorNodeGroup) {
            if (obj instanceof OperatorNode) {
                OperatorNode operatorNode = (OperatorNode) obj;

                if (operatorNode.getOperator().isUnary()) {
                    boolean foundChar = false;
                    int position = operatorNode.getPosition() - 1;

                    if (position == -1) {
                        operatorNode.setUnary(true);
                    } else {
                        while (!(foundChar || position < 0)) {
                            if (!SEPARATORS.contains(text.charAt(position))) {
                                foundChar = true;
                            }

                            if (position == 0 || (foundChar && CHARS.contains(text.charAt(position)))) {
                                operatorNode.setUnary(true);
                            }

                            --position;
                        }
                    }
                }
            }
        }
    }

    private static OperatorNodeGroup updateParenthesisGroups(final String text, OperatorNodeGroup operatorNodeGroup)
            throws ExpressionException {
        OperatorNodeGroup parents = getGroups(text);

        while (operatorNodeGroup.size() > 0) {
            HasOperatorNode obj = operatorNodeGroup.pop();
            if (obj instanceof OperatorNode) {
                parents.place((OperatorNode) obj);
            }
        }
        return parents;
    }

    private static OperatorNodeGroup getGroups(final String text) throws ExpressionException {
        int level = 0;
        int j;
        ClassNodeGroup classGroup = null;
        int subLevel;
        int methodArgStart = -1;
        int methodArgGroupStart = -1;
        int lastCommaPos = -1;

        OperatorNodeGroup operatorNodeGroup = new OperatorNodeGroup(-1, text.length(), level, null);
        OperatorNodeGroup currentGroup = operatorNodeGroup;

        for (int i = 0; i < text.length(); ++i) {
            if ((i > 0) && (i < text.length() - 2) && (text.startsWith(DECLASS.getString(), i))) {

                if (currentGroup == operatorNodeGroup) {
                    classGroup = new ClassNodeGroup(currentGroup.getBeginPosition(), 0, level, currentGroup);
                    operatorNodeGroup = classGroup;
                } else {
                    j = i;
                    while (j > 0) {
                        char c = text.charAt(--j);
                        if (Parenthesis.OPEN.getChar() == c || SEPARATORS.contains(c) || CHARS.contains(c)) {
                            classGroup = new ClassNodeGroup(j + 1, 0, ++level, currentGroup);
                            currentGroup.add(classGroup);
                            break;
                        }
                    }
                }
                j = i + DECLASS.getString().length() - 1;
                while (j < text.length() - 1) {
                    char c = text.charAt(++j);
                    if (Parenthesis.CLOSE.getChar() == c || SEPARATORS.contains(c) || CHARS.contains(c)) {
                        if (classGroup != null)
                            classGroup.setEndPosition(j + 1);
                        break;
                    } else if (Parenthesis.OPEN.getChar() == c) {
                        // method
                        methodArgStart = j++;
                        methodArgGroupStart = j;
                        subLevel = 1;
                        // ++j;
                        while (subLevel > 0 && j < text.length()) {
                            char c2 = text.charAt(j);
                            if (Parenthesis.OPEN.getChar() == c2)
                                ++subLevel;
                            else if (Parenthesis.CLOSE.getChar() == c2)
                                --subLevel;
                            ++j;
                        }
                        if (classGroup != null)
                            classGroup.setEndPosition(j);
                        break;
                    } else if (j == text.length() - 1) {
                        if (classGroup != null)
                            classGroup.setEndPosition(j + 1);
                        break;
                    }
                }
                if (classGroup != null) {
                    currentGroup = classGroup;
                    classGroup = null;
                }
            }

            if (text.charAt(i) == Parenthesis.OPEN.getChar()) {
                ++level;

                OperatorNodeGroup subGroup = (i == methodArgStart) ? new MethodArgumentGroup(i, 0, level, currentGroup)
                        : new OperatorNodeGroup(i, 0, level, currentGroup);
                currentGroup.add(subGroup);
                currentGroup = subGroup;

            } else if (text.charAt(i) == Parenthesis.CLOSE.getChar()) {
                if (level == 0) {
                    throw new UnexpectedParenthesisException(i);
                }

                if (currentGroup instanceof MethodArgumentGroup && methodArgGroupStart > -1
                        && i > methodArgGroupStart) {
                    String str = text.substring(methodArgGroupStart, i).trim();
                    if (str.length() > 0) {
                        MethodArgument argument = new MethodArgument(str);
                        currentGroup.add(argument);
                    } else {
                        if (lastCommaPos > -1)
                            throw new UnexpectedCharException(lastCommaPos);
                        else
                            throw new UnexpectedCharException(i);
                    }
                }

                if (currentGroup.getLevel() == level && currentGroup.getEndPosition() == 0) {
                    currentGroup.setEndPosition(i);
                }

                currentGroup = currentGroup.getParent();
                --level;
            } else if (text.charAt(i) == CHR_COMMA) {

                if (currentGroup instanceof MethodArgumentGroup && methodArgGroupStart > -1
                        && i > methodArgGroupStart) {
                    String str = text.substring(methodArgGroupStart, i).trim();
                    if (str.length() > 0) {
                        MethodArgument argument = new MethodArgument(str);
                        currentGroup.add(argument);
                        methodArgGroupStart = i + 1;
                        lastCommaPos = i;
                    } else
                        throw new UnexpectedCharException(i);
                } else
                    throw new UnexpectedCharException(i);
            }
        }

        return operatorNodeGroup;
    }

    private static Expression expressionFromNode(final String text, OperatorNode operatorNode)
            throws ExpressionException {
        return expressionFromNode(text, operatorNode, null);
    }

    private static Expression expressionFromNode(final String text, OperatorNode operatorNode,
                                                 Expression parentExpression) throws ExpressionException {
        if (operatorNode.getOperator() == null) {
            if (operatorNode.getLeftNode() == null && operatorNode.getRightNode() == null) {
                if (operatorNode.isUnary())
                    return createUnaryExpression(text, operatorNode, parentExpression);
                else
                    return createBinaryExpression(text, operatorNode, parentExpression);
            } else if (operatorNode.getLeftNode() != null)
                return expressionFromNode(text, operatorNode.getLeftNode(), parentExpression);
            else
                return null;
        } else if (operatorNode.isUnary()) {
            return createUnaryExpression(text, operatorNode, parentExpression);
        } else {
            return createBinaryExpression(text, operatorNode, parentExpression);
        }
    }

    private static void setExpressionLeftSide(BinaryExpression expression, final String text, OperatorNode operatorNode)
            throws ExpressionException {
        if (operatorNode.getLeftNode() != null) {
            expression.setLeftSide(expressionFromNode(text, operatorNode.getLeftNode(), expression));
        } else {
            String str = getLeftSideString(text, operatorNode.getPosition() - 1);

            Primitive primitive = stringToPrimitive(str);
            if (primitive instanceof Variable) {
                Variable variable = (Variable) primitive;
                Variable oldVariable = expression.variables.get(variable.getName());

                if (oldVariable != null) {
                    expression.setLeftSide(oldVariable);
                } else {
                    expression.setLeftSide(primitive);
                }
            } else {
                expression.setLeftSide(primitive);
            }
        }
    }

    private static String getLeftSideString(final String text, final int start) throws ExpressionException {
        return getSideString(text, start, -1);
    }

    private static String getRightSideString(final String text, final int start) throws ExpressionException {
        return getSideString(text, start, 1);
    }

    @SuppressWarnings("incomplete-switch")
    private static String getSideString(final String text, final int start, final int increment)
            throws ExpressionException {
        final StringBuilder result = new StringBuilder();
        MachineState status = MachineState.NONE;
        int position = start;
        char c;

        while (!status.equals(MachineState.END)) {
            c = text.charAt(position);

            switch (status) {
                case NONE:
                    if (CHR_STRING_SEPARATOR == c) {
                        status = MachineState.STRING;
                        result.append(c);
                    } else if (!SEPARATORS.contains(c)) {
                        status = MachineState.WORD;
                        position -= increment;
                    }
                    break;
                case WORD:
                    if (SEPARATORS.contains(c) || CHARS.contains(c)
                            || (increment > 0 && (c == Parenthesis.CLOSE.getChar() || c == Parenthesis.OPEN.getChar()))
                            || (increment < 0 && c == Parenthesis.OPEN.getChar())) {
                        status = MachineState.AFTER;
                        position -= increment;
                    } else {
                        if (increment > 0) {
                            result.append(c);
                        } else {
                            result.insert(0, c);
                        }
                    }
                    break;

                case AFTER:
                    if (SEPARATORS.contains(c) || CHARS.contains(c)
                            || (increment > 0 && (c == Parenthesis.CLOSE.getChar() || c == Parenthesis.OPEN.getChar()))
                            || (increment < 0 && c == Parenthesis.OPEN.getChar())) {
                        status = MachineState.END;
                    } else if (!(SEPARATORS.contains(c)
                            || (increment > 0 && (c == Parenthesis.CLOSE.getChar() || c == Parenthesis.OPEN.getChar()))
                            || (increment < 0 && c == Parenthesis.OPEN.getChar()))) {
                        throw new UnexpectedCharException(position);
                    }
                    break;

                case STRING:
                    if (CHR_STRING_SEPARATOR == c) {
                        status = MachineState.NONE;
                    }
                    if (increment > 0) {
                        result.append(c);
                    } else {
                        result.insert(0, c);
                    }
                    break;
            }

            position += increment;

            if ((increment > 0 && position >= text.length()) || (increment < 0 && position < 0)) {
                status = MachineState.END;
            }
        }
        return result.toString();
    }

    private static Primitive stringToPrimitive(String string) {
        if (isValue(string))
            return new Constant(string);
        else if (isIdentifier(string))
            return new Variable(string, Object.class);

        return null;
    }

    private static boolean isValue(String string) {
        return (isNotBlank(string) && (NUMBERS.contains(string.charAt(0))
                || (string.startsWith(STR_DOUBLE_QUOTE)
                && string.endsWith(STR_DOUBLE_QUOTE))
                || (string.equalsIgnoreCase(Boolean.TRUE.toString())
                || string.equalsIgnoreCase(Boolean.FALSE.toString()))));
    }

    private static boolean isIdentifier(String string) {
        return (isNotBlank(string));
    }

    private static void setExpressionRightSide(UnaryExpression expression, final String text, OperatorNode operatorNode)
            throws ExpressionException {
        if (operatorNode.getRightNode() != null) {
            expression.setRightSide(expressionFromNode(text, operatorNode.getRightNode(), expression));
        } else {
            String str = getRightSideString(text, operatorNode.getPosition()
                    + (operatorNode.getOperator() != null ? operatorNode.getOperator().getString().length() : 0));

            if (operatorNode.getOperator() == null)
                expression.operator = Operator.PLUS;

            if (DECLASS.equals(operatorNode.getOperator())) {
                if (operatorNode.getGroup() instanceof ClassNodeGroup) {
                    ClassNodeGroup group = (ClassNodeGroup) operatorNode.getGroup();
                    MethodArgumentGroup argumentGroup = group.getArgumentGroup();
                    if (argumentGroup != null) { // method call
                        Primitive[] arguments = argumentGroup.getArgumentPrimitives();
                        for (Primitive argument : arguments) {
                            if (argument instanceof Expression) {
                                Expression argumentExpression = (Expression) argument;
                                argumentExpression.mergeVariables(expression.variables);
                            }
                        }

                        Method method = new Method(str, arguments);
                        expression.setRightSide(method);
                    } else { // atttribute value
                        Attribute attribute = new Attribute(str, Object.class);
                        expression.setRightSide(attribute);
                    }
                }
            } else {
                Primitive primitive = stringToPrimitive(str);
                if (primitive instanceof Variable) {
                    Variable variable = (Variable) primitive;
                    Variable oldVariable = expression.variables.get(variable.getName());

                    if (oldVariable != null) {
                        expression.setRightSide(oldVariable);
                    } else {
                        expression.setRightSide(primitive);
                    }
                } else {
                    expression.setRightSide(primitive);
                }
            }
        }
    }

    private static Expression createUnaryExpression(final String text, OperatorNode operatorNode,
                                                    Expression parentExpression) throws ExpressionException {
        UnaryExpression expression = new UnaryExpression(parentExpression);
        expression.operator = operatorNode.getOperator();

        setExpressionRightSide(expression, text, operatorNode);

        return expression;
    }

    private static Expression createBinaryExpression(final String text, OperatorNode operatorNode,
                                                     Expression parentExpression) throws ExpressionException {
        BinaryExpression expression = new BinaryExpression(parentExpression);
        expression.operator = operatorNode.getOperator();

        setExpressionLeftSide(expression, text, operatorNode);
        setExpressionRightSide(expression, text, operatorNode);

        return expression;
    }

    private enum Parenthesis {
        OPEN(CHR_BRACE_OPEN), CLOSE(CHR_BRACE_CLOSE);

        private final char c;

        Parenthesis(char c) {
            this.c = c;
        }

        public char getChar() {
            return c;
        }

    }

    private enum MachineState {
        NONE, STRING, WORD, AFTER, END
    }

}
