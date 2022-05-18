package com.github.pister.common.ddl.ibatis;

import com.github.pister.common.ddl.route.MultiRouteUtil;
import com.github.pister.common.ddl.route.Router;
import com.github.pister.common.ddl.runtime.ParamsContexts;
import com.github.pister.common.ddl.runtime.SqlInputProperties;
import com.github.pister.common.ddl.shard.ShardInfo;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ibatis.SqlMapClientCallback;
import wint.dal.ibatis.SqlExecutor;
import wint.lang.utils.CollectionUtil;
import wint.lang.utils.MapUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:51
 */
public class DistributedSqlExecutor implements SqlExecutor {

    private static final Logger log = LoggerFactory.getLogger(DistributedSqlExecutor.class);

    private Router router;
    private SqlExecutorManager sqlExecutorManager;
    private String idName;

    public DistributedSqlExecutor(Router router, SqlExecutorManager sqlExecutorManager, String idName) {
        this.router = router;
        this.sqlExecutorManager = sqlExecutorManager;
        this.idName = idName;
    }

    protected Router getRouter() {
        return router;
    }

    @Override
    public Object execute(SqlMapClientCallback action) {
        throw new UnsupportedOperationException("You must use DistributedSqlExecutor.execute(Object routeParameter, SqlMapClientCallback action) instead!");
    }

    /**
     * 用于批量处理
     *
     * @param routeParameter 该字段可以是一个普通数据对象或是map，属性中必须路由字段和值
     *                       举例：如果路由字段是cat，该对象应该类似{catId:123}
     * @param action
     * @return
     */
    public Object execute(Object routeParameter, SqlMapClientCallback action) {
        SqlInputProperties sqlInputProperties = ParamsContexts.create(routeParameter);
        ShardInfo shardInfo = getRouter().route(sqlInputProperties);
        sqlInputProperties.setTableIndex(shardInfo.getTableIndex());
        return execute(shardInfo, action);
    }

    public Object execute(ShardInfo shardInfo, SqlMapClientCallback action) {
        PropertiesSupportSqlMapClientCallback propertiesSupportSqlMapClientCallback = new PropertiesSupportSqlMapClientCallback(action, shardInfo, getRouter(), idName);
        return sqlExecutorManager.getSqlExecutor(shardInfo).execute(propertiesSupportSqlMapClientCallback);
    }

    /**
     * 用于批量处理
     *
     * @param propertyName  路由字段名
     * @param statementName
     * @param inputObjects
     * @param executor
     * @return 批量更新的行数
     */
    public int executeBatchMultiRoute(String propertyName, String statementName, List inputObjects, Executor executor) {
        return (Integer) executeMultiRoute(propertyName, statementName, inputObjects, executor, new ShardCallback<String>() {

            private AtomicInteger rows = new AtomicInteger(0);

            @Override
            public void onShardCallback(final String statementName, final Object parameterObject, ShardInfo shardInfo) {
                if (parameterObject instanceof Collection) {
                    final List parameterList = (List) parameterObject;
                    if (CollectionUtil.isEmpty(parameterList)) {
                        return;
                    }
                    int n = (Integer) execute(shardInfo, new SqlMapClientCallback() {
                        @Override
                        public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {
                            executor.startBatch();
                            for (Object param : parameterList) {
                                executor.update(statementName, param);
                            }
                            return executor.executeBatch();
                        }
                    });
                    rows.addAndGet(n);
                } else {
                    rows.addAndGet(update(statementName, parameterObject));
                }
            }

            @Override
            public void onInit() {

            }

            @Override
            public Object getResult() {
                return rows.get();
            }
        });
    }

