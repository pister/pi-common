package com.github.pister.common.lq;

/**
 *
 * 本地持久化的queue
 *
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午9:37
 */
public interface LocalQueue {

    /**
     * 向队列尾部放入数据
     * @param data
     */
    void addLast(byte[] data);

    /**
     * 从队列头部获取并移除数据
     * @param waitTimeMillis 如果队列为空，最多等待的毫秒数
     * @return 返回队列头部数据，如果没有获取到则返回 null
     * @throws InterruptedException
     */
    byte[] removeFirst(long waitTimeMillis) throws InterruptedException;


}
