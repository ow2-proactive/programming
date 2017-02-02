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
package functionalTests.node.lookup;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestNodeLookup extends FunctionalTest {

    @Test(expected = NodeException.class)
    public void testRemoteException() throws NodeException {
        NodeFactory.getNode("rmi://chuck.norris:1099/gun");
    }

    @Test(expected = NodeException.class)
    public void testNotBoundException() throws NodeException {
        NodeFactory.getNode("rmi://localhost:1099/gun");
    }

    @Test
    public void test() throws NodeException {
        Node node = NodeFactory.getDefaultNode();
        String url = node.getNodeInformation().getURL();

        NodeFactory.getNode(url);
    }
}
