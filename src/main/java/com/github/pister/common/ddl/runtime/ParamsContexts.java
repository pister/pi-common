package com.github.pister.common.ddl.runtime;

import com.github.pister.common.ddl.config.TableNameConfig;
import com.github.pister.common.ddl.route.TypeUtil;
import wint.lang.magic.MagicClass;
import wint.lang.magic.MagicObject;
import wint.lang.magic.Property;
import wint.lang.utils.MapUtil;

import java.util.Collection;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:53
 */
public final class ParamsContexts {

    private ParamsContexts() {
    }

    public static SqlInputProperties create(Object object) {
        SqlInputProperties sqlInputProperties = createImpl(object, TableNameConfig.tableIndexFormat);
        return sqlInputProperties;
    }

    public static class ObjectRoute {
        private Object routeValue;
        private Object object;

        public ObjectRoute(Object routeValue, Object object) {
            this.routeValue = routeValue;
            this.object = object;
        }

        public Object getRouteValue() {
            return routeValue;
        }

        public Object getObject() {
            return object;
        }
    }

    private static SqlInputProperties createImpl(Object object, String tableIndexFormat) {
        if (object == null) {
            Map<String, Object> newMap = MapUtil.newHashMap();
            newMap.put("value", null);
            return new SqlInputProperties(newMap, tableIndexFormat);
        }
        if (object instanceof Map) {
            Map<String, Object> newMap = MapUtil.newHashMap((Map<String, Object>) object);
            return new SqlInputProperties(newMap, tableIndexFormat);
        }
        if (object instanceof Collection) {
            Map<String, Object> newMap = MapUtil.newHashMap();
            newMap.put("values", object);
            return new SqlInputProperties(newMap, tableIndexFormat);
        }
        if (object.getClass().isArray()) {
            Map<String, Object> newMap = MapUtil.newHashMap();
            newMap.put("values", object);
            return new SqlInputProperties(newMap, tableIndexFormat);
        }
        if (TypeUtil.isInnerType(object.getClass())) {
            Map<String, Object> newMap = MapUtil.newHashMap();
            newMap.put("value", object);
            return new SqlInputProperties(newMap, tableIndexFormat);
        }
        return new SqlInputProperties(objectToMap(object), tableIndexFormat);
    }

    private static Map<String, Object> objectToMap(Object object) {
        MagicObject magicObject = MagicObject.wrap(object);
        MagicClass magicClass = magicObject.getMagicClass();
        Map<String, Property> propertyMap = magicClass.getReadableProperties();
        Map<String, Object> ret = MapUtil.newHashMap();
        for (Map.Entry<String, Property> entry : propertyMap.entrySet()) {
            Property property = entry.getValue();
            Object value = property.getValue(object);
            ret.put(entry.getKey(), value);
        }
        return ret;
    }


}
