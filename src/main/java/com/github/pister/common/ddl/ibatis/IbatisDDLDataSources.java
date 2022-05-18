package com.github.pister.common.ddl.ibatis;


import com.github.pister.common.ddl.config.TableConfig;
import com.github.pister.common.ddl.config.TableNameConfig;
import com.github.pister.common.ddl.datasource.DataSourceGroup;
import com.github.pister.common.ddl.datasource.DataSourcePoolMatrix;
import com.github.pister.common.ddl.id.Sequence;
import com.github.pister.common.ddl.id.seq.DbTableRangeLoader;
import com.github.pister.common.ddl.id.seq.LocalRangeSequence;
import com.github.pister.common.ddl.route.DefaultRouterManger;
import com.github.pister.common.ddl.route.Router;
import com.github.pister.common.ddl.route.RouterManger;
import com.github.pister.common.lang.util.MapUtil;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.ibatis.SqlMapClientFactoryBean;
import wint.dal.ibatis.DefaultSqlExecutor;
import wint.dal.ibatis.ReadWriteSqlMapClientSource;
import wint.dal.ibatis.SqlExecutor;

import javax.sql.DataSource;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午2:06
 */
public class IbatisDDLDataSources implements InitializingBean {

    private Map<Integer, SqlExecutor> databaseIndexedSqlMapClients = MapUtil.newHashMap();
    private Map<Integer, DataSourceGroup> shardDataSources = MapUtil.newHashMap();

    private Map<String, TableConfig> tableConfigMap;
    private RouterManger routerManger;

    private SqlExecutorManager sqlExecutorManager;

    private DbTableRangeLoader dbTableRangeLoader;

    private DataSource sequenceDataSource;

    private int sequenceDataSourceIndex;

    private String configLocation = "sql-map.xml";

    private Integer idRangeStep;

    // TODO 临时增加
    private String tableIndexFormat = TableNameConfig.tableIndexFormat;

    public void afterPropertiesSet() throws Exception {
        for (Map.Entry<Integer, DataSourceGroup> entry : shardDataSources.entrySet()) {
            int index = entry.getKey();
            DataSourceGroup dataSourceGroup = entry.getValue();
            ReadWriteSqlMapClientSource readWriteSqlMapClientSource = new ReadWriteSqlMapClientSource();
            readWriteSqlMapClientSource.setMasterDataSource(dataSourceGroup.getMaster());
            readWriteSqlMapClientSource.setSlaveDataSources(dataSourceGroup.getSlavers());

            SqlMapClientFactoryBean sqlMapClientFactoryBean = new SqlMapClientFactoryBean();
            sqlMapClientFactoryBean.setDataSource(dataSourceGroup.getMaster());
            sqlMapClientFactoryBean.setConfigLocation(new ClassPathResource(configLocation));
            sqlMapClientFactoryBean.afterPropertiesSet();
            SqlMapClient sqlMapClient = (SqlMapClient)sqlMapClientFactoryBean.getObject();
            readWriteSqlMapClientSource.setSqlMapClient(sqlMapClient);

            readWriteSqlMapClientSource.afterPropertiesSet();

            SqlExecutor sqlExecutor = new DefaultSqlExecutor(readWriteSqlMapClientSource);
            databaseIndexedSqlMapClients.put(index, sqlExecutor);
        }

        DefaultRouterManger defaultRouterManger = new DefaultRouterManger();
        defaultRouterManger.setTableConfigMap(tableConfigMap);
        defaultRouterManger.init();
        this.routerManger = defaultRouterManger;
        this.sqlExecutorManager = new SqlExecutorManager(databaseIndexedSqlMapClients);

        if (sequenceDataSource == null) {
            DataSourceGroup dataSourceGroup = shardDataSources.get(sequenceDataSourceIndex);
            if (dataSourceGroup == null || dataSourceGroup.getMaster() == null) {
                throw new RuntimeException("property sequenceDataSource cat not be null!");
            }
            sequenceDataSource = dataSourceGroup.getMaster();
        }
        DbTableRangeLoader dbTableRangeLoader = new DbTableRangeLoader();
        dbTableRangeLoader.setDataSource(sequenceDataSource);
        if (idRangeStep != null && idRangeStep > 0) {
            dbTableRangeLoader.setStep(idRangeStep);
        }
        dbTableRangeLoader.init();
        this.dbTableRangeLoader = dbTableRangeLoader;

        TableNameConfig.tableIndexFormat = this.tableIndexFormat;
    }

    public DistributedSqlExecutor getDistributedSqlExecutor(String name) {
        Router router = routerManger.getRouter(name);
        TableConfig tableConfig = routerManger.getTableConfig(name);
        return new DistributedSqlExecutor(router, sqlExecutorManager, tableConfig.getIdName());
    }

    public RouterManger getRouterManger() {
        return routerManger;
    }

    public Sequence getSequence(String name) {
        return new LocalRangeSequence(dbTableRangeLoader, name);
    }

    public void setShardDataSources(Map<Integer, DataSourceGroup> shardDataSources) {
        this.shardDataSources = shardDataSources;
    }

    public void setTableConfigMap(Map<String, TableConfig> tableConfigMap) {
        this.tableConfigMap = tableConfigMap;
    }

    public void setSequenceDataSource(DataSource sequenceDataSource) {
        this.sequenceDataSource = sequenceDataSource;
    }

    public void setDataSourcePoolMatrix(DataSourcePoolMatrix dataSourcePoolMatrix) {
        if (dataSourcePoolMatrix != null) {
            this.shardDataSources = dataSourcePoolMatrix.getShardDataSourceGroup();
        }
    }

    public void setSequenceDataSourceIndex(int sequenceDataSourceIndex) {
        this.sequenceDataSourceIndex = sequenceDataSourceIndex;
    }

    public void setTableIndexFormat(String tableIndexFormat) {
        this.tableIndexFormat = tableIndexFormat;
    }

    public void setIdRangeStep(Integer idRangeStep) {
        this.idRangeStep = idRangeStep;
    }
}
