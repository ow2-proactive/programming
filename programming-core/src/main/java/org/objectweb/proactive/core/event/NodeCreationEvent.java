/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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

    private static final long serialVersionUID = 61L;
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
