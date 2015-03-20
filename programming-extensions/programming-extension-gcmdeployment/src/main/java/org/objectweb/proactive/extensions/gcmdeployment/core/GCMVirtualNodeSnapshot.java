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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.core;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;


/** An immutable snapshot of a GCM virtual node
 * 
 * @see GCMVirtualNode
 */
final public class GCMVirtualNodeSnapshot implements Serializable {

    private static final long serialVersionUID = 60L;
    final private String name;
    final private boolean isGreedy;
    final private boolean isReady;
    final private long nbRequiredNodes;
    final private long nbCurrentNodes;
    final private List<Node> currentNodes;
    final private Topology currentTopology;
    final private UniqueID uniqueId;

    public GCMVirtualNodeSnapshot(final GCMVirtualNode vn) {
        this.name = vn.getName();
        this.isGreedy = vn.isGreedy();
        this.isReady = vn.isReady();
        this.nbRequiredNodes = vn.getNbRequiredNodes();
        this.nbCurrentNodes = vn.getNbCurrentNodes();
        this.currentNodes = vn.getCurrentNodes();
        this.currentTopology = vn.getCurrentTopology();
        this.uniqueId = vn.getUniqueID();
    }

    /**
     * @see GCMVirtualNode#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see GCMVirtualNode#isGreedy()
     */
    public boolean isGreedy() {
        return isGreedy;
    }

    /**
     * @see GCMVirtualNode#isReady()
     */
    public boolean isReady() {
        return isReady;
    }

    /**
     * @see GCMVirtualNode#getNbRequiredNodes()
     */
    public long getNbRequiredNodes() {
        return nbRequiredNodes;
    }

    /**
     * @see GCMVirtualNode#getNbCurrentNodes()
     */
    public long getNbCurrentNodes() {
        return nbCurrentNodes;
    }

    /**
     * @see GCMVirtualNode#getCurrentNodes()
     */
    public List<Node> getCurrentNodes() {
        return currentNodes;
    }

    /**
     * @see GCMVirtualNode#getCurrentTopology()
     */
    public Topology getCurrentTopology() {
        return currentTopology;
    }

    /**
     * @see GCMVirtualNode#getUniqueID()
     */
    public UniqueID getUniqueID() {
        return uniqueId;
    }
}
