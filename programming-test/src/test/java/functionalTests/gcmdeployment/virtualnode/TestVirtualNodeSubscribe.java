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

import java.io.FileNotFoundException;
import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNodeSubscribe extends GCMFunctionalTest {
    static GCMApplication gcma;

    GCMVirtualNode vnGreedy;

    GCMVirtualNode vnMaster;

    Semaphore semIsReady = new Semaphore(0);

    Semaphore semNodeAttached = new Semaphore(-1);

    boolean isReady = false;

    long nodes = 0;

    public TestVirtualNodeSubscribe() throws FileNotFoundException, ProActiveException {
        super(LocalHelpers.getDescriptor(TestVirtualNodeSubscribe.class));
        super.startDeployment();
        gcma = super.gcmad;
    }

    @Before
    public void before() throws ProActiveException, FileNotFoundException {
        vnGreedy = gcma.getVirtualNode("greedy");
        vnMaster = gcma.getVirtualNode("master");
    }

    @Test(expected = ProActiveException.class)
    public void testIsReadyWithGreedyVN() throws ProActiveException {
        vnGreedy.subscribeIsReady(this, "isReady");
    }

    @Test(expected = ProActiveException.class)
    public void testNoSuchMethod() throws ProActiveException {
        vnMaster.subscribeNodeAttachment(this, "LOL", false);
    }

    @Test(expected = ProActiveException.class)
    public void testInvalidSignature() throws ProActiveException {
        vnMaster.subscribeNodeAttachment(this, "brokenNodeAttached", false);
    }

    @Test(expected = ProActiveException.class)
    public void testInvalidSignatureIsReady() throws ProActiveException {
        vnMaster.subscribeNodeAttachment(this, "brokenIsReady", false);
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt1() throws ProActiveException {
        vnMaster.subscribeIsReady(this, "null");
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt2() throws ProActiveException {
        vnMaster.subscribeIsReady(null, null);
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt3() throws ProActiveException {
        vnMaster.unsubscribeIsReady(this, "null");
    }

    @Test(expected = ProActiveException.class)
    public void testCrashIt4() throws ProActiveException {
        vnMaster.unsubscribeIsReady(null, null);
    }

    @Test
    public void test() throws FileNotFoundException, ProActiveException, InterruptedException {
        // Failure <=> Timeout

        vnMaster.subscribeNodeAttachment(this, "nodeAttached", false);
        vnMaster.subscribeIsReady(this, "isReady");

        gcma.startDeployment();
        gcma.waitReady();

        // wait for the notification

        semIsReady.acquire();
        semNodeAttached.acquire();

        vnMaster.unsubscribeNodeAttachment(this, "nodeAttached");
    }

    public void isReady(String vnName) {
        Assert.assertNotNull(gcma.getVirtualNode(vnName));
        semIsReady.release();
    }

    public void nodeAttached(Node node, String vnName) {
        semNodeAttached.release();
    }

    public void brokenIsReady(long l) {
    }

    public void brokenNodeAttached(Object o) {
    }
}
