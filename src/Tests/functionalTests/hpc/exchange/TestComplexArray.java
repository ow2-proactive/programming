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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.hpc.exchange;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;

import functionalTests.GCMFunctionalTest;


public class TestComplexArray extends GCMFunctionalTest {
    private B b1, b2, b3;

    public TestComplexArray() throws ProActiveException {
        super(2, 2);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        b1 = PAActiveObject.newActive(B.class, new Object[] {}, super.getANode());
        b2 = PAActiveObject.newActive(B.class, new Object[] {}, super.getANode());
        b3 = PAActiveObject.newActive(B.class, new Object[] {}, super.getANode());

        b1.start(1, b1, b2, b3);
        b2.start(2, b1, b2, b3);
        b3.start(3, b1, b2, b3);
    }

    @org.junit.After
    public void after() throws Exception {
        double cs_b1_1 = b1.getChecksum1();
        double cs_b2_1 = b2.getChecksum1();

        double cs_b2_2 = b2.getChecksum2();
        double cs_b3_2 = b3.getChecksum2();

        assertTrue(cs_b1_1 == cs_b2_1);
        assertTrue(cs_b2_2 == cs_b3_2);
    }
}
