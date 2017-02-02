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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.w3c.dom.Node;


public class BridgeSSHParser extends AbstractBridgeParser {
    private static final String ATTR_COMMAND_OPTIONS = "commandOptions";

    private static final String ATTR_USERNAME = "username";

    private static final String ATTR_HOSTNAME = "hostname";

    static final String NODE_NAME = "sshBridge";

    @Override
    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        BridgeSSH bridgeSSH = (BridgeSSH) super.parseBridgeNode(bridgeNode, xpath);

        String hostname = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_HOSTNAME);
        String username = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_USERNAME);
        String commandOptions = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_COMMAND_OPTIONS);

        try {
            Node privateKeyNode = (Node) xpath.evaluate("dep:privateKey", bridgeNode, XPathConstants.NODE);
            if (privateKeyNode != null) {
                PathElement privateKey = GCMParserHelper.parsePathElementNode(privateKeyNode);
                bridgeSSH.setPrivateKey(privateKey);
            }

        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        if (hostname != null) {
            bridgeSSH.setHostname(hostname);
        }

        if (username != null) {
            bridgeSSH.setUsername(username);
        }

        if (commandOptions != null) {
            bridgeSSH.setCommandOptions(commandOptions);
        }

        return bridgeSSH;
    }

    @Override
    public AbstractBridge createBridge() {
        return new BridgeSSH();
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }
}
