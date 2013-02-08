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
package functionalTests.activeobject.futuremonitoring;

import functionalTests.GCMFunctionalTest;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.node.Node;

import static junit.framework.Assert.assertTrue;


/**
 * Test monitoring the futures
 */

public class TestFutureMonitoring extends GCMFunctionalTest {

    public TestFutureMonitoring() throws ProActiveException {
        super(4, 1);
        super.startDeployment();
    }

    @Test
    public void action() throws Exception {
        Node node1 = super.getANode();
        Node node2 = super.getANode();
        Node node3 = super.getANode();

        // With AC
        boolean exception = false;
        A a1 = PAActiveObject.newActive(A.class, null, node1);
        A future = a1.sleepForever();
        A a2 = PAActiveObject.newActive(A.class, null, node2);
        A ac = a2.wrapFuture(future);
        a2.crash();
        try {
            //System.out.println(ac);
            ac.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            fmpfe.printStackTrace();
            exception = true;
        }
        assertTrue(exception);

        // With AC and Terminate AO
        boolean exceptionT = false;
        A a1T = PAActiveObject.newActive(A.class, null, node1);
        A futureT = a1T.sleepForever();
        A a2T = PAActiveObject.newActive(A.class, null, node3);
        A acT = a2T.wrapFuture(futureT);
        a2T.crashWithTerminate();
        try {
            //System.out.println(ac);
            acT.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            fmpfe.printStackTrace();
            exceptionT = true;
        }
        assertTrue(exceptionT);

        // Without AC
        exception = false;
        A a1bis = PAActiveObject.newActive(A.class, null, node1);
        a1bis.crash();
        try {
            //System.out.println(future);
            future.toString();
        } catch (FutureMonitoringPingFailureException fmpfe) {
            fmpfe.printStackTrace();
            exception = true;
        }
        assertTrue(exception);
    }
}
