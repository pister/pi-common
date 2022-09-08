package com.github.pister.common.ddl.ibatis;

import com.github.pister.common.ddl.datasource.DataSourceGroup;
import com.github.pister.common.ddl.datasource.DataSourcePoolMatrix;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import wint.dal.ibatis.ReadWriteSqlMapClientSource;
import wint.dal.ibatis.ext.ExtSqlMapClientFactoryBean;

/**
 * User: huangsongli
 * Date: 16/5/18
 * Time: 上午9:42
 */
public class FixedRouteSqlMapClientSource implements InitializingBean {

    private DataSourcePoolMatrix dataSourcePoolMatrix;
    private String configLocation = "sql-map.xml";
    private ReadWriteSqlMapClientSource readWriteSqlMapClientSource;
    private int dataSourceRouteIndex = 0;

    public void afterPropertiesSet() throws Exception {
        DataSourceGroup dataSourceGroup = dataSourcePoolMatrix.getShardDataSourceGroup().get(dataSourceRouteIndex);

        ReadWriteSqlMapClientSource readWriteSqlMapClientSource = new ReadWriteSqlMapClientSource();
        readWriteSqlMapClientSource.setMasterDataSource(dataSourceGroup.getMaster());
        readWriteSqlMapClientSource.setSlaveDataSources(dataSourceGroup.getSlavers());

        ExtSqlMapClientFactoryBean sqlMapClientFactoryBean = new ExtSqlMapClientFactoryBean();
        sqlMapClientFactoryBean.setDataSource(dataSourceGroup.getMaster());
        sqlMapClientFactoryBean.setConfigLocation(new ClassPathResource(configLocation));
        sqlMapClientFactoryBean.afterPropertiesSet();
        SqlMapClient sqlMapClient = (SqlMapClient) sqlMapClientFactoryBean.getObject();
        readWriteSqlMapClientSource.setSqlMapClient(sqlMapClient);

        readWriteSqlMapClientSource.afterPropertiesSet();

        this.readWriteSqlMapClientSource = readWriteSqlMapClientSource;

    }

    public ReadWriteSqlMapClientSource getReadWriteSqlMapClientSource() {
        return readWriteSqlMapClientSource;
    }

    public void setDataSourceRouteIndex(int dataSourceRouteIndex) {
        this.dataSourceRouteIndex = dataSourceRouteIndex;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public void setDataSourcePoolMatrix(DataSourcePoolMatrix dataSourcePoolMatrix) {
        this.dataSourcePoolMatrix = dataSourcePoolMatrix;
    }
}
