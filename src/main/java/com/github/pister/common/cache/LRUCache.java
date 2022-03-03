package com.github.pister.common.cache;

import com.github.pister.common.cache.help.LRUUnit;
import com.github.pister.common.cache.help.ObjectWithExpire;
import com.github.pister.common.lang.util.StringUtil;
import com.github.pister.common.cache.help.LinkedHashMapLRUUnit;
import com.github.pister.common.lang.util.DateUtil;

import java.lang.ref.SoftReference;
import java.util.Date;

/**
 * 一个线程安全，且性能不错的lru cache;
 * 注意：该类的最大容量只是大概的大小，不是精确值，请不要依赖该类最大值的精确值。
 * <p>
 * User: huangsongli
 * Date: 16/10/28
 * Time: 下午1:26
 */
public class LRUCache implements Cache {

    private LRUUnit<SoftReference<ObjectWithExpire>>[] parts;

    private final int partsCount;

    /**
     * 创建一个lru缓存，构造函数的参数是缓存的大约大小。
     * 如果hash算法好，配备足够平均的话，meanlySize的误差比较小。
     *
     * @param meanlySize
     */
    public LRUCache(int meanlySize) {
        this.partsCount = computeParts(meanlySize);
        final int sizePerPart = meanlySize / partsCount;
        this.parts = new LRUUnit[partsCount];
        for (int i = 0; i < partsCount; ++i) {
            parts[i] = new LinkedHashMapLRUUnit<SoftReference<ObjectWithExpire>>(sizePerPart);
        }
    }

    private LRUUnit<SoftReference<ObjectWithExpire>> findLRUUnit(String key) {
        if (StringUtil.isEmpty(key)) {
            throw new IllegalArgumentException("not support empty key!");
        }
        int hashKey = Math.abs(key.hashCode());
        int index = hashKey % partsCount;
        return parts[index];
    }

    private static int computeParts(int meanlySize) {
        if (meanlySize < 10) {
            return 1;
        }
        if (meanlySize < 100) {
            return 2;
        }
        if (meanlySize < 1000) {
            return 3;
        }
        if (meanlySize < 10000) {
            return 4;
        }
        if (meanlySize < 100000) {
            return 8;
        }
        if (meanlySize < 1000000) {
            return 16;
        }
        return 20;
    }

    @Override
    public Object get(String key) {
        SoftReference<ObjectWithExpire> softReference = findLRUUnit(key).get(key);
        if (softReference == null) {
            return null;
        }
        ObjectWithExpire objectWithExpire = softReference.get();
        if (objectWithExpire == null) {
            return null;
        }
        if (objectWithExpire.isExpired()) {
            return null;
        }
        return objectWithExpire.getObject();
    }

    @Override
    public void set(String key, Object object, int expireInSeconds) {
        Date expireDate = DateUtil.addSecond(new Date(), expireInSeconds);
        ObjectWithExpire owe = new ObjectWithExpire(object, expireDate.getTime());
        findLRUUnit(key).put(key, new SoftReference<ObjectWithExpire>(owe));
    }

    @Override
    public void delete(String key) {
        findLRUUnit(key).remove(key);
    }

    @Override
    public void clearAll() {
        for (LRUUnit<SoftReference<ObjectWithExpire>> unit : parts) {
            unit.clear();
        }
    }
}
