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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


/** A helper class for CountDownLatch
 * 
 * CountDownLatch can throw an InterruptedException. Usually we are just waiting until
 * the latch has counted down to zero. This helper provide a default try/catch around the
 * wait() method.
 * 
 */
public class SweetCountDownLatch extends CountDownLatch {

    private Logger logger;

    public SweetCountDownLatch(int count, Logger logger) {
        super(count);
        this.logger = logger;
    }

    @Override
    public void await() {
        boolean wait = true;
        while (wait) {
            try {
                super.await();
                wait = false;
            } catch (InterruptedException e) {
                logger.debug("Interrupted in SweetCountDownLatch", e);
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
