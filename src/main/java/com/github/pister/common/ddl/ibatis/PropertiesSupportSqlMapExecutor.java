package com.github.pister.common.ddl.ibatis;

import com.github.pister.common.ddl.route.Router;
import com.github.pister.common.ddl.runtime.ParamsContexts;
import com.github.pister.common.ddl.runtime.SqlInputProperties;
import com.github.pister.common.ddl.shard.ShardInfo;
import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.BatchException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午7:21
 */
public class PropertiesSupportSqlMapExecutor implements SqlMapExecutor {

    private SqlMapExecutor sqlMapExecutor;
    private ShardInfo routeShardInfo;
    private Router router;

    private String idName;

    public PropertiesSupportSqlMapExecutor(SqlMapExecutor sqlMapExecutor, ShardInfo routeShardInfo, Router router, String idName) {
        this.sqlMapExecutor = sqlMapExecutor;
        this.routeShardInfo = routeShardInfo;
        this.router = router;
        this.idName = idName;
    }

    protected Object makeSqlInputPropertiesObject(Object parameterObject) {
        SqlInputProperties sqlInputProperties = makeSqlInputProperties(parameterObject);
        return sqlInputProperties.getPropertyValues();
    }

    protected SqlInputProperties makeSqlInputProperties(Object parameterObject) {
        SqlInputProperties sqlInputProperties = ParamsContexts.create(parameterObject);
        if (parameterObject == null) {
            sqlInputProperties.setTableIndex(routeShardInfo.getTableIndex());
            return sqlInputProperties;
        }
        ShardInfo propertyShardInfo = router.route(sqlInputProperties);
        if (!propertyShardInfo.equals(routeShardInfo)) {
            throw new RuntimeException("route shard is: " + routeShardInfo + ", but property shard is: " + propertyShardInfo);
        }
        sqlInputProperties.setTableIndex(routeShardInfo.getTableIndex());
        return sqlInputProperties;
    }

    @Override
    public Object insert(String id, Object parameterObject) throws SQLException {
        SqlInputProperties sqlInputProperties = makeSqlInputProperties(parameterObject);
        /*
        if (!sqlInputProperties.checkIdExist(idName)) {
            throw new RuntimeException("property: " + idName + " cat not be empty or zero!");
        }
        */
        return sqlMapExecutor.insert(id, sqlInputProperties.getPropertyValues());
    }

    @Override
    public Object insert(String id) throws SQLException {
        return insert(id, null);
    }

    @Override
    public int update(String id, Object parameterObject) throws SQLException {
        return sqlMapExecutor.update(id, makeSqlInputPropertiesObject(parameterObject));
    }

    @Override
    public int update(String id) throws SQLException {
        return sqlMapExecutor.update(id, makeSqlInputPropertiesObject(null));
    }

    @Override
    public int delete(String id, Object parameterObject) throws SQLException {
        return sqlMapExecutor.delete(id, makeSqlInputPropertiesObject(parameterObject));
    }

    @Override
    public int delete(String id) throws SQLException {
        return sqlMapExecutor.delete(id, makeSqlInputPropertiesObject(null));
    }

    @Override
    public Object queryForObject(String id, Object parameterObject) throws SQLException {
        return sqlMapExecutor.queryForObject(id, makeSqlInputPropertiesObject(parameterObject));
    }

    @Override
    public Object queryForObject(String id) throws SQLException {
        return sqlMapExecutor.queryForObject(id, makeSqlInputPropertiesObject(null));
    }

    @Override
    public Object queryForObject(String id, Object parameterObject, Object resultObject) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List queryForList(String id, Object parameterObject) throws SQLException {
        return sqlMapExecutor.queryForList(id, makeSqlInputPropertiesObject(parameterObject));
    }

    @Override
    public List queryForList(String id) throws SQLException {
        return sqlMapExecutor.queryForList(id, makeSqlInputPropertiesObject(null));
    }

    @Override
    public List queryForList(String id, Object parameterObject, int skip, int max) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List queryForList(String id, int skip, int max) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void queryWithRowHandler(String id, Object parameterObject, RowHandler rowHandler) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void queryWithRowHandler(String id, RowHandler rowHandler) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaginatedList queryForPaginatedList(String id, Object parameterObject, int pageSize) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PaginatedList queryForPaginatedList(String id, int pageSize) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map queryForMap(String id, Object parameterObject, String keyProp) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map queryForMap(String id, Object parameterObject, String keyProp, String valueProp) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startBatch() throws SQLException {
        sqlMapExecutor.startBatch();
    }

    @Override
    public int executeBatch() throws SQLException {
        return sqlMapExecutor.executeBatch();
    }

    @Override
    public List executeBatchDetailed() throws SQLException, BatchException {
        return sqlMapExecutor.executeBatchDetailed();
    }
}
