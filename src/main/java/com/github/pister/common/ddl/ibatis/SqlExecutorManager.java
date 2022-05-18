package com.github.pister.common.ddl.ibatis;

import com.github.pister.common.ddl.shard.ShardInfo;
import wint.dal.ibatis.SqlExecutor;

import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 上午10:36
 */
public class SqlExecutorManager {

    private Map<Integer, SqlExecutor> databaseIndexedSqlMapClients;

    public SqlExecutorManager(Map<Integer, SqlExecutor> databaseIndexedSqlMapClients) {
        this.databaseIndexedSqlMapClients = databaseIndexedSqlMapClients;
    }

    public SqlExecutor getSqlExecutor(ShardInfo shardInfo) {
        return databaseIndexedSqlMapClients.get(shardInfo.getDatabaseIndex());
    }


}
