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
package functionalTests.gcmdeployment.virtualnode;

import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;


public class TestVirtualNodeSubscribeWithHistory extends GCMFunctionalTest {

    int counter = 0;

    Semaphore sem = new Semaphore(4);

    public TestVirtualNodeSubscribeWithHistory() throws ProActiveException {
        super(2, 2);
        super.startDeployment();
    }

    @Test
    public void test() throws InterruptedException, ProActiveException {
        // Block until a node register, so history will be used at least for one node
        super.getANode();

        GCMVirtualNode vn = super.gcmad.getVirtualNode(super.DEFAULT_VN_NAME);
        Assert.assertNotNull(vn);
        vn.subscribeNodeAttachment(this, "callback", true);

        // Test passed ! (callback has been invoked deployment.size times)
    }

    public void callback(Node node, String vn) {
        sem.release();
    }
}
