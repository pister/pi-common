package com.github.pister.common.cache;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: huangsongli
 * Date: 16/10/28
 * Time: 下午1:42
 */
public class LRUCacheTest extends TestCase {

    public void test1() {
        LRUCache cache = new LRUCache(3000);
        for (int i = 0; i < 1000; ++i) {
            cache.set("aa" + i, "bb" + i, 10000);
        }
        System.out.println(cache.get("aa1"));
    }

    public void no_testPerform1() throws InterruptedException {
        LRUCache cache = new LRUCache(50000);
        final int threadCount = 10;
        final ExecutorService service = Executors.newFixedThreadPool(threadCount * 3);
        AtomicLong setCounter = new AtomicLong();
        AtomicLong getCounter = new AtomicLong();
        AtomicLong deleteCounter = new AtomicLong();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount * 3);
        PrintThread printThread = new PrintThread(getCounter, setCounter, deleteCounter);
        final int LOOP_COUNT = 10000000;
        for (int i = 0; i < threadCount; ++i) {
            {
                SetBenchThread benchThread = new SetBenchThread(cache, LOOP_COUNT, setCounter, countDownLatch);
                service.execute(benchThread);
            }{
                GetBenchThread benchThread = new GetBenchThread(cache, LOOP_COUNT, getCounter, countDownLatch);
                service.execute(benchThread);
            }{
                DeleteBenchThread benchThread = new DeleteBenchThread(cache, LOOP_COUNT, deleteCounter, countDownLatch);
                service.execute(benchThread);
            }
        }
        printThread.start();
        countDownLatch.await();
    }

    private static class PrintThread extends Thread {
        AtomicLong setCounter;
        AtomicLong getCounter;
        AtomicLong deleteCounter;

        public PrintThread(AtomicLong setCounter, AtomicLong getCounter, AtomicLong deleteCounter) {
            this.setCounter = setCounter;
            this.getCounter = getCounter;
            this.deleteCounter = deleteCounter;
        }

        long start = System.currentTimeMillis();

        private double qps(AtomicLong v, long delta) {
            if (v == null) {
                return 0;
            }
            return v.get() / (delta/1000.0);
        }

        public void run() {
            while(true) {
                try {
                    Thread.sleep(1000);
                    long now = System.currentTimeMillis();
                    long delta = now - start;
                    System.out.println("set:" + qps(setCounter, delta)
                            + "\t\tget:" +  qps(getCounter, delta)
                            + "\t\tdelete:" +  qps(deleteCounter, delta)
                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private abstract class BenchThread implements Runnable {

        protected LRUCache cache;

        private int loopCount;

        private AtomicLong counter;

        private CountDownLatch countDownLatch;

        public BenchThread(LRUCache cache, int loopCount, AtomicLong counter, CountDownLatch countDownLatch) {
            this.cache = cache;
            this.loopCount = loopCount;
            this.counter = counter;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            for (int i = 0; i < loopCount; i++) {
                work(i);
                counter.incrementAndGet();
            }
            countDownLatch.countDown();
        }

        protected abstract void work(int i);
    }

    protected class GetBenchThread extends BenchThread {

        public GetBenchThread(LRUCache cache, int loopCount, AtomicLong counter, CountDownLatch countDownLatch) {
            super(cache, loopCount, counter, countDownLatch);
        }

        @Override
        protected void work(int i) {
            cache.get("a-" + i);
        }
    }

    protected class SetBenchThread extends BenchThread {

        public SetBenchThread(LRUCache cache, int loopCount, AtomicLong counter, CountDownLatch countDownLatch) {
            super(cache, loopCount, counter, countDownLatch);
        }

        @Override
        protected void work(int i) {
            cache.set("a-" + i, "b-" + i, 10000);
        }
    }

    protected class DeleteBenchThread extends BenchThread {

        public DeleteBenchThread(LRUCache cache, int loopCount, AtomicLong counter, CountDownLatch countDownLatch) {
            super(cache, loopCount, counter, countDownLatch);
        }

        @Override
        protected void work(int i) {
            cache.delete("a-" + i);
        }
    }


}