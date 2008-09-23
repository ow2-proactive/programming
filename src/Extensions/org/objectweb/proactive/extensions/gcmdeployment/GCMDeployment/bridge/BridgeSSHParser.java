/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
