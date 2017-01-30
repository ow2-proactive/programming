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
