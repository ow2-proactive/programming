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
package org.objectweb.proactive.core.runtime;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.filter.DefaultFilter;
import org.objectweb.proactive.core.filter.Filter;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapper;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * For internal use only. This class is a runtime representation of a node and
 * should not be used outside a runtime
 */

public class LocalNode {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
    private String name;
    private List<UniqueID> activeObjectsId;
    private String virtualNodeName;
    private Properties localProperties;
    private RemoteObjectExposer<ProActiveRuntime> runtimeRoe;

    // JMX MBean
    private NodeWrapperMBean mbean;

    /**
     * @param nodeName
     *            the node's name
     * @param securityManager
     *            the security manager
     * @param virtualNodeName
     *            the name of the virtual node this node belongs to
     * @param replacePreviousBinding
     *            if a node existing with the same name in the registry, replace
     *            it
     * @throws ProActiveException
     */
    public LocalNode(String nodeName, String virtualNodeName, boolean replacePreviousBinding)
            throws ProActiveException {
        this.name = nodeName;
        this.virtualNodeName = virtualNodeName;
        this.activeObjectsId = new ArrayList<UniqueID>();
        this.localProperties = new Properties(System.getProperties());

        this.runtimeRoe = new RemoteObjectExposer<ProActiveRuntime>("LocalNode_" + name,
            ProActiveRuntime.class.getName(), ProActiveRuntimeImpl.getProActiveRuntime(),
            ProActiveRuntimeRemoteObjectAdapter.class);
        this.runtimeRoe.createRemoteObject(name, replacePreviousBinding);

        // JMX registration
        // if (CentralProperties.PA_JMX_MBEAN.isTrue()) {
        String runtimeUrl = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = FactoryName.createNodeObjectName(runtimeUrl, nodeName);
        if (!mbs.isRegistered(oname)) {
            mbean = new NodeWrapper(oname, this, runtimeUrl);
            try {
                mbs.registerMBean(mbean, oname);
            } catch (InstanceAlreadyExistsException e) {
                logger.error("A MBean with the object name " + oname + " already exists", e);
            } catch (MBeanRegistrationException e) {
                logger.error("Can't register the MBean of the LocalNode", e);
            } catch (NotCompliantMBeanException e) {
                logger.error("The MBean of the LocalNode is not JMX compliant", e);
            }
        }

        // }

        // END JMX registration
    }

    /**
     * @return Returns the active objects located inside the node.
     */
    public List<UniqueID> getActiveObjectsId() {
        return this.activeObjectsId;
    }

    /**
     * set the list of active objects contained by the node
     * 
     * @param activeObjects
     *            active objects to set.
     */
    public void setActiveObjects(List<UniqueID> activeObjects) {
        this.activeObjectsId = activeObjects;
    }

    /**
     * @return Returns the node name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            The node name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the name of the virtual node by which the node has been
     *         instancied if any.
     */
    public String getVirtualNodeName() {
        return this.virtualNodeName;
    }

    /**
     * @param virtualNodeName
     *            The virtualNodeName to set.
     */
    public void setVirtualNodeName(String virtualNodeName) {
        this.virtualNodeName = virtualNodeName;
    }

    public void terminateActiveObjects() {
    }

    /**
     * Returns all active objects. Returns All active objects.
     */
    public List<UniversalBody> getActiveObjects() {
        return this.getActiveObjects(new DefaultFilter());
    }

    /**
     * Retuns all active objects filtered.
     * 
     * @param filter
     *            The filter
     * @return all active objects filtered.
     */
    public List<UniversalBody> getActiveObjects(Filter filter) {
        List<UniversalBody> localBodies = new ArrayList<UniversalBody>();
        LocalBodyStore localBodystore = LocalBodyStore.getInstance();

        if (this.activeObjectsId == null) {
            // Probably the node is killed
            return localBodies;
        }

        synchronized (this.activeObjectsId) {
            for (int i = 0; i < this.activeObjectsId.size(); i++) {
                UniqueID bodyID = this.activeObjectsId.get(i);

                // check if the body is still on this vm
                Body body = localBodystore.getLocalBody(bodyID);

                if (body == null) {
                    // runtimeLogger.warn("body null");
                    // the body with the given ID is not any more on this
                    // ProActiveRuntime
                    // unregister it from this ProActiveRuntime
                    this.activeObjectsId.remove(bodyID);
                } else {
                    if (filter.filter(body)) {
                        // the body is on this runtime then return the remote
                        // reference of the active object
                        localBodies.add(body.getRemoteAdapter());
                    }
                }
            }
        }
        return localBodies;
    }

    /**
     * Unregisters the specified <code>UniqueID</code> from the node
     * 
     * @param bodyID
     *            The <code>UniqueID</code> to remove
     */
    public void unregisterBody(UniqueID bodyID) {
        this.activeObjectsId.remove(bodyID);
    }

    /**
     * Registers the specified body in the node. In fact it is the
     * <code>UniqueID</code> of the body that is attached to the node.
     * 
     * @param bodyID
     *            The body to register
     */
    public void registerBody(UniqueID bodyID) {
        this.activeObjectsId.add(bodyID);
    }

    public void terminate() {
        List<UniqueID> activeObjects = this.getActiveObjectsId();

        for (int i = 0; i < activeObjects.size(); i++) {
            UniqueID bodyID = activeObjects.get(i);

            // check if the body is still on this vm
            Body body = LocalBodyStore.getInstance().getLocalBody(bodyID);

            if (body != null) {
                ProActiveLogger.getLogger(Loggers.NODE).info(
                        "node " + this.name + " is being killed, terminating body " + bodyID);
                body.terminate();
            }
        }

        try {
            this.runtimeRoe.unregisterAll();
            this.runtimeRoe.unexportAll();
        } catch (Exception e) {
            logger.info(e.toString());
        }

        // JMX Notification
        ProActiveRuntimeWrapperMBean runtimeMBean = ProActiveRuntimeImpl.getProActiveRuntime().getMBean();
        if ((runtimeMBean != null) && (this.mbean != null)) {
            runtimeMBean.sendNotification(NotificationType.nodeDestroyed, this.mbean.getURL());
        }

        // END JMX Notification

        // JMX unregistration
        if (mbean != null) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = this.mbean.getObjectName();
            if (mbs.isRegistered(objectName)) {
                try {
                    mbs.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e) {
                    logger.error("The MBean with the objectName " + objectName + " was not found", e);
                } catch (MBeanRegistrationException e) {
                    logger.error("The MBean with the objectName " + objectName +
                        " can't be unregistered from the MBean server", e);
                }
            }
            this.mbean = null;
        }

        // END JMX unregistration
    }

    /**
     * Put the specified key value in this property list.
     * 
     * @param key
     *            the key to be placed into this property list.
     * @param value
     *            the value corresponding to key.
     * @return the previous value of the specified key in this property list, or
     *         <code>null</code> if it did not have one.
     */
    public Object setProperty(String key, String value) {
        return this.localProperties.setProperty(key, value);
    }

    /**
     * Searches for the property with the specified key in this property list.
     * The method returns <code>null</code> if the property is not found.
     * 
     * @param key
     *            the hashtable key.
     * @return the value in this property list with the specified key value.
     */
    public String getProperty(String key) {
        return this.localProperties.getProperty(key);
    }

    public String getURL() {
        return this.runtimeRoe.getURL();
    }
}
