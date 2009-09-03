package unitTests.dataspaces.mock;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;


public class MOCKNode implements Node {

    final private MOCKNodeInformation info;
    final private MOCKProActiveRuntime runtime;

    public MOCKNode(String runtimeId, String nodeId) {
        info = new MOCKNodeInformation(nodeId);
        runtime = new MOCKProActiveRuntime(runtimeId);
    }

    public Object[] getActiveObjects() throws NodeException, ActiveObjectCreationException {
        return null;
    }

    public Object[] getActiveObjects(String className) throws NodeException, ActiveObjectCreationException {
        return null;
    }

    public NodeInformation getNodeInformation() {
        return info;
    }

    public int getNumberOfActiveObjects() throws NodeException {

        return 0;
    }

    public ProActiveRuntime getProActiveRuntime() {
        return runtime;
    }

    public String getProperty(String key) throws ProActiveException {

        return null;
    }

    public VMInformation getVMInformation() {
        return runtime.getVMInformation();
    }

    public void killAllActiveObjects() throws NodeException, IOException {

    }

    public Object setProperty(String key, String value) throws ProActiveException {

        return null;
    }

}
