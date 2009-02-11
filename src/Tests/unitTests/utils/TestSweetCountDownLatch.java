package unitTests.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.Sleeper;
import org.objectweb.proactive.core.util.SweetCountDownLatch;

import unitTests.UnitTests;


public class TestSweetCountDownLatch extends UnitTests {

    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new SweetCountDownLatch(1);

        T t = new T(latch, Thread.currentThread(), 0);
        new Thread(t).start();
        latch.await();

        Assert.assertEquals(0, latch.getCount());
    }

    @Test
    public void testTimeoutExpired() {
        SweetCountDownLatch latch = new SweetCountDownLatch(1);

        T t = new T(latch, Thread.currentThread(), 10000);
        new Thread(t).start();
        boolean b = latch.await(50, TimeUnit.MILLISECONDS);

        Assert.assertFalse(b);
    }

    @Test
    public void testTimeout() {
        SweetCountDownLatch latch = new SweetCountDownLatch(1);

        T t = new T(latch, Thread.currentThread(), 0);
        new Thread(t).start();
        boolean b = latch.await(10000, TimeUnit.MILLISECONDS);

        Assert.assertTrue(b);
    }

    class T implements Runnable {
        private CountDownLatch latch;
        private Thread waiter;
        private long sleepms;

        public T(CountDownLatch latch, Thread waiter, long sleepms) {
            this.latch = latch;
            this.waiter = waiter;
            this.sleepms = sleepms;
        }

        public void run() {
            new Sleeper(this.sleepms).sleep();

            this.waiter.interrupt();
            Thread.yield();
            this.latch.countDown();

        }
    }
}
