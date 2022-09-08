package com.github.pister.common.ddl.ibatis;

import com.github.pister.common.ddl.route.Router;
import com.github.pister.common.ddl.shard.ShardInfo;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import wint.dal.ibatis.spring.SqlMapClientCallback;

import java.sql.SQLException;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午7:19
 */
public class PropertiesSupportSqlMapClientCallback implements SqlMapClientCallback {

    private SqlMapClientCallback target;

    private ShardInfo routeShardInfo;

    private Router router;

    private String idName;

    public PropertiesSupportSqlMapClientCallback(SqlMapClientCallback target, ShardInfo routeShardInfo, Router router, String idName) {
        this.target = target;
        this.routeShardInfo = routeShardInfo;
        this.router = router;
        this.idName = idName;
    }

    @Override
    public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {
        PropertiesSupportSqlMapExecutor propertiesSupportSqlMapExecutor = new PropertiesSupportSqlMapExecutor(executor, routeShardInfo, router, idName);
        return target.doInSqlMapClient(propertiesSupportSqlMapExecutor);
    }

}
