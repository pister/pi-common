package com.github.pister.common.ddl.runtime;

import wint.lang.magic.MagicObject;

import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:32
 */
public class SqlInputProperties implements InputProperties {

    public static final String TABLE_INDEX_NAME = "tableIndex";

    private String tableIndexFormat;

    private Object propertyValues;

    public SqlInputProperties(Object propertyValues, String tableIndexFormat) {
        this.propertyValues = propertyValues;
        this.tableIndexFormat = tableIndexFormat;
    }

    public Object getPropertyValues() {
        return propertyValues;
    }

    public Object getProperty(String name) {
        if (propertyValues instanceof Map) {
            return ((Map<String, Object>)propertyValues).get(name);
        }
        MagicObject magicObject = MagicObject.wrap(propertyValues);
        return magicObject.getPropertyValue(name);
    }

    public void setProperty(String name, Object value) {
        if (propertyValues instanceof Map) {
            ((Map<String, Object>)propertyValues).put(name, value);
            return;
        }
        MagicObject magicObject = MagicObject.wrap(propertyValues);
        magicObject.setPropertyValue(name, value);
    }


    public boolean checkIdExist(String idName) {
        Object value = getProperty(idName);
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            if (((String) value).length() == 0) {
                return false;
            }
        }
        if (value instanceof Number) {
            if (((Number) value).longValue() == 0) {
                return false;
            }
        }
        return true;
    }


    protected String formatTableIndex(int tableIndex) {
       // return String.format("%04d", tableIndex);
        return String.format(tableIndexFormat, tableIndex);
    }

    public void setTableIndex(int tableIndex) {
        String tableIndexString = formatTableIndex(tableIndex);
        if (propertyValues instanceof Map) {
            ((Map<String, Object>)propertyValues).put(TABLE_INDEX_NAME, tableIndexString);
        } else {
            ((TableIndexSetter)propertyValues).setTableIndex(tableIndexString);
        }
    }

    @Override
    public String toString() {
        return "SqlInputProperties{" +
                "propertyValues=" + propertyValues +
                '}';
    }
}
