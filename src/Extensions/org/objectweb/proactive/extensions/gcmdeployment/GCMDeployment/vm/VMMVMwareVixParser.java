package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.vmwarevix.VMwareVMM.Service;
import org.w3c.dom.Node;


public class VMMVMwareVixParser extends AbstractVMMParser {

    static final String NODE_NAME = "vmware-vix";
    static final String PA_HYPERVISOR_PORT = "port";
    static final String PA_HYPERVISOR_SERVICE = "service";
    static final String PA_SERVICE_SERVER = "server";
    static final String PA_SERVICE_WORKSTATION = "workstation";
    static final String PA_SERVICE_VI = "vi";

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void initializeGCMVirtualMachineManager(Node vmmNode, XPath xpath, GCMVirtualMachineManager gcmVMM)
            throws VirtualServiceException {
        try {
            String port = GCMParserHelper.getAttributeValue(vmmNode, PA_HYPERVISOR_PORT);
            String service = GCMParserHelper.getAttributeValue(vmmNode, PA_HYPERVISOR_SERVICE);
            String user = gcmVMM.getUser(), pwd = gcmVMM.getPwd();
            int portNum = 0;
            for (String uri : gcmVMM.getUris()) {
                if (port != null) {
                    portNum = Integer.parseInt(port);
                }
                gcmVMM.addVirtualMachineManager(new VMwareVixVMMBean(uri, user, pwd, portNum,
                    getService(service)));
            }
        } catch (Exception e) {
            throw new VirtualServiceException(e, "Cannot initialize GCMVirtualMachineManager.");
        }
    }

    private Service getService(String service) {
        if (service.equals(PA_SERVICE_SERVER))
            return Service.vmwareServer;
        if (service.equals(PA_SERVICE_WORKSTATION))
            return Service.vmwareWorkstation;
        if (service.equals(PA_SERVICE_VI))
            return Service.vmwareServerVI;
        return Service.vmwareDefault;
    }

}
