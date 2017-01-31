package com.pr0gramm.statistics.generator;

import com.pr0gramm.statistics.helper.DecimalHelper;
import com.pr0gramm.statistics.helper.ReflectionHelper;
import com.pr0gramm.statistics.predicate.GeneralPredicate;
import com.pr0gramm.statistics.toolbox.SortOption;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by koray on 30/01/2017.
 */
public class StatisticsGenerator<T> {

    private ArrayList<T> list;
    private ArrayList<? extends GeneralPredicate<T>> predicates;

    private ArrayList<T> statisticsList = new ArrayList<T>();

    public StatisticsGenerator(ArrayList<T> list, ArrayList<? extends GeneralPredicate<T>> predicates) {
        this.list = list;
        this.predicates = predicates;
    }

    public void generate() {
        statisticsList.clear();
        for (T el : list) {
            if (satisfiesPredicates(el)) {
                statisticsList.add(el);
            }
        }
    }

    public ArrayList<T> sort(final SortOption sortOption) {
        Collections.sort(statisticsList, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                }

                Object o1Value;
                try {
                    o1Value = ReflectionHelper.getObject(sortOption.getField(), o1, o1.getClass());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("The field '" + sortOption.getField()
                        + "' doesn't exist you silly idiot! Terminating the program just for you now.");
                }

                Object o2Value;
                try {
                    o2Value = ReflectionHelper.getObject(sortOption.getField(), o2, o2.getClass());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("The field '" + sortOption.getField()
                        + "' doesn't exist you silly idiot! Terminating the program just for you now.");
                }

                if (DecimalHelper.isDecimal(o1Value.toString()) && DecimalHelper.isDecimal(o2Value.toString())) {
                    BigDecimal o1Decimal = new BigDecimal(o1Value.toString());
                    BigDecimal o2Decimal = new BigDecimal(o2Value.toString());

                    return o1Decimal.compareTo(o2Decimal);
                } else {
                    return o1Value.toString().compareTo(o2Value.toString());
                }
            }
        });
        return statisticsList;
    }

    private boolean satisfiesPredicates(T el) {
        for (GeneralPredicate<T> predicate : predicates) {
            if (!predicate.satisfiesPredicate(el)) {
                return false;
            }
        }

        return true;
    }

    public ArrayList<T> getStatisticsList() {
        return statisticsList;
    }
}
