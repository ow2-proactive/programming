package functionalTests.activeobject;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGetUrl extends GCMFunctionalTestDefaultNodes {

    public TestGetUrl() {
        super(1, 1);
    }

    @Test(expected = ProActiveRuntimeException.class)
    public void testNonAO() {
        Object o = new Object();
        String url = PAActiveObject.getUrl(o);
    }

    @Test
    public void testPAAactiveObject() throws ActiveObjectCreationException, NodeException, IOException {
        Node node = super.getANode();
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {}, node);
        String url = PAActiveObject.getUrl(ao);
        ao = (AO) PAActiveObject.lookupActive(AO.class.getName(), url);
        ao.v();
    }

    public static class AO {
        public AO() {

        }

        public boolean v() {
            return true;
        }

    }

}
