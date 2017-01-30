package com.pr0gramm.statistics.predicate;

import com.pr0gramm.statistics.helper.DecimalHelper;
import com.pr0gramm.statistics.helper.ReflectionHelper;
import com.pr0gramm.statistics.toolbox.OperatorType;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * A predicate which combines two verbs with an operator.
 * <p>
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

        if (operatorType == null) {
            throw new UnsupportedOperationException("The operator " + verbs[1]
                + " is not supported. Please use one of the following: ==, !=, >, <, >=, <=");
        }

        getField(fieldName);

        predicateParsed = true;
    }

    @Override
    public boolean satisfiesPredicate(T obj) throws UnsupportedOperationException {
        if (!predicateParsed) {
            throw new UnsupportedOperationException("parsePredicate must be called before calling satisfiesPredicate!");
        }

        Object actualValue;
        try {
            actualValue = ReflectionHelper.getObject(fieldName, obj, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
            if (DecimalHelper.isDecimal(actualValue.toString()) && DecimalHelper.isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) > 0;
            }
            return actualValue.toString().compareTo(value) > 0;
        case LESS:
            if (DecimalHelper.isDecimal(actualValue.toString()) && DecimalHelper.isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) < 0;
            }
            return actualValue.toString().compareTo(value) < 0;
        case GREATER_OR_EQUAL:
            if (DecimalHelper.isDecimal(actualValue.toString()) && DecimalHelper.isDecimal(value)) {
                return (new BigDecimal(actualValue.toString())).compareTo(new BigDecimal(value)) >= 0;
            }
            return actualValue.toString().compareTo(value) >= 0;
        case LESS_OR_EQUAL:
            if (DecimalHelper.isDecimal(actualValue.toString()) && DecimalHelper.isDecimal(value)) {
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

    private Field getField(String name) throws NoSuchFieldException {
        if (name.contains(".")) {
            String[] splittedFields = name.split("\\.");
            Field f = null;
            for (String s : splittedFields) {
                if (f == null) {
                    f = type.getDeclaredField(s);
                } else {
                    f = f.getType().getDeclaredField(s);
                }
            }
            return f;
        } else {
            return type.getDeclaredField(name);
        }
    }
}
