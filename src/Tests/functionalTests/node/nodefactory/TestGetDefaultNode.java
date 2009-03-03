package functionalTests.node.nodefactory;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGetDefaultNode extends GCMFunctionalTestDefaultNodes {

    /* Checks that two runtime don't have the same default node URI.
     *
     */
    public TestGetDefaultNode() {
        super(1, 1);
    }

    @Test
    public void test() throws NodeException, ActiveObjectCreationException {
        Node dnode0;
        Node dnode1;

        dnode0 = NodeFactory.getDefaultNode();

        Node node = super.getANode();
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {}, node);
        dnode1 = ao.getDefaultNode();

        String url0 = dnode0.getNodeInformation().getURL();
        String url1 = dnode1.getNodeInformation().getURL();
        Assert.assertFalse(url0.equals(url1));
    }

    @SuppressWarnings("serial")
    static public class AO implements Serializable {

        public AO() {
        }

        public Node getDefaultNode() throws NodeException {
            return NodeFactory.getDefaultNode();
        }
    }
}
