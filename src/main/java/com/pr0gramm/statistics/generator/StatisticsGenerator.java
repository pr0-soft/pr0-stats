package com.pr0gramm.statistics.generator;

import com.pr0gramm.statistics.predicate.GeneralPredicate;

import java.util.ArrayList;

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