    private static final Executor DEFAULT_EXECUTOR = new Executor() {

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    /**
     * 多路由查询列表 (目前仅支持单个属性，不支持父子属性)
     * 注意：由当前线程串行执行
     *
     * @param propertyName    路由字段名
     * @param statementName
     * @param parameterObject
     * @return
     */
    public List queryForListMultiRoute(String propertyName, final String statementName, final Object parameterObject) {
        return queryForListMultiRoute(propertyName, statementName, parameterObject, DEFAULT_EXECUTOR);
    }

    protected interface ShardCallback<T> {
        void onShardCallback(T arg, Object parameterObject, ShardInfo shardInfo);

        void onInit();

        Object getResult();
    }


    /**
     * 多路由执行sql， (目前仅支持单个属性，不支持父子属性)
     *
     * @param propertyName
     * @param arg
     * @param parameterObject
     * @param executor
     * @param shardCallback   注意该实例需要坚持线程安全！！！
     * @return
     */
    protected <T> Object executeMultiRoute(String propertyName, final T arg, final Object parameterObject, Executor executor, final ShardCallback<T> shardCallback) {
        Map<ShardInfo, Object> groupObjects = MultiRouteUtil.groupByShard(getRouter(), propertyName, parameterObject);
        shardCallback.onInit();
        final CountDownLatch countDownLatch = new CountDownLatch(groupObjects.size());
        for (Map.Entry<ShardInfo, Object> entry : groupObjects.entrySet()) {
            final ShardInfo shardInfo = entry.getKey();
            final Object shardRouteValues = entry.getValue();
            /*
            SqlInputProperties shardSqlInputProperties = ParamsContexts.create(parameterObject);
            shardSqlInputProperties.setProperty(propertyName, entry.getValue());
            */
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        shardCallback.onShardCallback(arg, shardRouteValues, shardInfo);
                    } catch (Exception e) {
                        log.error("error", e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return shardCallback.getResult();
    }

    public int updateMultiRoute(String propertyName, final String statementName, final Object parameterObject) {
        return updateMultiRoute(propertyName, statementName, parameterObject, DEFAULT_EXECUTOR);
    }

    public int updateMultiRoute(String propertyName, final String statementName, final Object parameterObject, Executor executor) {
        return (Integer) executeMultiRoute(propertyName, statementName, parameterObject, executor, new ShardCallback<String>() {

            private AtomicInteger rows = new AtomicInteger(0);

            @Override
            public void onShardCallback(String statementName, Object parameterObject, ShardInfo shardInfo) {
                int n = update(statementName, parameterObject, shardInfo);
                rows.addAndGet(n);
            }

            @Override
            public void onInit() {
            }

            @Override
            public Object getResult() {
                return rows.get();
            }
        });
    }

    /**
     * 多路由查询列表 (目前仅支持单个属性，不支持父子属性)
     *
     * @param propertyName    路由字段名
     * @param statementName
     * @param parameterObject
     * @param executor        多路由执行的线程executor
     * @return
     */
    public List queryForListMultiRoute(String propertyName, final String statementName, final Object parameterObject, Executor executor) {
        return (List) executeMultiRoute(propertyName, statementName, parameterObject, executor, new ShardCallback<String>() {

            private List<Object> resultList = Collections.synchronizedList(new ArrayList<Object>());

            @Override
            public void onShardCallback(String statementName, Object parameterObject, ShardInfo shardInfo) {
                resultList.addAll(queryForList(statementName, parameterObject, shardInfo));
            }

            @Override
            public void onInit() {
            }

            @Override
            public Object getResult() {
                return CollectionUtil.newArrayList(resultList);
            }
        });
    }

    private Map<ShardInfo, List<Object>> multiRouteValues(String propertyName, Iterator iterator) {
        Map<ShardInfo, List<Object>> shardValues = MapUtil.newHashMap();
        while (iterator.hasNext()) {
            ParamsContexts.ObjectRoute obj = (ParamsContexts.ObjectRoute) iterator.next();
            Map<String, Object> params = MapUtil.newHashMap();
            params.put(propertyName, obj.getRouteValue());
            SqlInputProperties sqlInputProperties = ParamsContexts.create(params);
            ShardInfo shardInfo = getRouter().route(sqlInputProperties);
            sqlInputProperties.setTableIndex(shardInfo.getTableIndex());

            List<Object> values = shardValues.get(shardInfo);
            if (values == null) {
                values = CollectionUtil.newArrayList();
                shardValues.put(shardInfo, values);
            }
            values.add(obj.getObject());
        }
        return shardValues;
    }


    @Override
    public Object queryForObject(String statementName, Object parameterObject) {
        return queryForObject(statementName, parameterObject, null);
    }

    public Object queryForObject(String statementName, Object parameterObject, ShardInfo shardInfo) {
        SqlExecutorAndSqlInputProperties sqlExecutorAndSqlInputProperties = handleProperty(parameterObject, shardInfo);
        return sqlExecutorAndSqlInputProperties.sqlExecutor.queryForObject(statementName, sqlExecutorAndSqlInputProperties.getPropertiesObject());
    }

    protected SqlExecutorAndSqlInputProperties handleProperty(Object parameterObject, ShardInfo shardInfo) {
        SqlInputProperties sqlInputProperties = ParamsContexts.create(parameterObject);
        if (shardInfo == null) {
            shardInfo = getRouter().route(sqlInputProperties);
        }
        sqlInputProperties.setTableIndex(shardInfo.getTableIndex());
        SqlExecutor sqlExecutor = sqlExecutorManager.getSqlExecutor(shardInfo);
        return new SqlExecutorAndSqlInputProperties(sqlExecutor, sqlInputProperties);
    }

    @Override
    public List queryForList(String statementName, Object parameterObject) {
        return queryForList(statementName, parameterObject, null);
    }

    public List queryForList(String statementName, Object parameterObject, ShardInfo shardInfo) {
        SqlExecutorAndSqlInputProperties sqlExecutorAndSqlInputProperties = handleProperty(parameterObject, shardInfo);
        return sqlExecutorAndSqlInputProperties.sqlExecutor.queryForList(statementName, sqlExecutorAndSqlInputProperties.getPropertiesObject());
    }

    @Override
    public Object insert(String statementName, Object parameterObject) {
        return insert(statementName, parameterObject, null);
    }

    public Object insert(String statementName, Object parameterObject, ShardInfo shardInfo) {
        SqlExecutorAndSqlInputProperties sqlExecutorAndSqlInputProperties = handleProperty(parameterObject, shardInfo);
        /*
        if (!sqlExecutorAndSqlInputProperties.sqlInputProperties.checkIdExist(idName)) {
            throw new RuntimeException("property: " + idName + " cat not be empty or zero!");
        }
        */
        return sqlExecutorAndSqlInputProperties.sqlExecutor.insert(statementName, sqlExecutorAndSqlInputProperties.getPropertiesObject());
    }

    @Override
    public int update(String statementName, Object parameterObject) {
        return update(statementName, parameterObject, null);
    }

    public int update(String statementName, Object parameterObject, ShardInfo shardInfo) {
        SqlExecutorAndSqlInputProperties sqlExecutorAndSqlInputProperties = handleProperty(parameterObject, shardInfo);
        return sqlExecutorAndSqlInputProperties.sqlExecutor.update(statementName, sqlExecutorAndSqlInputProperties.getPropertiesObject());
    }

    @Override
    public int delete(String statementName, Object parameterObject) {
        return delete(statementName, parameterObject, null);
    }

    public int delete(String statementName, Object parameterObject, ShardInfo shardInfo) {
        SqlExecutorAndSqlInputProperties sqlExecutorAndSqlInputProperties = handleProperty(parameterObject, shardInfo);
        return sqlExecutorAndSqlInputProperties.sqlExecutor.delete(statementName, sqlExecutorAndSqlInputProperties.getPropertiesObject());
    }

    @Override
    public Object queryForObject(String statementName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List queryForList(String statementName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object insert(String statementName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(String statementName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(String statementName) {
        throw new UnsupportedOperationException();
    }


    private static class SqlExecutorAndSqlInputProperties {
        SqlExecutor sqlExecutor;
        SqlInputProperties sqlInputProperties;

        private SqlExecutorAndSqlInputProperties(SqlExecutor sqlExecutor, SqlInputProperties sqlInputProperties) {
            this.sqlExecutor = sqlExecutor;
            this.sqlInputProperties = sqlInputProperties;
        }

        public Object getPropertiesObject() {
            return sqlInputProperties.getPropertyValues();
        }
    }
}
