/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;


public abstract class AbstractBridgeParser implements BridgeParser {
    static final String ATT_ID = "id";
    static final String ATT_HOSTNAME = "hostname";
    static final String ATT_USERNAME = "username";
    static final String ATT_COMMANDPATH = "commandPath";

    public AbstractBridgeParser() {
    }

    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        String value;
        AbstractBridge bridge = createBridge();

        // Mandatory fields
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_ID);
        bridge.setId(value);
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_HOSTNAME);
        bridge.setHostname(value);

        // Optional fields
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_USERNAME);
        if (value != null) {
            bridge.setUsername(value);
        }
        value = GCMParserHelper.getAttributeValue(bridgeNode, ATT_COMMANDPATH);
        if (value != null) {
            bridge.setCommandPath(value);
        }

        return bridge;
    }

    public abstract AbstractBridge createBridge();

    public abstract String getNodeName();
}
