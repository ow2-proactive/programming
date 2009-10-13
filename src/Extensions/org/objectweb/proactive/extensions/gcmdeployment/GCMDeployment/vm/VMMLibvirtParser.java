package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import javax.xml.xpath.XPath;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.w3c.dom.Node;


public class VMMLibvirtParser extends AbstractVMMParser {

    static final String NODE_NAME = "libvirt";

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void initializeGCMVirtualMachineManager(Node vmmNode, XPath xpath, GCMVirtualMachineManager gcmVMM)
            throws VirtualServiceException {
        try {
            for (String uri : gcmVMM.getUris()) {
                gcmVMM.addVirtualMachineManager(new LibvirtVMMBean(uri));
            }
        } catch (Exception e) {
            throw new VirtualServiceException(e, "Cannot initialize GCMVirtualMachineManager.");
        }
    }
}
