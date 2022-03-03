package com.github.pister.common.cache;

import com.github.pister.common.lang.util.MapUtil;
import com.github.pister.common.cache.help.ObjectWithExpire;
import com.github.pister.common.lang.util.DateUtil;

import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.Map;

/**
 * User: huangsongli
 * Date: 16/4/5
 * Time: 上午11:10
 */
public class JvmCache implements Cache {

    private Map<String, SoftReference<ObjectWithExpire>> cacheHolder = MapUtil.newConcurrentHashMap();

    @Override
    public Object get(String key) {
        SoftReference<ObjectWithExpire> softReference = cacheHolder.get(key);
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
        cacheHolder.put(key, new SoftReference<ObjectWithExpire>(owe));
    }

    @Override
    public void delete(String key) {
        cacheHolder.remove(key);
    }

    @Override
    public void clearAll() {
        cacheHolder.clear();
    }
}