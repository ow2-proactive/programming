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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.node.Node;


/**
 * <p>
 * Event sent when a Node is created for a given VirtualNode.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2004/07/06
 * @since   ProActive 2.0.1
 *
 */
public class NodeCreationEvent extends ProActiveEvent {
    public static final int NODE_CREATED = 10;

    protected Node node;

    protected VirtualNodeInternal vn;

    protected int nodeCreated;

    /**
     * Creates a new <code>NodeCreationEvent</code>
     * @param vn the virtualnode on which the creation occurs
     * @param messageType the type of the event
     * @param node the newly created node
     * @param nodeCreated the number of nodes already created
     */
    public NodeCreationEvent(VirtualNodeInternal vn, int messageType, Node node, int nodeCreated) {
        super(vn, messageType);
        this.node = node;
        this.vn = vn;
        this.nodeCreated = nodeCreated;
    }

    /**
     * @return Returns the node.
     */
    public Node getNode() {
        return node;
    }

    /**
     * @return Returns the vn.
     */
    public VirtualNodeInternal getVirtualNode() {
        return vn;
    }

    /**
     * @return Returns the number of nodes already created.
     */
    public int getNodeCreated() {
        return nodeCreated;
    }
}
