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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


/**
 * Holder class for the data representation.
 */
public abstract class AbstractData extends Observable {
    // -------------------------------------------
    // --- Variables -----------------------------
    // -------------------------------------------

    /**
     * A boolean to know if this is monitored or not. A not monitored object has
     * no children (its monitored children map is empty).
     * <p>
     * This volatile variable is used to provide thread-safety since it can be
     * accessed by several threads at the same time during the reset
     * communications user action.
     */
    protected volatile boolean isMonitored = true;

    /**
     * The monitored children
     */
    protected final Map<String, AbstractData> monitoredChildren;

    /**
     * The object name associated to this object.
     */
    protected final ObjectName objectName;

    // -------------------------------------------
    // --- Constructor ---------------------------
    // -------------------------------------------

    /**
     * The standard constructor for this model. Only object name have to be
     * provided. A HashMap<String, AbstractData> is used for monitored children
     * and not monitored ones.
     * 
     * @param objectName
     *            An instance of ObjectName for this model
     */
    public AbstractData(final ObjectName objectName) {
        this(objectName, new ConcurrentHashMap<String, AbstractData>());
    }

    /**
     * This constructor is provided to allow subclasses to specify their own map
     * implementations for monitored children and not monitored children.
     * 
     * @param objectName
     *            An instance of ObjectName for this model
     * @param monitoredChildren
     *            An instance of map for monitored children
     */
    public AbstractData(final ObjectName objectName, final Map<String, AbstractData> monitoredChildren) {
        this.objectName = objectName;
        this.monitoredChildren = monitoredChildren;
    }

    // -------------------------------------------
    // --- Methods -------------------------------
    // -------------------------------------------

    /**
     * Returns the object name associated to this object.
     * 
     * @return
     */
    public ObjectName getObjectName() {
        return this.objectName;
    }

    /**
     * Adds a child to this object, and explore this one.
     * 
     * @param child
     *            The child to explore
     */
    public synchronized void addChild(AbstractData child) {
        if (!this.monitoredChildren.containsKey(child.getKey())) {
            this.monitoredChildren.put(child.getKey(), child);
            setChanged();
            notifyObservers(new MVCNotification(MVCNotificationTag.ADD_CHILD, child));
            child.explore();
        }
    }

