package functionalTests.node.equals;

import java.rmi.AlreadyBoundException;

import junit.framework.Assert;

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
        Node node1 = NodeFactory.createNode("///node1");
        Node node2 = NodeFactory.createNode("///node2");

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
