package com.pr0gramm.statistics.api;

/**
 * Created by koray on 27/01/2017.
 */
public class Result {

    private boolean atEnd;
    private boolean atStart;
    private String error;

    private Item[] items;

    private long ts;
    private String cache;
    private int rt;
    private int qc;

    public Result(boolean atEnd, boolean atStart, String error, Item[] items, long ts, String cache, int rt, int qc) {
        this.atEnd = atEnd;
        this.atStart = atStart;
        this.error = error;
        this.items = items;
        this.ts = ts;
        this.cache = cache;
        this.rt = rt;
        this.qc = qc;
    }

    public boolean isAtEnd() {
        return atEnd;
    }

    public boolean isAtStart() {
        return atStart;
    }

    public String getError() {
        return error;
    }

    public Item[] getItems() {
        return items;
    }

    public long getTs() {
        return ts;
    }

    public String getCache() {
        return cache;
    }

    public int getRt() {
        return rt;
    }

    public int getQc() {
        return qc;
    }
}
