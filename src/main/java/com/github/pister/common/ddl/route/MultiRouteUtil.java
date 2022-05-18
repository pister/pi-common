package com.github.pister.common.ddl.route;

import com.github.pister.common.ddl.runtime.ParamsContexts;
import com.github.pister.common.ddl.runtime.SqlInputProperties;
import com.github.pister.common.ddl.shard.ShardInfo;
import wint.lang.magic.MagicList;
import wint.lang.magic.MagicObject;
import wint.lang.magic.Transformer;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;

import java.util.*;

/**
 * Created by songlihuang on 2017/2/16.
 */
public class MultiRouteUtil {

    public static Map<ShardInfo, Object> groupByShard(Router router, String propertyName, Object parameterObject) {
        RouteObjectOperator routeObjectOperator = createOperator(propertyName, parameterObject);
        if (routeObjectOperator == null) {
            return null;
        }
        Iterator<RouteObject> iterator = routeObjectOperator.iterator();
        while (iterator.hasNext()) {
            RouteObject routeObject = iterator.next();
            Map<String, Object> params = MapUtil.newHashMap();
            params.put(propertyName, routeObject.getRouteValue(propertyName));
            SqlInputProperties sqlInputProperties = ParamsContexts.create(params);
            ShardInfo shardInfo = router.route(sqlInputProperties);
            sqlInputProperties.setTableIndex(shardInfo.getTableIndex());
            routeObjectOperator.merge(shardInfo, routeObject);
        }
        return routeObjectOperator.getMergedResult();
    }

    private static RouteObjectOperator createOperator(String propertyName, Object parameterObject) {
        if (parameterObject == null) {
            return null;
        }
        if (parameterObject instanceof Collection) {
            return new CollectionRouteObjectOperator((Collection) parameterObject);
        }
        if (parameterObject.getClass().isArray()) {
            return new CollectionRouteObjectOperator(MagicList.wrapArray(parameterObject));
        }
        if (parameterObject instanceof Map) {
            Map m = (Map) parameterObject;
            Object value = m.get(propertyName);
            Collection values;
            if (value instanceof Collection) {
                values = (Collection) value;
            } else {
                values = Arrays.asList(value);
            }
            Map<String, Object> remainParams = getRemainParams(m, propertyName);
            return new ParameterNameMapRouteObjectOperator(propertyName, values, remainParams);
        }
        throw new RuntimeException("not support value type:" + propertyName);
    }

    private static Map<String, Object> getRemainParams(Map params, String propertyName) {
        Map<String, Object> ret = new HashMap<String, Object>(params);
        ret.remove(propertyName);
        return ret;
    }

    private interface RouteObjectOperator {
        Iterator<RouteObject> iterator();

        void merge(ShardInfo shardInfo, RouteObject routeObject);

        Map<ShardInfo, Object> getMergedResult();
    }

    interface RouteObject {
        Object getRouteValue(String propertyName);

        Object getObject();
    }

    private static class ParameterNameMapRouteObjectOperator implements RouteObjectOperator {

        private String parameterName;

        private Collection values;

        private Map<ShardInfo, List<Object>> shardValues = MapUtil.newHashMap();

        private Map<String, Object> remainParams;

        public ParameterNameMapRouteObjectOperator(String parameterName, Collection values, Map<String, Object> remainParams) {
            this.parameterName = parameterName;
            this.values = values;
            this.remainParams = remainParams;
        }

        @Override
        public Iterator<RouteObject> iterator() {
            return CollectionUtil.transformList(values, new Transformer<Object, RouteObject>() {
                @Override
                public RouteObject transform(Object object) {
                    return new PureValueRouteObject(object);
                }
            }).iterator();
        }

        @Override
        public void merge(ShardInfo shardInfo, RouteObject routeObject) {
            List<Object> values = shardValues.get(shardInfo);
            if (values == null) {
                values = CollectionUtil.newArrayList();
                shardValues.put(shardInfo, values);
            }
            values.add(routeObject.getObject());
        }

        @Override
        public Map<ShardInfo, Object> getMergedResult() {
            Map<ShardInfo, Object> ret = MapUtil.newHashMap();
            for (Map.Entry<ShardInfo, List<Object>> entry : shardValues.entrySet()) {
                ShardInfo shardInfo = entry.getKey();
                List<Object> values = entry.getValue();
                Map<String, Object> m = MapUtil.newHashMap();
                m.put(parameterName, values);
                m.putAll(remainParams);
                ret.put(shardInfo, m);
            }
            return ret;
        }
    }

    private static class CollectionRouteObjectOperator implements RouteObjectOperator {

        private Collection objects;

        private Map<ShardInfo, Object> shardValues = MapUtil.newHashMap();

        public CollectionRouteObjectOperator(Collection objects) {
            this.objects = objects;
        }

        @Override
        public Iterator<RouteObject> iterator() {
            return CollectionUtil.transformList(objects, new Transformer<Object, RouteObject>() {
                @Override
                public RouteObject transform(Object object) {
                    return new PlainRouteObject(object);
                }
            }).iterator();
        }

        @Override
        public void merge(ShardInfo shardInfo, RouteObject routeObject) {
            List<Object> values = (List<Object>) shardValues.get(shardInfo);
            if (values == null) {
                values = CollectionUtil.newArrayList();
                shardValues.put(shardInfo, values);
            }
            values.add(routeObject.getObject());
        }

        @Override
        public Map<ShardInfo, Object> getMergedResult() {
            return shardValues;
        }
    }

    private static class PureValueRouteObject implements RouteObject {

        private Object value;

        public PureValueRouteObject(Object value) {
            this.value = value;
        }

        @Override
        public Object getRouteValue(String propertyName) {
            return value;
        }

        public Object getObject() {
            return value;
        }
    }

    private static class PlainRouteObject implements RouteObject {

        private Object object;

        public PlainRouteObject(Object object) {
            this.object = object;
        }

        @Override
        public Object getRouteValue(String propertyName) {
            MagicObject magicObject = MagicObject.wrap(object);
            return magicObject.getPropertyValue(propertyName);
        }

        public Object getObject() {
            return object;
        }
    }


}
