package com.pr0gramm.statistics.helper;

import com.pr0gramm.statistics.Main;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by koray on 30/01/2017.
 */
public class AbsoluteStatisticsHelper {

    public static <T> void parseAbsoluteStatistics(ArrayList<T> list, String fieldName, Class<T> tClass,
        boolean average) {
        BigDecimal decimal = new BigDecimal(0);

        int count = 0;
        for (T item : list) {
            try {
                Object o = ReflectionHelper.getObject(fieldName, item, tClass);
                if (DecimalHelper.isDecimal(o.toString())) {
                    decimal = decimal.add(new BigDecimal(o.toString()));
                    count++;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("This should never happen. If it happened you are a Loser...");
            } catch (NoSuchFieldException e) {
                Main.getLogger().log(Level.WARNING, "The field with the name '" + fieldName + "' could not be found!");
                return;
            }
        }

        if (!average) {
            Main.getLogger()
                .log(Level.INFO, "The total combined value of field '" + fieldName + "' is " + decimal.toString());
        } else {
            decimal = decimal.divide(new BigDecimal(count == 0 ? 1 : count), RoundingMode.CEILING);
            Main.getLogger()
                .log(Level.INFO, "The average value of field '" + fieldName + "' is " + decimal.toString());
        }
    }
}
