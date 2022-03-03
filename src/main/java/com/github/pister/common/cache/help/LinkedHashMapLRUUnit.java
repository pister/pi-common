package com.github.pister.common.cache.help;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/10/28
 * Time: 下午12:54
 */
public class LinkedHashMapLRUUnit<T> extends LinkedHashMap<String, T> implements LRUUnit<T> {

    private int maxSize;

    private Object lockObject = new Object();

    public LinkedHashMapLRUUnit(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public T get(String key) {
        synchronized (lockObject) {
            return super.get(key);
        }
    }

    @Override
    public void clear() {
        synchronized (lockObject) {
            super.clear();
        }
    }

    @Override
    public int size() {
        synchronized (lockObject) {
            return super.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lockObject) {
            return super.isEmpty();
        }
    }

    @Override
    public T put(String key, T value) {
        synchronized (lockObject) {
            return super.put(key, value);
        }
    }

    @Override
    public T remove(String key) {
        synchronized (lockObject) {
            return super.remove(key);
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
        return size() > maxSize;
    }
}
