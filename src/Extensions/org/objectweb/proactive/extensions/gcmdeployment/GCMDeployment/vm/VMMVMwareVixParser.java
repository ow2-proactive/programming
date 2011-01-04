/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
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
