package org.objectweb.proactive.core.node;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class NodeImplTest {

    @Before
    public void disableRMISecurityManager() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
    }

    // The bug actually lies in ProActiveRuntimeImpl but is visible when trying to kill AOs on a node
    @Test
    public void allActiveObjectsAreKilled() throws Exception {
        Node localNode = NodeFactory.createLocalNode("allActiveObjectsAreKilled", false,
                "allActiveObjectsAreKilled");

        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        localNode.killAllActiveObjects();

        assertEquals(0, localNode.getNumberOfActiveObjects());

        // second kill reveals the issue
        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        localNode.killAllActiveObjects();

        assertEquals(0, localNode.getNumberOfActiveObjects());
    }

    @Test
    public void testThreadDump() throws Exception {
        Node localNode = NodeFactory.createLocalNode("testThreadDump", false, "testThreadDump");

        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);

        String threadDump = localNode.getThreadDump();

        System.out.println(threadDump);

        assertTrue(threadDump.contains(SimpleActiveObject.class.getSimpleName()));
        localNode.killAllActiveObjects();
    }

    public static class SimpleActiveObject {
    }
}