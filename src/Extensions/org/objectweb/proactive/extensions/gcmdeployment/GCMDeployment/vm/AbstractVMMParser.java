/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class allows the parsing of a specific GCMD file collecting
 * common pieces of information to deploy virtual machines from
 * GCMD.
 * @author jmguilla
 *
 */
public abstract class AbstractVMMParser implements VMMParser {

    static final String XPATH_AUTH = "dep:authentication";
    static final String XPATH_HYPERVISOR = "dep:hypervisor";
    static final String XPATH_IMAGE = "dep:image";
    static final String PA_VMM_ID = "id";
    static final String PA_AUTH_USER = "user";
    static final String PA_AUTH_PWD = "pwd";
    static final String PA_HYPERVISOR_URI = "url";
    static final String PA_IMAGE_ID = "key";
    static final String PA_IMAGE_OS = "os";
    static final String PA_IMAGE_COUNT = "count";

    /**
     * To get authentication, hypervisor's uri, image and id information.
     * @param vmmNode
     * @param xpath
     * @return
     * @throws XPathExpressionException
     * @throws VirtualServiceException
     */
    public GCMVirtualMachineManager parseVMMNode(Node vmmNode, XPath xpath) throws XPathExpressionException,
            VirtualServiceException {
        GCMVirtualMachineManager vmm = new GCMVirtualMachineManager();
        //setId
        String id = GCMParserHelper.getAttributeValue(vmmNode, PA_VMM_ID);
        vmm.setId(id);

        //gathering authentication info
        NodeList auths = (NodeList) xpath.evaluate(XPATH_AUTH, vmmNode, XPathConstants.NODESET);
        if (auths.getLength() > 1)
            GCMDeploymentLoggers.GCMD_LOGGER
                    .warn("To many authentication info supplied. Only the first one is taken into account");
        if (auths != null && auths.getLength() >= 1) {
            Node auth = auths.item(0);
            String value = GCMParserHelper.getAttributeValue(auth, PA_AUTH_USER);
            vmm.setUser(value);
            value = GCMParserHelper.getAttributeValue(auth, PA_AUTH_PWD);
            vmm.setPwd(value);
        }

        //gathering hypervisors info
        NodeList hypervisors = (NodeList) xpath.evaluate(XPATH_HYPERVISOR, vmmNode, XPathConstants.NODESET);
        if (hypervisors != null) {
            for (int i = 0; i < hypervisors.getLength(); ++i) {
                Node hypervisor = hypervisors.item(i);
                vmm.addHypervisorURI(GCMParserHelper.getAttributeValue(hypervisor, PA_HYPERVISOR_URI));
            }
        } else {
            vmm.addHypervisorURI("localhost");
        }

        //gathering images info
        NodeList images = (NodeList) xpath.evaluate(XPATH_IMAGE, vmmNode, XPathConstants.NODESET);
        if (images != null) {
            for (int i = 0; i < images.getLength(); ++i) {
                Node image = images.item(i);
                String key = GCMParserHelper.getAttributeValue(image, PA_IMAGE_ID);
                String count = GCMParserHelper.getAttributeValue(image, PA_IMAGE_COUNT);
                String os = GCMParserHelper.getAttributeValue(image, PA_IMAGE_OS);
                vmm.addVMBean(key, count == null ? 1 : Integer.parseInt(count), os);
            }
        }
        initializeGCMVirtualMachineManager(vmmNode, xpath, vmm);
        return vmm;
    }

    /**
     * to return the associated Virtual machine manager
     * with the good dynamic type.
     * @throws VirtualServiceException
     */
    public abstract void initializeGCMVirtualMachineManager(Node vmmNode, XPath xpath,
            GCMVirtualMachineManager gcmVMM) throws VirtualServiceException;

    /**
     * get the tag's name that the instanciated
     * parser will have to handle.
     */
    public abstract String getNodeName();

}
