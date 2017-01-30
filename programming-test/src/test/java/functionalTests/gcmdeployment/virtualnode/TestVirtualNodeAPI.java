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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNodeAPI extends GCMFunctionalTest {
    public TestVirtualNodeAPI() throws FileNotFoundException, ProActiveException {
        super(LocalHelpers.getDescriptor(TestVirtualNodeAPI.class));
        super.startDeployment();
    }

    @Test
    public void metaTest() throws InterruptedException {
        /*
         * Since this testsuite was one of the slowest, this metaTest has been added to avoid
         * unnecessary deployments (one deployment is performed for each @Test annotation)
         */
        testGetName();
        testIsGreedy();
        testIsReady();
        testGetNbRequiredNodes();
        testGetNbCurrentNodes();
        testGetCurrentNodes();
        testGetNewNodes();
    }

    //    @Test
    public void testGetName() {
        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");
        GCMVirtualNode vn3 = gcmad.getVirtualNode("vn3");

        Assert.assertEquals("vn1", vn1.getName());
        Assert.assertEquals("vn2", vn2.getName());
        Assert.assertEquals("vn3", vn3.getName());
    }

    //    @Test
    public void testIsGreedy() {
        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");
        GCMVirtualNode vn3 = gcmad.getVirtualNode("vn3");

        Assert.assertNull(gcmad.getVirtualNode("IDontExist"));
        Assert.assertTrue(vn1.isGreedy());
        Assert.assertTrue(vn3.isGreedy());
        Assert.assertFalse(vn2.isGreedy());
    }

    //    @Test
    public void testIsReady() {
        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");
        GCMVirtualNode vn3 = gcmad.getVirtualNode("vn3");

        vn1.waitReady();
        vn2.waitReady();

        Assert.assertTrue(vn1.isReady());
        Assert.assertTrue(vn2.isReady());
        Assert.assertFalse(vn3.isReady());
    }

    //    @Test
    public void testGetNbRequiredNodes() {
        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");
        GCMVirtualNode vn3 = gcmad.getVirtualNode("vn3");
        GCMVirtualNode vn4 = gcmad.getVirtualNode("vn4");
        GCMVirtualNode vn5 = gcmad.getVirtualNode("vn5");

        System.out.println("Plop");

        Assert.assertEquals(0, vn1.getNbRequiredNodes());
        Assert.assertEquals(1, vn2.getNbRequiredNodes());
        Assert.assertEquals(2, vn3.getNbRequiredNodes());
        Assert.assertEquals(2, vn4.getNbRequiredNodes());
        Assert.assertEquals(3, vn5.getNbRequiredNodes());
    }

    //    @Test
    public void testGetNbCurrentNodes() throws InterruptedException {
        // failure = timeout reached

        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");
        GCMVirtualNode vn3 = gcmad.getVirtualNode("vn3");
        GCMVirtualNode vn4 = gcmad.getVirtualNode("vn4");
        GCMVirtualNode vn5 = gcmad.getVirtualNode("vn5");

        while (true) {
            if (1 == vn2.getCurrentNodes().size() && 1 == vn3.getCurrentNodes().size() &&
                1 == vn4.getCurrentNodes().size() && 2 == vn5.getCurrentNodes().size())
                return; // test passed

            Thread.sleep(1000);
        }
    }

    //    @Test
    public void testGetCurrentNodes() throws InterruptedException {
        GCMVirtualNode vn5 = gcmad.getVirtualNode("vn5");

        while (vn5.getCurrentNodes().isEmpty()) {
            Thread.sleep(1000);
        }
        Thread.sleep(1000);

        // Check isolation
        List<Node> vn5Nodes = vn5.getCurrentNodes();
        vn5Nodes.remove(vn5Nodes.iterator().next());
        Assert.assertTrue(vn5.getCurrentNodes().size() == ((vn5Nodes.size()) + 1));
    }

    //    @Test
    public void testGetNewNodes() throws InterruptedException {
        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");

        // Wait for allocation
        vn1.getANode();
        Thread.sleep(1000);

        // Check isolation
        List<Node> vn1Nodes = vn1.getCurrentNodes();
        List<Node> set1 = vn1.getNewNodes();
        Assert.assertTrue(set1.containsAll(vn1Nodes) && (set1.size() == vn1Nodes.size()));
        Assert.assertTrue(vn1.getNewNodes().size() == 0);

        // TODO register manually some Node and check the returned set again
    }
}
