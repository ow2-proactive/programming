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
