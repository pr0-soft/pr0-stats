package com.pr0gramm.statistics.predicate;

import com.pr0gramm.statistics.toolbox.ConditionalOperatorType;

/**
 * A predicate which combines two predicates and tests them against a given operator.
 * <p>
 * This supports recursive predicates.
 * <p>
 * Created by koray on 30/01/2017.
 */
public class ConditionalPredicateCombination<S, T extends GeneralPredicate<S>> implements GeneralPredicate<S> {

    private T predicate1;
    private T predicate2;

    private ConditionalOperatorType operator;

    public ConditionalPredicateCombination(T predicate1, T predicate2, ConditionalOperatorType operator) {
        this.predicate1 = predicate1;
        this.predicate2 = predicate2;
        this.operator = operator;
    }

    @Override
    public boolean satisfiesPredicate(S obj) throws UnsupportedOperationException {
        switch (operator) {
        case AND:
            return predicate1.satisfiesPredicate(obj) && predicate2.satisfiesPredicate(obj);
        case OR:
            return predicate1.satisfiesPredicate(obj) || predicate2.satisfiesPredicate(obj);
        }

        throw new UnsupportedOperationException(
            "Currently ConditionalPredicateCombination supports just AND and OR operators! Your tried the operator "
                + operator.toString());
    }
}
