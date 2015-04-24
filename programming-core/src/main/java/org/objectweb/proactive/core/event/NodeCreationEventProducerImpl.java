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


public class NodeCreationEventProducerImpl extends AbstractEventProducer implements NodeCreationEventProducer {

    private static final long serialVersionUID = 62L;
    public NodeCreationEventProducerImpl() {
        super(false, false);
    }

    //
    //-------------------inherited methods from AbstractEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.AbstractEventProducer#notifyOneListener(ProActiveListener, ProActiveEvent)
     */
    @Override
    protected void notifyOneListener(ProActiveListener proActiveListener, ProActiveEvent event) {
        NodeCreationEvent creationEvent = (NodeCreationEvent) event;
        NodeCreationEventListener creationEventListener = (NodeCreationEventListener) proActiveListener;

        //notify the listener that a creation occurs
        creationEventListener.nodeCreated(creationEvent);
    }

    //
    //-------------------PROTECTED METHODS------------------
    //
    protected void notifyListeners(VirtualNodeInternal vn, int type, Node node, int nodeCreated) {
        if (hasListeners()) {
            notifyAllListeners(new NodeCreationEvent(vn, type, node, nodeCreated));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("no listener");
            }
        }
    }

    //
    //-------------------implements NodeCreationEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.NodeCreationEventProducer#addNodeCreationEventListener(org.objectweb.proactive.core.event.NodeCreationEventListener)
     */
    public void addNodeCreationEventListener(NodeCreationEventListener listener) {
        addListener(listener);
    }

    /**
     * @see org.objectweb.proactive.core.event.NodeCreationEventProducer#removeNodeCreationEventListener(org.objectweb.proactive.core.event.NodeCreationEventListener)
     */
    public void removeNodeCreationEventListener(NodeCreationEventListener listener) {
    }
}
