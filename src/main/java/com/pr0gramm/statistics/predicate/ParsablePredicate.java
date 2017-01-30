package com.pr0gramm.statistics.predicate;

/**
 * A predicate which must be parsed with a specific format first.
 *
 * Created by koray on 30/01/2017.
 */
public interface ParsablePredicate<T> extends GeneralPredicate<T> {

    /**
     * Parses the given array string of verbs into a predicate and saves it.
     * <p>
     * Can be called just once.
     * <p>
     * {@code verbs} must be formatted like that:
     * <p>
     * { "fieldName", "(== | != | > | < | >= | <=)", "value" }
     * <p>
     * where the first argument ({@code fieldName}) represents a field name which can be found in {@code Type T}, the
     * second argument represents an operator and the third argument represents the value the field should be (or not
     * be,...).
     *
     * @param verbs The verbs which should be parsed to wire up this predicate.
     * @throws UnsupportedOperationException If parsePredicate was called already or the {@code verbs} have an invalid
     *                                       format.
     * @throws NoSuchFieldException          If the given Type T does not have a field named like in the given verbs.
     */
    void parsePredicate(String[] verbs) throws UnsupportedOperationException, NoSuchFieldException;
}
