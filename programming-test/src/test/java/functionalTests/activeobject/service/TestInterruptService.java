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
package functionalTests.activeobject.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * TestInterruptService this test checks the service interruption functionality.
 * It verifies that a request can be interrupted while it's being served and that the AO is still alive after this interruption
 *
 * @author The ProActive Team
 **/
public class TestInterruptService {

    public static class AOInterrupt {

        public AOInterrupt() {

        }

        public BooleanWrapper toInterrupt() {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new BooleanWrapper(true);
        }
    }

    @Test
    public void test() throws Throwable {
        AOInterrupt ao = PAActiveObject.newActive(AOInterrupt.class, new Object[0]);
        BooleanWrapper bw = ao.toInterrupt();
        Thread.sleep(500);
        PAActiveObject.interruptService(ao);
        boolean exceptionReceived = false;
        try {
            boolean value = bw.getBooleanValue();
        } catch (RuntimeException e) {
            e.printStackTrace();
            assertTrue(e.getCause() instanceof InterruptedException);
            exceptionReceived = true;
        }
        assertTrue(exceptionReceived);

        // verify that the AO is still alive
        PAActiveObject.pingActiveObject(ao);

        // resend interruption
        PAActiveObject.interruptService(ao);

        // verify again that the AO is still alive
        PAActiveObject.pingActiveObject(ao);
    }
}
