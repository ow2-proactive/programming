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
