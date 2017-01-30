/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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

    private Logger logger;

    /**
     * @param duration the amount of milliseconds to sleep. If 0, {@link #sleep()} returns immediately.
     * @param logger
     */
    public Sleeper(long duration, Logger logger) {
        this.duration = duration;
        this.logger = logger;
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
                logger.debug("Interrupted while sleeping", e);
            }
        }
    }
}
