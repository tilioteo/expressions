package com.tilioteo.expressions;

import org.apache.commons.lang3.StringUtils;

import static com.tilioteo.expressions.StringConstants.*;

/**
 * @author Kamil Morong - Hypothesis
 */
public class Constant extends Primitive {

    private Class<?> type;

    public Constant(String value) {
        assert (StringUtils.isNotBlank(value));

        boolean parsed = false;

        if (value.startsWith(STR_DOUBLE_QUOTE) && value.endsWith(STR_DOUBLE_QUOTE)) {
            setValue(value.substring(1, value.length() - 1));
            type = String.class;
            parsed = true;

        } else if (value.contains(STR_DOT)) {
            try {
                Double val = Double.parseDouble(value);
                setValue(val);
                type = Double.class;
                parsed = true;
            } catch (NumberFormatException e) {
            }

        }
        if (!parsed) {
            try {
                Integer val = Integer.parseInt(value);
                setValue(val);
                type = Integer.class;
                parsed = true;
            } catch (NumberFormatException e) {
            }
        }
        if (!parsed) {
            if (value.equalsIgnoreCase(STR_BOOL_TRUE)) {
                setValue(Boolean.TRUE);
            } else if (value.equalsIgnoreCase(STR_BOOL_FALSE)) {
                setValue(Boolean.FALSE);
            }
            type = Boolean.class;
        }
    }

    @Override
    public String toString() {
        Object value = getValue();
        if (type == String.class) {
            return value != null ? STR_DOUBLE_QUOTE + value + STR_DOUBLE_QUOTE
                    : "<null>";
        } else if (Number.class.isAssignableFrom(type)) {
            return value != null ? value.toString() : "<NaN>";
        } else {
            return getValue() != null ? value.toString() : "<null>";
        }
    }
}
