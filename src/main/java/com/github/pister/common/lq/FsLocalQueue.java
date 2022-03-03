package com.github.pister.common.lq;

import java.io.File;
import java.io.IOException;

/**
 * 本地文件队列
 *
 * User: huangsongli
 * Date: 16/12/21
 * Time: 上午9:39
 */
public class FsLocalQueue implements LocalQueue {

    private File basePath;

    /**
     * 创建一个本地文件队列
     * @param basePath 持久化的文件路径
     */
    public FsLocalQueue(File basePath) {
        this.basePath = basePath;
    }

    private ReadWriteDataManager readWriteDataManager;

    /**
     * 初始化，构造函数完成后务必调用此方法完成队列的初始化
     * @throws IOException
     */
    public void init() throws IOException {
        readWriteDataManager = new ReadWriteDataManager(basePath);
        readWriteDataManager.init();
    }

    @Override
    public void addLast(byte[] data) {
        try {
            readWriteDataManager.writeData(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public byte[] removeFirst(long waitTimeMillis) throws InterruptedException {
        try {
            return readWriteDataManager.readData(waitTimeMillis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
