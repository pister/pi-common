package com.github.pister.common.cache.help;

/**
 * User: huangsongli
 * Date: 16/10/28
 * Time: 下午1:25
 */
public interface LRUUnit<T> {
    T get(String key);

    void clear();

    int size();

    boolean isEmpty();

    T put(String key, T value);

    T remove(String key);
}
