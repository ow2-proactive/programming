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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.List;
import java.util.Map;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.p2p.service.util.P2PConstants;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.listener.RuntimeObjectListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


/**
 * Represents a Runtime in the IC2D model.
 * 
 * @author The ProActive Team
 */
public final class RuntimeObject extends AbstractData<HostObject, ProActiveNodeObject> {

    private final HostObject parent;
    private final String url;

    // private ProActiveConnection connection;
    private final String hostUrlServer;
    private final String serverName;
    private ProActiveRuntimeWrapperMBean proxyMBean;
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

    /** JMX Notification listener */
    private final RuntimeObjectListener listener;

    public RuntimeObject(HostObject parent, String runtimeUrl, ObjectName objectName, String hostUrl,
            String serverName) {
        super(objectName);
        this.parent = parent;

        this.url = FactoryName.getCompleteUrl(runtimeUrl);

        this.hostUrlServer = hostUrl;
        this.serverName = serverName;

        this.listener = new RuntimeObjectListener(this);
    }

    @Override
    public HostObject getParent() {
        return this.parent;
    }

    @Override
    public void explore() {
        if (super.isMonitored) {
            this.findNodes();
        }
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public String getType() {
        return "runtime object";
    }

    @Override
    public String getHostUrlServer() {
        return this.hostUrlServer;
    }

    @Override
    protected String getServerName() {
        return this.serverName;
    }

    /**
     * Returns the url of this object.
     * 
     * @return An url.
     */
    public String getUrl() {
        return this.url;
    }

    @Override
    public void destroy() {
        this.unsubscribeListener();
        for (final ProActiveNodeObject child : this.getMonitoredChildrenAsList()) {
            child.destroy();
        }
        super.destroy();
    }

    @Override
    public void setMonitored() {
        if (this.isMonitored) {
            return;
        }
        super.setMonitored();
    }

    @Override
    public void setNotMonitored() {
        super.setNotMonitored();
    }

    /**
     * Kill this runtime.
     */
    public void killRuntime() {
        new Thread() {
            @Override
            public void run() {
                Object[] params = {};
                String[] signature = {};
                invokeAsynchronous("killRuntime", params, signature);
                runtimeKilled();
            }
        }.start();
    }

    public void runtimeKilled() {
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.RUNTIME_OBJECT_RUNTIME_KILLED));
        ;
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RuntimeObject.this.destroy();
            }
        }.start();
    }

    /**
     * Updates the set of IC2D's NodeObjects so that it is in sync with the
     * ProActive Nodes on the monitored Host. The update is performed by
     * comparing the existing NodeObjects with the set of ProActive Node Objects
     * returned from <code>ProActiveRuntimeWrapperMBean.getNodes() </code>
     * 
     */
    private void findNodes() {
        final ProActiveConnection proActiveConnection = this.getProActiveConnection();
        if (proActiveConnection == null) {
            return;
        }

        if (this.proxyMBean == null) {
            this.proxyMBean = (ProActiveRuntimeWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(
                    proActiveConnection, super.objectName, ProActiveRuntimeWrapperMBean.class, false);
        }

        if (!PAActiveObject.pingActiveObject(proActiveConnection)) {
            System.out.println("Connection to runtime closed: " + this.getName());
            return;
        }

        List<ObjectName> nodeNames = null;
        try {
            if (!(proActiveConnection.isRegistered(super.objectName))) {
                return;
            }
            nodeNames = this.proxyMBean.getNodes();
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Could not get node list from runtime MBean of " + this.getName());
        }

        if (nodeNames == null) {
            return;
        }

        final Map<String, ProActiveNodeObject> childrenToRemove = this.getMonitoredChildrenAsMap();

        for (final ObjectName name : nodeNames) {
            // Search if the node is a P2P node
            final String nodeName = name.getKeyProperty(FactoryName.NODE_NAME_PROPERTY);
            if (nodeName.startsWith(P2PConstants.P2P_NODE_NAME) && getWorldObject().isP2PHidden() ||
                NodeFactory.isHalfBodiesNode(nodeName)) {
                // We have to skeep this node because it is a P2PNode or a
                // halfBodiesNode
                continue;
            }

            // Build the complete nodeUrl from the hostUrlServer and nodeName
            final String nodeUrl = this.hostUrlServer + nodeName;

            ProActiveNodeObject child = (ProActiveNodeObject) this.getChild(nodeUrl);

            // If this child is not monitored.
            if (child == null) {
                // Get the mbean proxy for the current node
                final NodeWrapperMBean proxyNodeMBean = (NodeWrapperMBean) MBeanServerInvocationHandler
                        .newProxyInstance(proActiveConnection, name, NodeWrapperMBean.class, false);

                // Get the jobId and the virtualNodeName in one call
                final String[] res = proxyNodeMBean.getJobIdAndVirtualNodeName();

                final String jobId = res[0];
                final String virtualNodeName = res[1];

                if (virtualNodeName == null) {
                    Console
                            .getInstance(Activator.CONSOLE_NAME)
                            .err(
                                    "Problem when getting virtual node name from the remote NodeWrapperNbean for node " +
                                        name);
                    logger
                            .error("Problem when getting virtual node name from the remote NodeWrapperNbean for node " +
                                nodeName + ". A null value was received.");
                    continue;
                }

                // Find the virtualNode if already monitored
                VirtualNodeObject vn = getWorldObject().getVirtualNode(virtualNodeName);

                // This virtual node is not monitored
                if (vn == null) {
                    vn = new VirtualNodeObject(virtualNodeName, jobId, getWorldObject());
                    getWorldObject().addVirtualNode(vn);
                }

                // Once the virtualNode object has been created or found
                // Create the child node object
                child = new ProActiveNodeObject(this, nodeUrl, name, vn, proxyNodeMBean);
                addChild(child);
                vn.addChild(child);

            }
            // This child is already monitored, but this child maybe contains
            // some not monitored objects.
            // else {
            child.explore();
            // }
            // Removes from the model the not monitored or terminated nodes.
            childrenToRemove.remove(child.getKey());
        }

        // Some child have to be removed
        for (final ProActiveNodeObject child : childrenToRemove.values()) {
            child.destroy();
        }
    }

    @Override
    public String getName() {
        return URIBuilder.getNameFromURI(this.url);
    }

    /**
     * Returns the ProActiveConnection for this Runtime
     */
    @Override
    public ProActiveConnection getProActiveConnection() {
        return JMXNotificationManager.getInstance().getConnection(this.url);
    }

    @Override
    public String toString() {
        return "Runtime: " + getUrl();
    }

    public boolean subscribeListener() {
        // Subscribe to the jmx listener
        try {
            JMXNotificationManager.getInstance().subscribe(super.objectName, this.listener, this.url);
            return true;
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot subscribe the JMX listener of " + getType() + " " + getName());
            return false;
        }
    }

    /**
     * This method must be called when ic2d is closed or the parent host object is not monitored or destroyed
     */
    public void unsubscribeListener() {
        try {
            // Unsubscribe the JMX listener
            JMXNotificationManager.getInstance().unsubscribe(super.objectName, this.listener);
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot unsubscribe the JMX listener of " + getType() + " " + getName());
        }
    }
}
