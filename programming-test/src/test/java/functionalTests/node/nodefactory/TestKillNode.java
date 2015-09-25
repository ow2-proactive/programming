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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.node.nodefactory;

import java.rmi.AlreadyBoundException;

import org.junit.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestKillNode extends FunctionalTest {
    /* PROACTIVE-573 reported that killNode was buggy */
    @Test
    public void test() throws NodeException, AlreadyBoundException {
        Node node = NodeFactory.createLocalNode("PROACTIVE-573", false, null);
        node = NodeFactory.getNode(node.getNodeInformation().getURL());
        Assert.assertNotNull(node);
        NodeFactory.killNode(node.getNodeInformation().getURL());
        try {
            node = NodeFactory.getNode(node.getNodeInformation().getURL());
            Assert.fail("The previous line must throw a NodeException");
        } catch (NodeException e) {
            logger.info("Exception catched, everything is fine");
        }
    }
}
