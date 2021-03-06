package com.pr0gramm.statistics.toolbox;

/**
 * Created by koray on 31/01/2017.
 */
public class SortOption {

    private SortType sortType;
    private String field;
    private int top;
    private String[] predicate;

    public SortOption(SortType sortType, String field, int top, String[] predicate) {
        this.sortType = sortType;
        this.field = field;
        this.top = top;
        this.predicate = predicate;
    }

    public SortType getSortType() {
        return sortType;
    }

    public String getField() {
        return field;
    }

    public int getTop() {
        return top;
    }

    public String[] getPredicate() {
        return predicate;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setPredicate(String[] predicate) {
        this.predicate = predicate;
    }
}
