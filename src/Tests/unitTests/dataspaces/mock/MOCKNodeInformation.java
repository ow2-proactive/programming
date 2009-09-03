package unitTests.dataspaces.mock;

import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.VMInformation;


public class MOCKNodeInformation implements NodeInformation {

    /**
     *
     */
    private static final long serialVersionUID = 1213394569845089015L;

    final private String name;

    public MOCKNodeInformation(String nodeId) {
        name = nodeId;
    }

    public String getName() {
        return name;
    }

    public String getProtocol() {

        return null;
    }

    public String getURL() {

        return null;
    }

    public VMInformation getVMInformation() {

        return null;
    }

    public void setJobID(String jobId) {

    }

    public String getJobID() {

        return null;
    }

}