    /**
     * Deletes a child from all recorded data.
     * 
     * @param child
     *            The child to delete.
     */
    public void removeChild(AbstractData child) {
        if (child == null) {
            return;
        }
        String key = child.getKey();
        this.monitoredChildren.remove(key);
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.REMOVE_CHILD, child));
    }

    /**
     * Moves a child from the monitored children to the NOT monitored children.
     * 
     * @param child
     *            The child to add to the NOT monitored children.
     */
    public void removeChildFromMonitoredChildren(AbstractData child) {
        this.monitoredChildren.remove(child.getKey());
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.REMOVE_CHILD_FROM_MONITORED_CHILDREN, child
                .getKey()));
    }

    /**
     * Returns the list of monitored children
     * 
     * @return The list of monitored children
     */
    public List<AbstractData> getMonitoredChildrenAsList() {
        return new ArrayList<AbstractData>(this.monitoredChildren.values());
    }

    /**
     * Returns a copy of the map of the monitored children.
     * 
     * @return
     */
    public Map<String, AbstractData> getMonitoredChildrenAsMap() {
        return new HashMap<String, AbstractData>(this.monitoredChildren);
    }

    /**
     * Returns the number of monitored children.
     * 
     * @return The number of monitored children.
     */
    public int getMonitoredChildrenSize() {
        return this.monitoredChildren.size();
    }

    /**
     * Returns a child, searches in all recorded data
     * 
     * @param key
     * @return the child
     */
    public AbstractData getChild(String key) {
        return this.monitoredChildren.get(key);
    }

    /**
     * Returns true if this object has associated a child with this key.
     * 
     * @param keyChild
     * @return True if this object has associated a child with this key.
     */
    public boolean containsChild(String keyChild) {
        return this.monitoredChildren.containsKey(keyChild);
    }

    /**
     * Returns the object's parent
     * 
     * @return the object's parent
     */
    public abstract AbstractData getParent();

    /**
     * Updates the set of the children of this object so that they are in sync
     * with the children of the monitored object corresponding to this object.
     * 
     */
    public abstract void explore();

    /**
     * Explores each child of monitored children.
     */
    public void exploreEachChild() {
        for (AbstractData child : getMonitoredChildrenAsList()) {
            child.explore();
        }
    }

    /**
     * Returns an unique identifer, it is an unique key used to add this object
     * to a set of monitored children.
     * 
     * @return The unique key.
     */
    public abstract String getKey();

    /**
     * Returns the type of the object (ex : "active object" for ActiveObject).
     * 
     * @return the type of the object.
     */
    public abstract String getType();

    /**
     * Returns the name of the object.
     * 
     * @return the name of the object.
     */
    public abstract String getName();

    /**
     * Returns the ProActive Connection
     * 
     * @return a ProActiveConnection
     */
    public ProActiveConnection getProActiveConnection() {
        return this.getParent().getProActiveConnection();
    }

    /**
     * Returns the MBeanServerConnection Connection. This method is used to
     * avoid third-party plugins being dependent on ProActive
     * 
     * @return a ProActiveConnection
     */
    public MBeanServerConnection getMBeanServerConnection() {
        return getProActiveConnection();
    }

    /**
     * Invokes an operation on the MBean associated to the ProActive object.
     * 
     * @param operationName
     *            The name of the operation to be invoked.
     * @param params
     *            An array containing the parameters to be set when the
     *            operation is invoked
     * @param signature
     *            An array containing the signature of the operation.
     * 
     * @return The object returned by the operation, which represents the result
     *         of invoking the operation on the ProActive object.
     * 
     * @throws IOException
     * @throws ReflectionException
     *             Wraps a <CODE>java.lang.Exception</CODE> thrown while
     *             trying to invoke the method.
     * @throws MBeanException
     *             Wraps an exception thrown by the MBean's invoked method.
     * @throws InstanceNotFoundException
     *             The MBean representing the ProActive object is not registered
     *             in the remote MBean server.
     */
    public Object invoke(String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return getProActiveConnection().invoke(getObjectName(), operationName, params, signature);
    }

    /**
     * Invokes an operation on the MBean associated to the ProActive object.
     * 
     * @param operationName
     *            The name of the operation to be invoked.
     * @param params
     *            An array containing the parameters to be set when the
     *            operation is invoked
     * @param signature
     *            An array containing the signature of the operation.
     * 
     * @return The object returned by the operation, which represents the result
     *         of invoking the operation on the ProActive object.
     */
    public Object invokeAsynchronous(String operationName, Object[] params, String[] signature) {
        return getProActiveConnection().invokeAsynchronous(getObjectName(), operationName, params, signature);
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException,
            InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return getProActiveConnection().getAttribute(getObjectName(), attribute);
    }

    public Object getAttributeAsynchronous(String attribute) {
        return getProActiveConnection().getAttributeAsynchronous(getObjectName(), attribute);
    }

    public String getHostUrlServer() {
        return getParent().getHostUrlServer();
    }

    /**
     * Returns the JMX Server Name
     * 
     * @return the JMX Server Name
     */
    protected String getServerName() {
        return getParent().getServerName();
    }

    /**
     * Returns the current World
     * 
     * @return The World, or null if the parent of this object is null.
     */
    public WorldObject getWorldObject() {
        return getParent().getWorldObject();
    }

    /**
     * Destroy this object.
     */
    public void destroy() {
        getParent().removeChild(this);
    }

    /**
     * Returns the host rank.
     * 
     * @return the host rank.
     */
    public int getHostRank() {
        return getParent().getHostRank();
    }

    /**
     * Return the max depth.
     * 
     * @return the max depth.
     */
    public int getDepth() {
        return getParent().getDepth();
    }

    /**
     * Returns true if this object is monitored false otherwise
     * 
     * @return
     */
    public boolean isMonitored() {
        return isMonitored;
    }

    /**
     * Sets the monitored state of this object
     */
    public void setMonitored() {
        this.isMonitored = true;
        // Explore this object immediately
        this.explore();
    }

    /**
     * This object is no more monitored, it exists but its children are
     * destroyed.
     */
    public void setNotMonitored() {
        if (!this.isMonitored) {
            return;
        }
        this.isMonitored = false;
        Console.getInstance(Activator.CONSOLE_NAME).log(
                getType() + " " + getName() + " is no more monitored.");
        for (final AbstractData child : this.monitoredChildren.values()) {
            child.destroy();
        }
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.STATE_CHANGED, State.NOT_MONITORED));
    }
}
