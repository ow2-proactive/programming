package functionalTests.node.nodefactory;

import java.rmi.AlreadyBoundException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestKillNode extends FunctionalTest {
    /* PROACTIVE-573 reported that killNode was buggy */
    @Test
    public void test() throws NodeException, AlreadyBoundException {
        Node node = NodeFactory.createNode("PROACTIVE-573");
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
