package com.pr0gramm.statistics.helper;

import java.math.BigDecimal;

/**
 * Created by koray on 30/01/2017.
 */
public class DecimalHelper {

    public static boolean isDecimal(String s) {
        try {
            new BigDecimal(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static boolean isInt(String s) {
        try {
            int i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
