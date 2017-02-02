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
package functionalTests.activeobject.futuremonitoring;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.node.Node;

import functionalTests.GCMFunctionalTest;


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
