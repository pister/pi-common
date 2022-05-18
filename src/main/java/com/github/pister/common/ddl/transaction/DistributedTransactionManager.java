package com.github.pister.common.ddl.transaction;

import com.github.pister.common.ddl.datasource.DataSourceGroup;
import com.github.pister.common.ddl.datasource.DataSourcePoolMatrix;
import com.github.pister.common.ddl.ibatis.IbatisDDLDataSources;
import com.github.pister.common.ddl.route.Router;
import com.github.pister.common.ddl.runtime.ParamsContexts;
import com.github.pister.common.ddl.shard.ShardInfo;
import com.github.pister.common.ddl.transaction.support.DataSourceTransactionManager;
import org.springframework.beans.factory.InitializingBean;
import wint.lang.utils.MapUtil;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 分布式事务支持，仅支持在同一数据库的事务
 * <p>
 * User: huangsongli
 * Date: 16/10/12
 * Time: 下午1:30
 */
public class DistributedTransactionManager implements InitializingBean {

    private DataSourcePoolMatrix dataSourcePoolMatrix;

    private IbatisDDLDataSources ibatisDDLDataSources;

    private Map<Integer, TransactionManager> shardTransactionManager;

    /**
     * 指定数据库索引执行
     *
     * @param dbIndex
     * @param transactionCallback
     * @param <T>
     * @return
     */
    public <T> T execute(int dbIndex, TransactionCallback<T> transactionCallback) {
        TransactionManager transactionManager = shardTransactionManager.get(dbIndex);
        if (transactionManager == null) {
            throw new RuntimeException("not found transaction for dbIndex:" + dbIndex);
        }
        return transactionManager.execute(transactionCallback);
    }

    /**
     * 根据数据库路由的配置和路由参数执行事务
     *
     * @param routeName
     * @param params
     * @param transactionCallback
     * @param <T>
     * @return
     */
    public <T> T execute(String routeName, Map<String, Object> params, TransactionCallback<T> transactionCallback) {
        Router router = ibatisDDLDataSources.getRouterManger().getRouter(routeName);
        if (router == null) {
            throw new RuntimeException("can not find route name:" + routeName);
        }
        ShardInfo shardInfo = router.route(ParamsContexts.create(params));
        int dbIndex = shardInfo.getDatabaseIndex();
        return execute(dbIndex, transactionCallback);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Map<Integer, TransactionManager> shardTransactionManager = MapUtil.newHashMap();
        Map<Integer, DataSourceGroup> dataSourceGroupMap = dataSourcePoolMatrix.getShardDataSourceGroup();
        for (Map.Entry<Integer, DataSourceGroup> entry : dataSourceGroupMap.entrySet()) {
            DataSourceGroup dataSourceGroup = entry.getValue();
            shardTransactionManager.put(entry.getKey(), createTransactionManager(dataSourceGroup.getMaster()));
        }
        this.shardTransactionManager = shardTransactionManager;
    }

    protected TransactionManager createTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        dataSourceTransactionManager.afterPropertiesSet();
        return dataSourceTransactionManager;
    }

    public void setDataSourcePoolMatrix(DataSourcePoolMatrix dataSourcePoolMatrix) {
        this.dataSourcePoolMatrix = dataSourcePoolMatrix;
    }

    public void setIbatisDDLDataSources(IbatisDDLDataSources ibatisDDLDataSources) {
        this.ibatisDDLDataSources = ibatisDDLDataSources;
    }
}
