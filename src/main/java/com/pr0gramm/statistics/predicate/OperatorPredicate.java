package com.pr0gramm.statistics.predicate;

import com.pr0gramm.statistics.toolbox.OperatorType;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * Created by koray on 29/01/2017.
 */
public class OperatorPredicate<T> implements ParsablePredicate<T> {

    private Class<T> type;

    private boolean predicateParsed = false;

    private String fieldName;
    private OperatorType operatorType;
    private String value;

    public OperatorPredicate(Class<T> type) {
        this.type = type;
    }

    @Override
    public void parsePredicate(String[] verbs) throws UnsupportedOperationException, NoSuchFieldException {
        if (predicateParsed) {
            throw new UnsupportedOperationException("This predicate was already parsed!");
        }

        if (verbs.length != 3) {
            throw new UnsupportedOperationException("The verbs have an invalid format!");
        }

        fieldName = verbs[0];
        operatorType = OperatorType.fromString(verbs[1]);
        value = verbs[2];

        type.getDeclaredField(fieldName);

        predicateParsed = true;
    }

    @Override
    public boolean satisfiesPredicate(T obj) throws UnsupportedOperationException {
        if (!predicateParsed) {
            throw new UnsupportedOperationException("parsePredicate must be called before calling satisfiesPredicate!");
        }

        Field field;
        try {
            field = type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // This will *never* happen. We are testing this already in parsePredicate...
            throw new RuntimeException("This should not happen. Something went totally wrong.");
        }

        field.setAccessible(true);

        Object actualValue;
        try {
            actualValue = field.get(obj);
        } catch (IllegalAccessException e) {
            // This will never happen because we are setting the field to accessible above...
            throw new RuntimeException("This should not happen. Something went totally wrong.");
        }

        if (actualValue == null) {
            // If actualValue is null we can't compare it so it doesn't satisfy the predicate.
            return false;
        }

        switch (operatorType) {
        case EQUAL:
            return actualValue.toString().equals(value);
        case NOT_EQUAL:
            return !actualValue.toString().equals(value);
        case GREATER:
            if (isDecimal(actualValue.toString()) && isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) > 0;
            }
            return actualValue.toString().compareTo(value) > 0;
        case LESS:
            if (isDecimal(actualValue.toString()) && isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) < 0;
            }
            return actualValue.toString().compareTo(value) < 0;
        case GREATER_OR_EQUAL:
            if (isDecimal(actualValue.toString()) && isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) >= 0;
            }
            return actualValue.toString().compareTo(value) >= 0;
        case LESS_OR_EQUAL:
            if (isDecimal(actualValue.toString()) && isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) <= 0;
            }
            return actualValue.toString().compareTo(value) <= 0;
        }

        return false;
    }

    @Override
    public String toString() {
        return fieldName + " " + operatorType.toString() + " " + value;
    }

    private boolean isDecimal(String s) {
        try {
            new BigDecimal(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
