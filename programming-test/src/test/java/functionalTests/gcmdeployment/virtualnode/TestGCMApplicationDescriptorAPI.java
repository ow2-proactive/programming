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
package functionalTests.gcmdeployment.virtualnode;

import java.io.FileNotFoundException;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.gcmdeployment.core.TopologyImpl;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;
import org.objectweb.proactive.utils.Sleeper;
import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;
import org.junit.Assert;
import org.junit.Test;


public class TestGCMApplicationDescriptorAPI extends GCMFunctionalTest {
    public TestGCMApplicationDescriptorAPI() throws ProActiveException, FileNotFoundException {
        super(LocalHelpers.getDescriptor(TestGCMApplicationDescriptorAPI.class));
        super.startDeployment();
    }

    @Test
    public void test() throws ProActiveException, FileNotFoundException {
        super.gcmad = PAGCMDeployment.loadApplicationDescriptor(super.applicationDescriptor, super
                .getFinalVariableContract());
        Assert.assertFalse(super.gcmad.isStarted());
        Assert.assertEquals(2, super.gcmad.getVirtualNodes().size());

        super.gcmad.startDeployment();

        Assert.assertTrue(super.gcmad.isStarted());
        Assert.assertEquals(2, super.gcmad.getVirtualNodes().size());

        GCMVirtualNode vn1 = super.gcmad.getVirtualNode("vn1");
        Assert.assertNotNull(vn1);
        while (vn1.getNbCurrentNodes() != 5) {
            new Sleeper(500, ProActiveLogger.getLogger(Loggers.SLEEPER)).sleep();
        }
        List<Node> nodes = vn1.getCurrentNodes();

        // Check reachable
        for (Node node : nodes) {
            node.getActiveObjects();
        }

        super.gcmad.kill();

        // Check unreachable
        for (Node node : nodes) {
            boolean exception = false;
            try {
                node.getActiveObjects();
            } catch (Throwable e) {
                exception = true;
            }
            Assert.assertTrue(exception);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionGetAllNode() {
        super.gcmad.getAllNodes();
    }

    @Test(expected = ProActiveException.class)
    public void testExceptionGetTopology() throws ProActiveException {
        super.gcmad.getTopology();
    }

    @Test(expected = ProActiveException.class)
    public void testExceptionUpdateTopology() throws ProActiveException {
        Topology t = new TopologyImpl();
        super.gcmad.updateTopology(t);
    }

    @Test
    public void testGetVirtualNode() {
        GCMVirtualNode vn = super.gcmad.getVirtualNode("IDontExist");
        Assert.assertNull(vn);
    }

}
