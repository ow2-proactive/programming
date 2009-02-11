package functionalTests.remoteobject.turnremote;

import java.io.Serializable;
import java.rmi.dgc.VMID;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestTurnRemote extends GCMFunctionalTestDefaultNodes {

    public TestTurnRemote() {
        super(1, 1);
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        Node node = super.getANode();
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {}, node);
        RObject ro = ao.deploy();

        VMID localVMID = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        VMID remoteVMID = ro.callMe();
        Assert.assertFalse(localVMID.equals(remoteVMID));

    }

    @SuppressWarnings("serial")
    public static class AO implements Serializable {
        public AO() {
        }

        public RObject deploy() {
            return PARemoteObject.turnRemote(new RObject());
        }
    }

    public static class RObject {
        public RObject() {
        }

        public VMID callMe() {
            return ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        }
    }

}
