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
package gcmdeployment.virtualnode;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;
import functionalTests.FunctionalTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestGetANodeMultithread extends FunctionalTest {
    static final int TIMEOUT = 1000;
    static final int CLIENTS = 10;
    GCMVirtualNodeImpl vn;
    GCMApplicationDescriptorMockup gcma;
    ProActiveRuntime part;

    @BeforeClass
    static public void setCapacity() {
        ProActiveRuntimeImpl.getProActiveRuntime().setCapacity(12000);
    }

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
        gcma = new GCMApplicationDescriptorMockup();
        part = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Test
    public void multithreadSimple() throws InterruptedException {
        final int nodes = 10000;
        for (int i = 0; i < nodes; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Client[] clients = new Client[CLIENTS];

        for (int client = 0; client < clients.length; client++) {
            clients[client] = new Client(vn);
        }

        for (int client = 0; client < clients.length; client++) {
            clients[client].start();
        }

        for (int client = 0; client < clients.length; client++) {
            clients[client].join();
        }

        int sum = 0;
        for (int client = 0; client < clients.length; client++) {
            sum += clients[client].counter;
        }

        Assert.assertEquals(nodes, sum);
    }

    class Client extends Thread {
        GCMVirtualNodeImpl vn;
        public int counter;

        public Client(GCMVirtualNodeImpl vn) {
            this.vn = vn;
            counter = -1;
        }

        public void run() {
            Node node;

            do {
                node = vn.getANode(TIMEOUT);
                counter++;
            } while (node != null);
        }
    }

    static void checkGetANodeIsNull(GCMVirtualNodeImpl vn) {
        long before = System.currentTimeMillis();
        Node rNode = vn.getANode(TIMEOUT);
        long after = System.currentTimeMillis();
        long timeElapsed = after - before;

        Assert.assertFalse("Timeout too short", timeoutTooShort(TIMEOUT, timeElapsed));
        Assert.assertFalse("Timeout too long", timeoutTooLong(TIMEOUT, timeElapsed));
        Assert.assertNull(rNode);
    }

    void checkGetANodeIsNotNull(GCMVirtualNodeImpl vn, int i) {
        Node rNode = vn.getANode(TIMEOUT);
        String nodeName = part.getVMInformation().getName() + "_" + Constants.GCM_NODE_NAME + i;

        Assert.assertNotNull(rNode);
        Assert.assertEquals(nodeName, rNode.getNodeInformation().getName());
    }

    static boolean timeoutTooShort(long timeout, long timeElapsed) {
        if (timeElapsed < timeout)
            return true;

        return false;
    }

    static boolean timeoutTooLong(long timeout, long timeElapsed) {
        if (timeElapsed > 2 * timeout)
            return true;

        return false;
    }
}
