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
