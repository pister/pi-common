package com.github.pister.common.ddl.transaction;

/**
 * User: huangsongli
 * Date: 16/5/17
 * Time: 下午3:44
 */
public interface TransactionCallback<T> {

    T doTransaction();
}
