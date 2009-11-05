/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;


/**
 * Used in the JMX notifications
 * @author The ProActive Team
 */
public class NodeNotificationData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;
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
