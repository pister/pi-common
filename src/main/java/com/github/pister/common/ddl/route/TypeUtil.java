package com.github.pister.common.ddl.route;

import wint.lang.utils.CollectionUtil;

import java.sql.Timestamp;
import java.util.Set;

/**
 * Created by songlihuang on 2017/2/16.
 */
public class TypeUtil {

    private static Set<Class<?>> innerObjects = CollectionUtil.newHashSet();

    static {
        innerObjects.add(Void.TYPE);
        innerObjects.add(Boolean.TYPE);
        innerObjects.add(Byte.TYPE);
        innerObjects.add(Short.TYPE);
        innerObjects.add(Character.TYPE);
        innerObjects.add(Integer.TYPE);
        innerObjects.add(Long.TYPE);
        innerObjects.add(Float.TYPE);
        innerObjects.add(Double.TYPE);
        innerObjects.add(Void.class);
        innerObjects.add(Boolean.class);
        innerObjects.add(Byte.class);
        innerObjects.add(Short.class);
        innerObjects.add(Character.class);
        innerObjects.add(Integer.class);
        innerObjects.add(Long.class);
        innerObjects.add(Float.class);
        innerObjects.add(Double.class);

        innerObjects.add(String.class);
        innerObjects.add(java.util.Date.class);
        innerObjects.add(java.sql.Date.class);
        innerObjects.add(Timestamp.class);
    }

    public static boolean isInnerType(Class<?> c) {
        return innerObjects.contains(c);
    }
}
