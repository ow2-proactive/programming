/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.w3c.dom.Node;


public class GroupSSHParser extends AbstractGroupParser {
    private static final String ATTR_COMMAND_OPTIONS = "commandOptions";

    private static final String ATTR_HOST_LIST = "hostList";

    static final String NODE_NAME = "sshGroup";

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupSSH groupSSH = (GroupSSH) super.parseGroupNode(groupNode, xpath);

        // Mandatory attributes
        String hostList = GCMParserHelper.getAttributeValue(groupNode, ATTR_HOST_LIST);
        groupSSH.setHostList(hostList);

        String commandOptions = GCMParserHelper.getAttributeValue(groupNode, ATTR_COMMAND_OPTIONS);
        if (commandOptions != null) {
            groupSSH.setCommandOption(commandOptions);
        }

        try {
            Node privateKeyNode = (Node) xpath.evaluate("dep:privateKey", groupNode, XPathConstants.NODE);
            if (privateKeyNode != null) {
                PathElement privateKey = GCMParserHelper.parsePathElementNode(privateKeyNode);
                groupSSH.setPrivateKey(privateKey);
            }

        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        return groupSSH;
    }

    @Override
    public AbstractGroup createGroup() {
        return new GroupSSH();
    }

    public String getNodeName() {
        return NODE_NAME;
    }
}
