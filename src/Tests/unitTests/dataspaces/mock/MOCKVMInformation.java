package unitTests.dataspaces.mock;

import java.net.InetAddress;
import java.rmi.dgc.VMID;

import org.objectweb.proactive.core.runtime.VMInformation;


public class MOCKVMInformation implements VMInformation {

    private static final long serialVersionUID = 8097368073406561838L;

    final private String name;

    public MOCKVMInformation(String runtimeId) {
        name = runtimeId;
    }

    public long getCapacity() {

        return 0;
    }

    public long getDeploymentId() {

        return 0;
    }

    public String getDescriptorVMName() {

        return null;
    }

    public String getHostName() {

        return null;
    }

    public InetAddress getInetAddress() {

        return null;
    }

    public String getName() {
        return name;
    }

    public long getTopologyId() {

        return 0;
    }

    public VMID getVMID() {

        return null;
    }

}
