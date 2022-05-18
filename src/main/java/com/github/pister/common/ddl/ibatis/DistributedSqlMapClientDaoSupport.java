package com.github.pister.common.ddl.ibatis;


import com.github.pister.common.ddl.id.Sequence;
import org.springframework.beans.factory.InitializingBean;

/**
 * User: huangsongli
 * Date: 16/4/28
 * Time: 下午2:18
 */
public abstract class DistributedSqlMapClientDaoSupport implements InitializingBean {

    protected DistributedSqlExecutor sqlMapExecutor;
    private IbatisDDLDataSources ibatisDDLDataSources;
    private Sequence sequence;


    public void afterPropertiesSet() throws Exception {
        sqlMapExecutor = ibatisDDLDataSources.getDistributedSqlExecutor(getName());
        sequence = ibatisDDLDataSources.getSequence(getName());
    }

    public DistributedSqlExecutor getSqlExecutor() {
        return sqlMapExecutor;
    }

    public DistributedSqlExecutor getSqlMapClientTemplate() {
        return getSqlExecutor();
    }

    protected Sequence getSequence() {
        return sequence;
    }

    protected abstract String getName();

    public void setIbatisDDLDataSources(IbatisDDLDataSources ibatisDDLDataSources) {
        this.ibatisDDLDataSources = ibatisDDLDataSources;
    }

}
