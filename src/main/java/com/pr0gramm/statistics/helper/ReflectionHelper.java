package com.pr0gramm.statistics.helper;

import java.lang.reflect.Field;

/**
 * Created by koray on 30/01/2017.
 */
public class ReflectionHelper {

    public static <T> Object getObject(String name, Object topLevelObj, Class<T> type) throws IllegalAccessException, NoSuchFieldException {
        if (name.contains(".")) {
            String[] splittedFields = name.split("\\.");
            Object o = topLevelObj;
            Field f = null;
            for (String s : splittedFields) {
                if (f == null) {
                    f = type.getDeclaredField(s);
                    f.setAccessible(true);
                    o = f.get(o);
                } else {
                    f = f.getType().getDeclaredField(s);
                    f.setAccessible(true);
                    o = f.get(o);
                }
            }
            return o;
        } else {
            Field f = type.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(topLevelObj);
        }
    }
}
