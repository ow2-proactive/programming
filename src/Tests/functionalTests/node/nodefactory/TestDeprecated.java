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
