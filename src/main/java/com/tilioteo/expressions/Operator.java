package com.tilioteo.expressions;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.tilioteo.expressions.StringConstants.*;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * @author Kamil Morong - Hypothesis
 */
enum Operator implements Serializable {

    DECLASS(STR_OP_DECLASS, 1),
    NOT(STR_OP_NOT, 2),
    MINUS(STR_OP_MINUS, 4), // NOTE if unary then ignore this priority
    PLUS(STR_OP_PLUS, 5), // NOTE if unary then ignore this priority
    MULTIPLY(STR_OP_MULTIPLY, 3),
    DIVIDE(STR_OP_DIVIDE, 3),
    EQUALS(STR_OP_EQUALS, 6),
    NOT_EQUALS(STR_OP_NOT_EQUAL, 6),
    GREATER(STR_OP_GREATER, 5),
    LESS(STR_OP_LESS, 5),
    GREATER_OR_EQUAL(STR_OP_GREATER_OR_EQUAL, 5),
    LESS_OR_EQUAL(STR_OP_LESS_OR_EQUAL, 5),
    AND(STR_OP_AND, 7),
    OR(STR_OP_OR, 8),
    XOR(STR_OP_XOR, 8),
    ASSIGN(STR_OP_ASSIGN, 9);

    public static final Set<Operator> UNARY = unmodifiableSet(Stream.of(NOT, MINUS, PLUS)
            .collect(toSet())
    );
    public static final Set<Operator> BINARY = unmodifiableSet(Stream.of(
                    ASSIGN, MINUS, PLUS, MULTIPLY, DIVIDE, EQUALS, NOT_EQUALS, GREATER, LESS, GREATER_OR_EQUAL,
                    LESS_OR_EQUAL, AND, OR, XOR, DECLASS)
            .collect(toSet())
    );
    public static final Set<Character> CHARS = unmodifiableSet(Stream.of(CHR_NOT, CHR_MINUS, CHR_PLUS, CHR_MULTIPLY,
                    CHR_DIVIDE, CHR_EQUALS, CHR_GREATER, CHR_LESS, CHR_AND, CHR_OR, CHR_TILDA)
            .collect(toSet())
    );
    private static final Map<String, Operator> lookup = new HashMap<>();

    static {
        for (Operator op : EnumSet.allOf(Operator.class)) {
            lookup.put(op.getString(), op);
        }
    }

    private final String string;
    private final int priority;

    Operator(String string, int priority) {
        this.string = string;
        this.priority = priority;
    }

    public static Operator get(String string) {
        return lookup.get(string);
    }

    public String getString() {
        return string;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isUnary() {
        return UNARY.contains(this);
    }

    public boolean isBinary() {
        return BINARY.contains(this);
    }

    @Override
    public String toString() {
        return string;
    }
}
