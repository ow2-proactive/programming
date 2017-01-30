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


public class BridgeOARSHParser extends BridgeSSHParser {
    private static final String ATTR_JOB_ID = "jobId";

    static final String NODE_NAME = "oarshBridge";

    @Override
    public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
        BridgeOARSH bridge = (BridgeOARSH) super.parseBridgeNode(bridgeNode, xpath);

        String jobId = GCMParserHelper.getAttributeValue(bridgeNode, ATTR_JOB_ID);

        bridge.setJobId(jobId);

        return bridge;
    }

    @Override
    public AbstractBridge createBridge() {
        return new BridgeOARSH();
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }
}
