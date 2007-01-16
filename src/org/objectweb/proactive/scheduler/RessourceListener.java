/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.scheduler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.event.NodeCreationEvent;
import org.objectweb.proactive.core.event.NodeCreationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This is the main object needed to get all the nodes created and add them to the
 * unused queue of the RessourceManager. It implements the NodeCreationEventListener
 * that notifies the object of the newly created nodes.
 *
 * @author cjarjouh
 *
 */
public class RessourceListener implements NodeCreationEventListener {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.RESSOURCE_LISTENER);

    // must implement:   public void nodeCreated(NodeCreationEvent event);
    private Vector unusedNodes;

    public RessourceListener(Vector unusedNodes, String xmlURL, Vector vnNames) {
        ProActiveDescriptor pad = null;
        this.unusedNodes = unusedNodes;

        try {
            pad = ProActive.getProactiveDescriptor(xmlURL);
        } catch (Exception e) {
            logger.error("error starting ressource listener");
            throw new RuntimeException(e);
        }

        for (int i = 0; i < vnNames.size(); ++i) {
            // getting the next virtual node defined in the ProActive Deployement Descriptor
            VirtualNode vn = pad.getVirtualNode((String) vnNames.get(i));

            // adding 'this' or anyother class has a listener of the 'NodeCreationEvent'
            ((VirtualNodeImpl) vn).addNodeCreationEventListener(this);
            //activate that virtual node
            vn.activate();
        }
        logger.debug("starting ressource listener");
    }

    /**
     * Each time a new node is discovered, we will add it to the unusedNodes list.
     */
    public void nodeCreated(NodeCreationEvent event) {
        // get the node
        Node newNode = event.getNode();
        // now you can create an active object on your node.
        newNode.getNodeInformation().setJobID("-");
        this.unusedNodes.add(newNode);
        logger.debug("new node acquired " +
            newNode.getNodeInformation().getURL());
    }
}
