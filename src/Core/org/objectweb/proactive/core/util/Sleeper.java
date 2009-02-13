package org.objectweb.proactive.core.util;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** A helper class to sleep a given amount of time in one line
 *
 * Calling Thread.sleep() requires a few lines of code to handle InterruptedException.
 * This code is duplicated everywhere (or missing). This helper should reduce the number 
 * of poorly handled InterruptedException and duplicated code.
 * 
 * If an InterruptionException is thrown while sleeping, it is logged (debug level)
 */
public class Sleeper {
    static final Logger logger = ProActiveLogger.getLogger(Loggers.SLEEPER);
    private long duration;

    /**
     * @param duration the amount of milliseconds to sleep. If 0, {@link #sleep()} returns immediately.
     */
    public Sleeper(long duration) {
        this.duration = duration;
    }

    /** Sleep the predefined amount of time.
     * 
     * It is safe to call this method several times and from different threads.
     */
    public void sleep() {
        if (this.duration == 0) {
            // Avoid to sleep forever
            return;
        }

        TimeoutAccounter timeoutAccounter = TimeoutAccounter.getAccounter(this.duration);
        while (!timeoutAccounter.isTimeoutElapsed()) {
            try {
                Thread.sleep(timeoutAccounter.getRemainingTimeout());
            } catch (InterruptedException e) {
                ProActiveLogger.logEatedException(logger, e);
            }
        }
    }
}
