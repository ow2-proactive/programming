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
package functionalTests.node.equals;

import java.rmi.AlreadyBoundException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.VMInformation;

import functionalTests.FunctionalTest;


public class TestEquals extends FunctionalTest {

    @Test
    public void test() throws NodeException, AlreadyBoundException {
        Node node1 = NodeFactory.createLocalNode("node1", false, null);
        Node node2 = NodeFactory.createLocalNode("node2", false, null);

        VMInformation localVM1 = node1.getVMInformation();
        VMInformation localVM2 = node2.getVMInformation();

        Assert.assertEquals(localVM1, localVM2);

        NodeInformation nodeInformation1 = node1.getNodeInformation();
        NodeInformation nodeInformation2 = node2.getNodeInformation();

        logger.info("NodeInformation");
        Assert.assertEquals(nodeInformation1, nodeInformation1);
        Assert.assertEquals(nodeInformation2, nodeInformation2);
        Assert.assertFalse(nodeInformation1.equals(nodeInformation2));
        Assert.assertFalse(nodeInformation2.equals(nodeInformation1));

        logger.info("Node self equals");
        Assert.assertEquals(node1, node1);
        Assert.assertEquals(node2, node2);

        logger.info("Nodes not equals");
        Assert.assertFalse(node1.equals(node2));
        Assert.assertFalse(node2.equals(node1));
    }

}
