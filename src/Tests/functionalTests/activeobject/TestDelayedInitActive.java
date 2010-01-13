/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.activeobject;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.Sleeper;

import functionalTests.FunctionalTest;


/**
 * Test case for PROACTIVE-652
 */
public class TestDelayedInitActive extends FunctionalTest {
    static final long SLEEP = 200;
    static final long EPSYLON = (long) (SLEEP / 10);

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        AO ao = PAActiveObject.newActive(AO.class, new Object[] {});
        long before = System.currentTimeMillis();
        /* Race condition: PAActiveObject.setIS() has not yet been called when ao.is() is served
         * The request is put into the request queue instead of being served as an IS -> asynchronous call 
         */
        ao.is();
        long after = System.currentTimeMillis();
        Assert.assertTrue("Method call seems to be async but should be sync (immediate service)", after -
            before >= SLEEP);
    }

    static public class AO implements Serializable, InitActive {
        /**
         * 
         */
        private static final long serialVersionUID = 420L;

        public void initActivity(Body body) {
            // Enlarge the race condition window
            raceConditionHelper();

            PAActiveObject.setImmediateService("is");
        }

        private void raceConditionHelper() {
            new Sleeper(100).sleep();
        }

        public void is() {
            new Sleeper(SLEEP).sleep();
        }
    }
}
