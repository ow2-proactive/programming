package functionalTests.node.kill;

import java.io.IOException;
import java.rmi.AlreadyBoundException;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestNodeKill extends FunctionalTest {

    // PROACTIVE-563
    @Test
    public void test() throws NodeException, AlreadyBoundException, ActiveObjectCreationException,
            IOException {
        Node node = NodeFactory.createNode("my_node");
        PAActiveObject.newActive(Object.class.getName(), new Object[] {}, node);
        node.killAllActiveObjects();
        System.out.println("TestKillLocalNode.main() Node killed ");
    }

}
