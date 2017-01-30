package com.pr0gramm.statistics.toolbox;

/**
 * Created by koray on 29/01/2017.
 */
public enum ConditionalOperatorType {
    AND, OR;

    public static ConditionalOperatorType fromString(String verb) {
        switch (verb.trim()) {
        case "&&":
            return ConditionalOperatorType.AND;
        case "||":
            return ConditionalOperatorType.OR;
        default:
            return null;
        }
    }
}
