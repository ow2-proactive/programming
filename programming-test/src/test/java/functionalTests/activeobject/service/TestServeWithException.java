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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.FunctionalTest;


/**
 * TestServeWithException
 *
 * @author The ProActive Team
 */
public class TestServeWithException extends FunctionalTest {

    @Test
    public void test() throws Throwable {
        AO ao = PAActiveObject.newActive(AO.class, new Object[] {});
        IntWrapper bw = ao.foo();

        try {
            PAFuture.waitFor(bw);
            assertFalse("This should not occur, received value " + bw, true);
        } catch (Throwable t) {
            t.printStackTrace();
            assertTrue("t is a CustomException", t instanceof CustomException);
        }
        try {
            IntWrapper bw2 = ao.foo2();

            PAFuture.waitFor(bw2);
            assertFalse("This should not occur, received value " + bw2, true);
        } catch (Throwable t2) {
            t2.printStackTrace();
            assertTrue("t2 is a CustomException2", t2 instanceof CustomException2);
        }

        try {
            IntWrapper bw3 = ao.foo3();

            PAFuture.waitFor(bw3);
            assertFalse("This should not occur, received value " + bw3, true);
        } catch (Throwable t3) {
            t3.printStackTrace();
            assertTrue("t3 is an IllegalArgumentException", t3 instanceof IllegalArgumentException);
        }
    }

}
