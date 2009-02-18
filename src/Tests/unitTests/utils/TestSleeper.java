package unitTests.utils;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.Sleeper;

import unitTests.UnitTests;


public class TestSleeper extends UnitTests {
    static final long SLEEP_TIME = 1000;

    @Test
    public void test() {
        Sleeper sleeper = new Sleeper(SLEEP_TIME);

        T t = new T(Thread.currentThread());
        Thread thread = new Thread(t);
        thread.setDaemon(true);
        thread.start();

        long before = System.currentTimeMillis();
        sleeper.sleep();
        long after = System.currentTimeMillis();

        logger.info("Spleeped " + (after - before) + " expected " + SLEEP_TIME);
        // -1 is here because System.nanoTime() is more accurate
        // than System.currentTimeMillis(). Rouding errors can leads to
        // after - before == SLEEP_TIME - 1
        Assert.assertTrue(after - before >= SLEEP_TIME - 1);
    }

    private class T implements Runnable {
        private Thread sleeper;

        public T(Thread sleeper) {
            this.sleeper = sleeper;
        }

        public void run() {
            while (true) {
                this.sleeper.interrupt();
            }
        }

    }
}
