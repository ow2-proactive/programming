package functionalTests.node.nodefactory;

import java.rmi.AlreadyBoundException;

import org.junit.Test;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestErrorHandling extends FunctionalTest {

    @Test(expected = NodeException.class)
    public void test() throws NodeException, AlreadyBoundException {
        NodeFactory.createNode("//wrong");
    }
}
