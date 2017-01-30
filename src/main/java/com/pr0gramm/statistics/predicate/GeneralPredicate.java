package com.pr0gramm.statistics.predicate;

/**
 * A general predicate
 * <p>
 * Created by koray on 29/01/2017.
 */
public interface GeneralPredicate<T> {

    /**
     * Returns true iff the given object of type T satisfies the predicate.
     *
     * @param obj The object which should be tested against this predicate
     * @return True iff the object satisfies the predicate.
     * @throws UnsupportedOperationException If the predicate is not built correctly in any way or the predicate
     *                                       contains something which can't be executed for the given object.
     */
    boolean satisfiesPredicate(T obj) throws UnsupportedOperationException;
}
