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
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;


/**
 * Used in the JMX notifications
 * @author The ProActive Team
 */
public class NodeNotificationData implements Serializable {

    /** The node */
    private Node node;

    private String vn;

    public NodeNotificationData() {
        // No args constructor
    }

    /**
     * Creates a new NodeNotificationData
     * @param node The node
     */
    public NodeNotificationData(Node node, String vn) {
        this.node = node;
        this.vn = vn;
    }

    public Node getNode() {
        return this.node;
    }

    public String getVirtualNode() {
        return this.vn;
    }

    @Override
    public String toString() {
        return this.node.toString();
    }
}
