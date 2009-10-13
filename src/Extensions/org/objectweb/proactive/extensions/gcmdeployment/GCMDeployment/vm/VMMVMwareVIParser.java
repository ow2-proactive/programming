package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import javax.xml.xpath.XPath;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.vmwarevi.VMwareVMM;
import org.w3c.dom.Node;


public class VMMVMwareVIParser extends AbstractVMMParser {

    static final String NODE_NAME = "vmware-vi";

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void initializeGCMVirtualMachineManager(Node vmmNode, XPath xpath, GCMVirtualMachineManager gcmVMM)
            throws VirtualServiceException {
        try {
            String user = gcmVMM.getUser(), pwd = gcmVMM.getPwd();
            for (String uri : gcmVMM.getUris()) {
                gcmVMM.addVirtualMachineManager(new VMwareVIVMMBean(uri, user, pwd));
            }
        } catch (Exception e) {
            throw new VirtualServiceException(e, "Cannot initialize GCMVirtualMachineManager.");
        }
    }
}
