package org.objectweb.proactive.extra.p2p.service.node;

import java.util.Vector;

import org.objectweb.proactive.core.node.Node;


/**
 * This is common interface to a lookup object in the Peer to peer infrastructure
 * A peer to peer lookup is a  nodes broker, and it sends RequestNodesMessage object 
 * in the peer to peer network, peers answer to the broker (i.e. a P2Plookup object)
 * by getting the stub of the remote lookup stored in the RequestNodesMessage
 * This interface defines the type of P2Plookup stub stored in RequestNodesMessage.
 * 
 * 
 * So this interface defines the callBacks called by remote NodeManager to P2PNodelookup
 * when those NodeManager have nodes to provide.
 * 
 * The basic implementation is {@link P2PNodeLookup}.
 * 
 * @author ProActive
 *
 */
public interface P2PLookupInt {
    /**
     * Receipt a reference to a shared node.
     *
     * @param givenNode the shared node.
     * @param remoteNodeManager the remote node manager for the given node.
     * @return the total number of nodes still needed.
     */
    public P2PNodeAck giveNode(Node givenNode, P2PNodeManager remoteNodeManager);

    /** Receipt shared nodes
     * @param givenNodes
     * @param remoteNodeManager
     */
    public void giveNodeForMax(Vector<Node> givenNodes, P2PNodeManager remoteNodeManager);

}
