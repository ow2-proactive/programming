/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.mbean.NodeWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


/**
 * This class represents the model of a proactive node.
 * 
 * @author vbodnart
 */
public final class ProActiveNodeObject extends AbstractData<RuntimeObject, ActiveObject> {
    /**
     * The parent runtime object
     */
    private final RuntimeObject parent;

    /**
     * The virtual node that owns this node object
     */
    private final VirtualNodeObject vnParent;

    /**
     * The url of this node object
     */
    private final String url;

    // Warning: Don't use this variable directly, use getProxyNodeMBean().
    private final NodeWrapperMBean proxyNodeMBean;

    public ProActiveNodeObject(final RuntimeObject parent, final String url, final ObjectName objectName,
            final VirtualNodeObject vnParent, final NodeWrapperMBean proxyNodeMBean) {
        // Call super constructor in order to specify a TreeMap<String,
        // AbstractData> for monitored children
        super(objectName);

        // new TreeMap<String, AbstractData>(
        // new ActiveObject.ActiveObjectComparator()));
        this.parent = parent;
        this.vnParent = vnParent;
        this.url = FactoryName.getCompleteUrl(url);
        this.proxyNodeMBean = proxyNodeMBean;
    }

    @Override
    public RuntimeObject getParent() {
        return this.parent;
    }

    /**
     * Returns the virtual node.
     * 
     * @return the virtual node.
     */
    public VirtualNodeObject getVirtualNode() {
        return this.vnParent;
    }

    /**
     * Gets a proxy for the MBean representing this Node. If the proxy does not
     * exist it creates it
     * 
     * @return The reference on the proxy to the node mbean
     */
    private NodeWrapperMBean getProxyNodeMBean() {
        return this.proxyNodeMBean;
    }

    /**
     * The destroy method of this class first destroys each children then
     * removes itself from its virtual node.
     */
    @Override
    public void destroy() {
        for (final ActiveObject child : this.getMonitoredChildrenAsList()) {
            child.internalDestroy();
        }
        this.monitoredChildren.clear();
        // Fire notification
        super.notifyObservers(new MVCNotification(MVCNotificationTag.REMOVE_CHILDREN, null));
        this.vnParent.removeChild(this);
        super.destroy();
    }

    @Override
    public void explore() {
        if (super.isMonitored) {
            this.findActiveObjects();
        }
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public String getType() {
        return "node object";
    }

    /**
     * Returns the url of this object.
     * 
     * @return An url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Finds all active objects of this node.
     */
    private void findActiveObjects() {
        final List<ObjectName> activeObjectNames = getProxyNodeMBean().getActiveObjects();
        // The list that will contain all new children
        final List<ActiveObject> newChildren = new ArrayList<ActiveObject>();
        for (final ObjectName aoObjectName : activeObjectNames) {
            // Get the ID property from the objectName
            final String aoID = aoObjectName.getKeyProperty(FactoryName.AO_ID_PROPERTY);

            // First check if the aoID is known
            if (super.getWorldObject().findActiveObject(aoID) != null) {
                continue;
            }
            // If the aoID is unknown create a new ActiveObject from the
            // aoObjectName
            if (!super.monitoredChildren.containsKey(aoID)) {
                final ActiveObject newChild = ActiveObject.createActiveObjectFrom(aoObjectName, this);
                super.monitoredChildren.put(aoID, newChild);
                newChildren.add(newChild);
            }
        }
        // In order to avoid sending costly notifications to observers each time
        // this method is called
        // set this observable object to be changed only if there are new
        // children

        final int numberOfNewChildren = newChildren.size();
        if (numberOfNewChildren > 0) {
            super.setChanged();
        }
        // If no new children return silently
        if (super.countObservers() != 0) {
            // If only one child no need to refresh all children there is a
            // specific notification for that case
            if (numberOfNewChildren == 1) {
                // Fire add child notification
                super.notifyObservers(new MVCNotification(MVCNotificationTag.ADD_CHILD, newChildren.get(0)));
                return;
            }
            // Fire add all children notification
            super.notifyObservers(new MVCNotification(MVCNotificationTag.ADD_CHILDREN, newChildren));
        }
    }

    /**
     * Called by
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.listener.RuntimeObjectListener}
     * on bodyCreated notification.
     * <p>
     * Adds a child active object if and only if the key (ie the string
     * representation of the unique id) is unknown.
     * 
     * @param id
     *            The UniqueID of the active object to add
     * @param className
     *            The name of the reified object class
     */
    public void addActiveObjectByID(final UniqueID id, final String className) {
        // Try to retrieve this active object by key
        final String key = id.toString();
        ActiveObject activeObject = (ActiveObject) super.getChild(key);
        // If the active object is not known by this node
        if (activeObject == null) {
            // Check the world knows it
            activeObject = this.getParent().getWorldObject().findActiveObject(key);
            // if its unknown create one and add it to monitored children
            if (activeObject == null) {
                super.addChild(ActiveObject.createActiveObjectFrom(id, className, this));
            }
        }
    }

    /**
     * Called by
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.listener.RuntimeObjectListener}
     * on bodyDestroyed notification.
     * <p>
     * removes a child active object if and only if the key (ie the string
     * representation of the unique id) is known.
     * 
     * @param id
     *            The UniqueID of the active object to add
     */
    public void removeActiveObjectByID(final UniqueID id) {
        // Try to retrieve this active object by key
        final String key = id.getCanonString();
        super.removeChild(super.getChild(key));
    }

    @Override
    public String getName() {
        return URIBuilder.getNameFromURI(this.url);
    }

    @Override
    public String toString() {
        return "Node: " + this.url;
    }

    /**
     * Returns the virtual node name.
     * 
     * @return the virtual node name.
     */
    public String getVirtualNodeName() {
        return this.vnParent.getName();
    }

    /**
     * Returns the Job Id.
     * 
     * @return the Job Id.
     */
    public String getJobId() {
        return this.vnParent.getJobID();
    }

    /**
     * Used to highlight this node, in a virtual node.
     * 
     * @param highlighted
     *            true, or false
     */
    public void setHighlight(boolean highlighted) {
        this.setChanged();
        this.notifyObservers(new MVCNotification(MVCNotificationTag.STATE_CHANGED,
            highlighted ? State.HIGHLIGHTED : State.NOT_HIGHLIGHTED));
    }

    public void notifyChanged() {
        this.setChanged();
        this.notifyObservers(null);
    }
}
