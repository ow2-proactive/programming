package org.objectweb.proactive.core.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** A helper class for CountDownLatch
 * 
 * CountDownLatch can throw an InterruptedException. Usually we are just waiting until
 * the latch has counted down to zero. This helper provide a default try/catch around the
 * wait() method.
 * 
 */
public class SweetCountDownLatch extends CountDownLatch {
    static final private Logger logger = ProActiveLogger.getLogger(Loggers.WAITER);

    public SweetCountDownLatch(int count) {
        super(count);
    }

    @Override
    public void await() {
        boolean wait = true;
        while (wait == true) {
            try {
                super.await();
                wait = false;
            } catch (InterruptedException e) {
                // Miam Miam Miam
                ProActiveLogger.logEatedException(logger, e);
            }
        }
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {

        boolean b = false;
        boolean anotherLoop;
        do {
            try {
                anotherLoop = false;
                b = super.await(timeout, unit);
            } catch (InterruptedException e) {
                // Miam miam miam, don't care we are looping
                anotherLoop = true;
            }
        } while (anotherLoop);

        return b;
    }

}
