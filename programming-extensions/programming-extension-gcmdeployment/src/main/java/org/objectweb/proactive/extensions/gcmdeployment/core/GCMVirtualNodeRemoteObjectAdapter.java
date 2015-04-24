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
package org.objectweb.proactive.extensions.gcmdeployment.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.SynchronousProxy;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;


public class GCMVirtualNodeRemoteObjectAdapter extends Adapter<GCMVirtualNode> implements GCMVirtualNode,
        Serializable {

    private static final long serialVersionUID = 62L;

    boolean isLocal = true;
    transient GCMVirtualNode vn;

    @Override
    protected void construct() {
        vn = GCMVirtualNodeImpl.getLocal(target.getUniqueID());
        if (vn == null) {
            isLocal = false;
            vn = target;
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        vn = GCMVirtualNodeImpl.getLocal(target.getUniqueID());
        if (vn == null) {
            isLocal = false;
            vn = target;
        }
    }

    public Node getANode() {
        return vn.getANode();
    }

    public Node getANode(int timeout) {
        return vn.getANode(timeout);
    }

    public List<Node> getCurrentNodes() {
        return vn.getCurrentNodes();
    }

    public Topology getCurrentTopology() {
        return vn.getCurrentTopology();
    }

    public String getName() {
        return vn.getName();
    }

    public long getNbCurrentNodes() {
        return vn.getNbCurrentNodes();
    }

    public long getNbRequiredNodes() {
        return vn.getNbRequiredNodes();
    }

    public List<Node> getNewNodes() {
        return vn.getNewNodes();
    }

    public boolean isGreedy() {
        return vn.isGreedy();
    }

    public boolean isReady() {
        return vn.isReady();
    }

    public void subscribeIsReady(Object client, String methodName) throws ProActiveException {
        if (!isLocal && (client instanceof BodyProxy) || (client instanceof ProxyForGroup) ||
            (client instanceof SynchronousProxy)) {
            throw new ProActiveException(
                "Remote subscription is only possible when client is an Active Object, a Group or a Remote Object");
        }
        vn.subscribeIsReady(client, methodName);
    }

    public void subscribeNodeAttachment(Object client, String methodName, boolean withHistory)
            throws ProActiveException {
        if (!isLocal && (client instanceof BodyProxy) || (client instanceof ProxyForGroup) ||
            (client instanceof SynchronousProxy)) {
            throw new ProActiveException(
                "Remote subscription is only possible when client is an Active Object, a Group or a Remote Object");
        }

        vn.subscribeNodeAttachment(client, methodName, withHistory);

    }

    public void unsubscribeIsReady(Object client, String methodName) throws ProActiveException {
        if (!isLocal && (client instanceof BodyProxy) || (client instanceof ProxyForGroup) ||
            (client instanceof SynchronousProxy)) {
            throw new ProActiveException(
                "Remote subscription is only possible when client is an Active Object, a Group or a Remote Object");
        }
        vn.unsubscribeIsReady(client, methodName);
    }

    public void unsubscribeNodeAttachment(Object client, String methodName) throws ProActiveException {
        if (!isLocal && (client instanceof BodyProxy) || (client instanceof ProxyForGroup) ||
            (client instanceof SynchronousProxy)) {
            throw new ProActiveException(
                "Remote subscription is only possible when client is an Active Object, a Group or a Remote Object");
        }
        vn.unsubscribeNodeAttachment(client, methodName);
    }

    public void updateTopology(Topology topology) {
        vn.updateTopology(topology);
    }

    public void waitReady() {
        vn.waitReady();
    }

    public void waitReady(long timeout) throws ProActiveTimeoutException {
        vn.waitReady(timeout);
    }

    public UniqueID getUniqueID() {
        return vn.getUniqueID();
    }
}
