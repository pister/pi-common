package com.github.pister.common.transaction.support;

import com.github.pister.common.transaction.TransactionManager;
import com.github.pister.common.transaction.TransactionCallback;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: huangsongli
 * Date: 16/5/17
 * Time: 下午1:34
 */
public class DataSourceTransactionManager implements TransactionManager, InitializingBean {

    private TransactionTemplate transactionTemplate;
    private DataSource dataSource;
    private AtomicBoolean inited = new AtomicBoolean(false);

    public void afterPropertiesSet() {
        if (!inited.compareAndSet(false, true)) {
            return;
        }
        org.springframework.jdbc.datasource.DataSourceTransactionManager dataSourceTransactionManager = new org.springframework.jdbc.datasource.DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        dataSourceTransactionManager.afterPropertiesSet();

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);

        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T execute(final TransactionCallback<T> transactionCallback) {
        return (T) transactionTemplate.execute(new org.springframework.transaction.support.TransactionCallback() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                return transactionCallback.doTransaction();
            }
        });
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
