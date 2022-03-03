package com.github.pister.common.cache;

/**
 * User: huangsongli
 * Date: 16/4/5
 * Time: 上午11:06
 */
public interface Cache {

    Object get(String key);

    void set(String key, Object object, int expireInSeconds);

    void delete(String key);

    void clearAll();

}
