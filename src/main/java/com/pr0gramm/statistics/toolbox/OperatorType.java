package com.pr0gramm.statistics.toolbox;

/**
 * Created by koray on 29/01/2017.
 */
public enum OperatorType {
    EQUAL, NOT_EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL;

    public static OperatorType fromString(String verb) {
        switch (verb.trim()) {
        case "==":
            return OperatorType.EQUAL;
        case "!=":
            return OperatorType.NOT_EQUAL;
        case ">":
            return OperatorType.GREATER;
        case "<":
            return OperatorType.LESS;
        case ">=":
            return OperatorType.GREATER_OR_EQUAL;
        case "<=":
            return OperatorType.LESS_OR_EQUAL;
        default:
            return null;
        }
    }
}
