package com.github.pister.common.ddl.ibatis;

import org.springframework.beans.factory.InitializingBean;
import wint.dal.ibatis.DefaultSqlExecutor;
import wint.dal.ibatis.SqlExecutor;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午2:18
 */
public abstract class FixedRouteSqlMapClientDaoSupport implements InitializingBean {

    private FixedRouteSqlMapClientSource fixedRouteSqlMapClientSource;
    private SqlExecutor sqlExecutor;

    public void afterPropertiesSet() throws Exception {
        this.sqlExecutor = new DefaultSqlExecutor(fixedRouteSqlMapClientSource.getReadWriteSqlMapClientSource());
    }

    public SqlExecutor getSqlExecutor() {
        return sqlExecutor;
    }

    public SqlExecutor getSqlMapClientTemplate() {
        return getSqlExecutor();
    }

    public void setFixedRouteSqlMapClientSource(FixedRouteSqlMapClientSource fixedRouteSqlMapClientSource) {
        this.fixedRouteSqlMapClientSource = fixedRouteSqlMapClientSource;
    }
}
