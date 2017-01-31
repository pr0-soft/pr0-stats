package com.pr0gramm.statistics.toolbox;

/**
 * Created by koray on 31/01/2017.
 */
public enum SortType {
    ASCENDING, DESCENDING;

    public static SortType fromString(String s) {
        if (s == null) {
            return null;
        }
        switch (s) {
        case "asc":
            return SortType.ASCENDING;
        case "desc":
            return SortType.DESCENDING;
        }

        return null;
    }
}
