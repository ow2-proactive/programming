/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.node.nodefactory;

import java.net.URI;
import java.rmi.AlreadyBoundException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.FunctionalTest;


@SuppressWarnings("deprecation")
public class TestDeprecated extends FunctionalTest {

    /* Test deprecated methods of NodeFactory
     */

    @Test
    public void test1() throws NodeException, AlreadyBoundException {
        Node node;
        node = NodeFactory.createNode("nodeName1");
        Node gNode = NodeFactory.getNode(node.getNodeInformation().getURL());
        Assert.assertEquals(node, gNode);
    }

    @Test
    public void testLocalhost() throws NodeException, AlreadyBoundException {
        Node node;
        node = NodeFactory.createNode("//localhost/nodeName2");
        Node gNode = NodeFactory.getNode(node.getNodeInformation().getURL());
        Assert.assertEquals(node, gNode);
    }

    @Test(expected = NodeException.class)
    public void testSlash() throws NodeException, AlreadyBoundException {
        NodeFactory.createNode("//wrong");
    }

    @Test
    public void testRemote() throws NodeException, AlreadyBoundException {
        Node node;
        URI uri = URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), "nodeName3");
        node = NodeFactory.createNode(uri.toString());
        Node gNode = NodeFactory.getNode(node.getNodeInformation().getURL());
        Assert.assertEquals(node, gNode);

    }

}
