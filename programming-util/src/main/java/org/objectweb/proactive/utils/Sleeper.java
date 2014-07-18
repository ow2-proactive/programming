/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.utils;

import org.apache.log4j.Logger;


/** A helper class to sleep a given amount of time in one line
 *
 * Calling Thread.sleep() requires a few lines of code to handle InterruptedException.
 * This code is duplicated everywhere (or missing). This helper should reduce the number 
 * of poorly handled InterruptedException and duplicated code.
 * 
 * If an InterruptionException is thrown while sleeping, it is logged (debug level)
 */
public class Sleeper {
    private long duration;

    /**
     * @param duration the amount of milliseconds to sleep. If 0, {@link #sleep()} returns immediately.
     * @param logger
     */
    public Sleeper(long duration, Logger logger) {
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
                e.printStackTrace();
            }
        }
    }
}
