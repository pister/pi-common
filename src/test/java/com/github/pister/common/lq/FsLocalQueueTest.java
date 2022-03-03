package com.github.pister.common.lq;

import com.github.pister.common.lang.util.SystemUtil;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by songlihuang on 2021/7/9.
 */
public class FsLocalQueueTest extends TestCase {

    private FsLocalQueue fsLocalQueue = new FsLocalQueue(new File(SystemUtil.USER_HOME + "/temp/fq_test1"));


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fsLocalQueue.init();
    }

    public void testAddLast() throws IOException {
        fsLocalQueue.addLast("hello".getBytes("utf-8"));
        fsLocalQueue.addLast("world".getBytes("utf-8"));
        fsLocalQueue.addLast("你好中国".getBytes("utf-8"));
    }

    public void testRemoveLast() throws InterruptedException, UnsupportedEncodingException {
        for (; ; ) {
            byte[] data = fsLocalQueue.removeFirst(0);
            if (data == null) {
                break;
            }
            System.out.println(new String(data, "utf-8"));
        }
    }

}