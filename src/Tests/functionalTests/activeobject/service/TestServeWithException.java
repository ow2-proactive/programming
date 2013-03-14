/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.service;

import functionalTests.FunctionalTest;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


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
