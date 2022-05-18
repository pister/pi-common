package com.github.pister.common.ddl.transaction.support;

import com.github.pister.common.ddl.transaction.TransactionCallback;
import com.github.pister.common.ddl.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 支持嵌套的事务
 *
 * User: huangsongli
 */
@SuppressWarnings("rawtypes")
public class ReentrantTransactionManager implements TransactionManager {

    private final static ThreadLocal<TransactionCallback> rootTransactionCallbackTL = new ThreadLocal<TransactionCallback>();
    private static final Logger log = LoggerFactory.getLogger(ReentrantTransactionManager.class);
    private TransactionManager transactionManager;

    @Override
    public <T> T execute(TransactionCallback<T> transactionCallback) {
        TransactionCallback rootTransactionCallback = getRootTransactionCallback();
        if (rootTransactionCallback == null) {
            // 如果没有根事务，把当前事务设置成根事务
            rootTransactionCallback = transactionCallback;
            setRootTransactionCallback(transactionCallback);
        }
        log.warn("root: " + rootTransactionCallback);
        if (rootTransactionCallback == transactionCallback) {
            // 如果当前事务就就是根事务，执行事务
            try {
                log.warn("do transaction start... ");
                return transactionManager.execute(transactionCallback);
            } finally {
                log.warn("finish transaction. ");
                clearRootTransactionCallback();
            }
        } else {
            // 直接执行
            log.warn("execute direct start... ");
            T t = transactionCallback.doTransaction();
            log.warn("execute direct finish. ");
            return t;
        }
    }

    private TransactionCallback getRootTransactionCallback() {
        return rootTransactionCallbackTL.get();
    }

    private void setRootTransactionCallback(TransactionCallback transactionCallback) {
        rootTransactionCallbackTL.set(transactionCallback);
    }

    private void clearRootTransactionCallback() {
        rootTransactionCallbackTL.remove();
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
