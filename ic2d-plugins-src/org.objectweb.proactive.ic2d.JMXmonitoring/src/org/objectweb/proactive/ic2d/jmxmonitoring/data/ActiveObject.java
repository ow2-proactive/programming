/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.debug.stepbystep.BreakpointType;
import org.objectweb.proactive.core.debug.stepbystep.DebugInfo;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.listener.ActiveObjectListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


/**
 * Class for the active object representation in the IC2D model.
 */
public final class ActiveObject extends AbstractData<ProActiveNodeObject, AbstractData<?, ?>> {

    public static final String ACTIVE_OBJECT_TYPE = "active object";

    /** The parent object !! CAN CHANGE because of migration !! */
    private ProActiveNodeObject parent;

    /**
     * Forwards methods in an MBean's management interface through the MBean
     * server to the BodyWrapperMBean. !! CAN CHANGE because of migration !!
     */
    private BodyWrapperMBean proxyMBean;

    /**
     * ID used to identify the active object globally, even in case of
     * migration.
     */
    private final UniqueID id;

    /**
     * A string version of the id that is used as a key
     */
    private final String key;

    /** Name of the class used to created the active object. */
    private final String className;

    /** The object's name (ex: ao#2) */
    private final String name;

    /**
     * The JMX Notification listener that will be subscribed to the
     * JMXNotificationManager *
     */
    private final NotificationListener listener;

    /**
     * State of the object defined as a constant of the enum
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.util.State}
     */
    private State currentState = State.UNKNOWN;

    /** request queue length */
    private int requestQueueLength = -1; // -1 = not known

    /**
     * List of outgoing communications such as for each communication in the
     * list, this active object is the source.
     */
    protected final List<Communication> outgoingCommunications;

    /**
     * List of incoming communications such as for each communication in the
     * list, this active object is the target.
     */
    protected final List<Communication> incomingCommunications;

    /**
     * The destination node url used during migration !! CAN CHANGE because of
     * migration !!
     */
    private String destNodeURLOnMigration;

    /**
     * A volatile variable used to handle or not the communications concerned by
     * this active object.
     * <p>
     * This volatile variable is used to provide thread-safety since it can be
     * accessed by several threads at the same time during the reset
     * communications user action.
     */
    protected volatile boolean canHandleCommunications = true;

    /**
     * Creates and returns new instance of ActiveObject from an ObjectName. All
     * operations related to the creation of an ActiveObject representation in
     * IC2D should be done in its class, NOT elsewhere !
     * <p>
     * The parent NodeObject MUST NOT contain an active object with same key
     * (string of unique id) !
     * 
     * @param objectName
     *            The JMX object name associated to this ActiveObject
     * @param parent
     *            The parent NodeObject
     * @return A new instance of ActiveObject class
     */
    public static ActiveObject createActiveObjectFrom(final ObjectName objectName,
            final ProActiveNodeObject parent) {
        // First create the mbean proxy to get all information about the active
        // object
        final BodyWrapperMBean proxyMBean = ActiveObject.createMBeanProxy(parent, objectName);
        // Since the id is already contained as a String in the ObjectName
        // the proxyMBean.getID() call can be avoid if the UniqueID can be built
        // from a string
        return new ActiveObject(parent, objectName, proxyMBean.getID(), proxyMBean.getName(), proxyMBean);
    }

    /**
     * Creates a new proxy to the possibly remote
     * {@link org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean} from a
     * connection provided by a NodeObject and the object name associated with
     * the mbean.
     * 
     * @param parent
     *            The connection with the remote mbean server
     * @param objectName
     *            The name associated with the mbean
     * @return A new proxy to the possibly remote mbean.
     */
    private static final BodyWrapperMBean createMBeanProxy(final ProActiveNodeObject parent,
            final ObjectName objectName) {
        return (BodyWrapperMBean) MBeanServerInvocationHandler.newProxyInstance(parent
                .getProActiveConnection(), objectName, BodyWrapperMBean.class, false);
    }

    /**
     * Creates and returns new instance of ActiveObject from an ObjectName. All
     * operations related to the creation of an ActiveObject representation in
     * IC2D should be done in its class, NOT elsewhere !
     * <p>
     * The parent NodeObject MUST NOT contain an active object with same key
     * (string of unique id) !
     * 
     * @param id
     *            The UniqueID of the ActiveObject
     * @param className
     *            The name of the reified object class
     * @param parent
     *            The parent NodeObject
     * @return A new instance of ActiveObject class
     */
    public static ActiveObject createActiveObjectFrom(final UniqueID id, final String className,
            final ProActiveNodeObject parent) {
        // Create the ObjectName from the id
        final ObjectName objectName = FactoryName.createActiveObjectName(id);
        // From the ObjectName create the MBeanProxy
        final BodyWrapperMBean proxyMBean = ActiveObject.createMBeanProxy(parent, objectName);
        return new ActiveObject(parent, objectName, id, className, proxyMBean);
    }

    // -------------------------------------------
    // --- Constructor ---------------------------
    // -------------------------------------------

    /**
     * Creates a new instance of ActiveObject from an ObjectName. Use static
     * methods to create an ActiveObject.
     * 
     * @param parent
     *            The parent NodeObject
     * @param objectName
     *            The JMX object name associated to this ActiveObject
     * @param id
     *            The UniqueID of the ActiveObject
     * @param className
     *            The name of the reified object class
     * @param proxyMBean
     *            The JMX proxy to the mbean of the body
     */
    private ActiveObject(final ProActiveNodeObject parent, final ObjectName objectName, final UniqueID id,
            final String className, final BodyWrapperMBean proxyMBean) {
        super(objectName, null);
        this.parent = parent;
        this.proxyMBean = proxyMBean;
        this.id = id;
        this.key = id.toString();
        this.className = className;
        this.name = NamesFactory.getInstance().associateName(this.id, this.className);
        this.listener = new ActiveObjectListener(this);
        this.subscribeListener();

        // CopyOnWriteArrayList is used since traversal operations vastly
        // outnumbers mutations
        // See addInCommunication and addOutCommunication methods of this class
        this.outgoingCommunications = new CopyOnWriteArrayList<Communication>();
        this.incomingCommunications = new CopyOnWriteArrayList<Communication>();

        //		// LOG TO REMOVE
        //		System.out.println("ActiveObject.ActiveObject() --> creating ao : " + this.name );
        //		if (parent.containsChild(this.key)
        //				|| parent.getWorldObject().findActiveObject(key) != null) {
        //			System.out
        //					.println("ActiveObject.ActiveObject() ---> PROBLEM ! ALREADY EXISTING ActiveObject  : "
        //							+ this.name
        //							+ " on NODE : "
        //							+ this.getParent().getName()
        //							+ " "
        //							+ "Current Thread : "
        //							+ Thread.currentThread().getName());
        //			Thread.dumpStack();
        //			
        //		}

        // Used to have good performances. TODO : Check if the performances are
        // as good as its said
        this.getWorldObject().addActiveObject(this);
    }

    /**
     * Change the current state
     * 
     * @param newState
     *            The new state of this active object
     */
    public synchronized void setState(final State newState) {
        if (newState.equals(currentState)) {
            return;
        }
        switch (newState) {
            case WAITING_BY_NECESSITY:
                if (currentState == State.SERVING_REQUEST) {
                    currentState = State.WAITING_BY_NECESSITY_WHILE_SERVING;
                } else {
                    currentState = State.WAITING_BY_NECESSITY_WHILE_ACTIVE;
                }
                break;
            case RECEIVED_FUTURE_RESULT:
                if (currentState == State.WAITING_BY_NECESSITY_WHILE_SERVING) {
                    currentState = State.SERVING_REQUEST;
                } else {
                    currentState = State.ACTIVE;
                }
                break;
            default:
                currentState = newState;
                break;
        }
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.STATE_CHANGED, this.currentState));
    }

    /**
     * Returns the current state of this active object.
     * 
     * @return The current state.
     */
    public synchronized State getState() {
        return this.currentState;
    }

    /**
     * Returns true if this active object is migrating false otherwise.
     * 
     * @return true if this active object is migrating false otherwise
     */
    public synchronized boolean isMigrating() {
        return this.currentState == State.MIGRATING;
    }

    /**
     * Sends a migration request to this object to migrate to another node. This
     * method uses the mbean proxy to send an asynchronous request of migration.
     * 
     * @param targetNode
     *            The target node object
     * @return true if it has successfully migrated, false otherwise.
     */
    public synchronized boolean migrateTo(final ProActiveNodeObject targetNode) {
        final Console console = Console.getInstance(Activator.CONSOLE_NAME);
        final String nodeTargetURL = targetNode.getUrl();
        try {
            // Migrate
            this.proxyMBean.migrateTo(nodeTargetURL);
        } catch (Exception e) {
            console.err("Couldn't migrate " + this.getName() + " to " + nodeTargetURL + " Reason : " +
                e.getMessage());
            return false;
        }
        console.log("Successfully sent a request of migration of " + this + " to " + nodeTargetURL);
        return true;
    }

    /**
     * Prepares this active object to migrate, sets the current state to
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.util.State.MIGRATING}.
     * 
     * @param destinationNodeURL
     *            The url of the destination node
     */
    public void prepareToMigrate(final String destinationNodeURL) {
        this.destNodeURLOnMigration = destinationNodeURL;
        this.setState(State.MIGRATING);
    }

    /**
     * Finishes the migration. Refreshes current parent and the mbean proxy of
     * this active object.
     * <p>
     * This method is
     * <code>synchronized</p> because sometimes it can be called by 2 different threads 
     * at the same time.
     * 
     * @param destinationRuntimeURL
     *            The <code>URL</code> of the destination runtime
     */
    public synchronized void finishMigration(final String destinationRuntimeURL) {
        // First if its not a migrating object or destNodeURLOnMigration is null we can't finish migration
        if (this.currentState != State.MIGRATING || this.destNodeURLOnMigration == null) {
            return;
        }

        // Get the console for the output
        final Console console = Console.getInstance(Activator.CONSOLE_NAME);

        // Try to locate the destination runtime object from its url

        // First locate the destination host from the given URL
        // The next code extracts proto://addr:port/ from
        // proto://addr:port/runtime_name
        final String destHostURL = destinationRuntimeURL.substring(0,
                destinationRuntimeURL.lastIndexOf('/') + 1);
        final HostObject destHostObject = (HostObject) this.getParent().getWorldObject()
                .getChild(destHostURL);
        if (destHostObject == null) {
            console.log("Cannot finish migration of the active object : " + this.name +
                ". Trying to migrate to an unknown host : " + destHostURL);
            return;
        }

        // Second locate the destination runtime by its URL
        final RuntimeObject destRuntimeObject = (RuntimeObject) destHostObject
                .getChild(destinationRuntimeURL);
        if (destRuntimeObject == null) {
            console.log("Cannot finish migration of the active object : " + this.name +
                ". Trying to migrate to an unknown runtime : " + destinationRuntimeURL);
            return;
        }

        // Third locate the destination node by its URL
        final ProActiveNodeObject destNodeObject = (ProActiveNodeObject) destRuntimeObject
                .getChild(this.destNodeURLOnMigration);
        if (destNodeObject == null) {
            console.log("Cannot finish migration of the active object : " + this.name +
                ". Trying to migrate to an unknown node : " + this.destNodeURLOnMigration);
            return;
        }

        // Update the mbean proxy (recreate one from the ProActiveConnection
        // provided by
        // the new node object)
        this.proxyMBean = ActiveObject.createMBeanProxy(destNodeObject, this.objectName);

        // WARNING !!! DO NOT REMOVE COMMUNICATIONS HERE !!!
        // Communications are represented by outgoingCommunications and
        // incomingCommunications list attributes of this class.
        // Representation of a migration concerns only edit parts NOT MODELS !
        // Since the active object model is NOT DELETED.

        // Remove this from current parent node
        this.parent.removeChild(this);

        // Update the new parent
        this.parent = destNodeObject;
        // Add this to new parent
        this.parent.addChild(this);

        // Restore a standard state
        this.setState(State.WAITING_FOR_REQUEST);
        this.destNodeURLOnMigration = null;
    }

    /**
     * Say to the model that a MigrationException occurred during a migration of
     * this active object.
     * 
     * @param migrationException
     */
    public void migrationFailed(MigrationException migrationException) {
        Console.getInstance(Activator.CONSOLE_NAME).logException(
                "The active object " + this + " didn't migrate!", migrationException);
    }

    /**
     * Returns the unique id of the active object.
     * 
     * @return An unique id.
     */
    public UniqueID getUniqueID() {
        return this.id;
    }

    /**
     * Returns the name of the class given in parameter to the constructor.
     * 
     * @return The class name
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Returns a JMX Notification listener.
     * 
     * @return a JMX Notification listener.
     */
    public NotificationListener getListener() {
        return this.listener;
    }

    /**
     * Returns a direct reference on the list of outgoing communications.
     * 
     * @return A list of outgoing communications
     */
    public List<Communication> getOutgoingCommunications() {
        return this.outgoingCommunications;
    }

    /**
     * Returns a direct reference on the list of incoming communications.
     * 
     * @return A list of incoming communications
     */
    public List<Communication> getIncomingCommunications() {
        return this.incomingCommunications;
    }

    /**
     * Adds a communication from this active object to another active object
     * Warning: This active object is the source of the communication.
     * 
     * @param destinationID
     *            The unique id of the destination of the communication.
     */
    public void addOutCommunication(final UniqueID destinationID) {
        if (!this.canHandleCommunications)
            return;
        // First seek through all known outgoing communications
        for (final Communication c : this.outgoingCommunications) {
            if (c.getTarget().getUniqueID().equals(destinationID)) {
                c.addOneCall();
                return;
            }
        }
        final ActiveObject destAO = getWorldObject().findActiveObject(destinationID.getCanonString());
        // If the destination ao is not known or this has no observers return
        // silently
        if (destAO == null || this.countObservers() == 0 || destAO.countObservers() == 0) {
            return;
        }
        // If it's an unknown outgoing communication create one
        new Communication(this, destAO);
    }

    /**
     * Adds a communication to this active object from another active object
     * Warning: This active object is the target of the communication.
     * 
     * @param sourceID
     *            The unique id of the source of the communication.
     */
    public void addInCommunication(final UniqueID sourceID) {
        if (!this.canHandleCommunications)
            return;
        // First seek through all known incoming communications
        for (final Communication c : this.incomingCommunications) {
            if (c.getSource().getUniqueID().equals(sourceID)) {
                c.addOneCall();
                return;
            }
        }
        final ActiveObject srcAO = getWorldObject().findActiveObject(sourceID.getCanonString());
        // If the source ao in not known or this has no observers return
        // silently
        if (srcAO == null || this.countObservers() == 0 || srcAO.countObservers() == 0) {
            return;
        }
        // If the communication was not found create one
        new Communication(srcAO, this);
    }

    /**
     * Removes all outgoing and incoming communications of this active object.
     * For each removed communication a notification is fired to the observers
     * of this active object.
     * 
     * @param softly
     *            If true then the communications will be removed soflty (ie
     *            through
     *            {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication#disconnect()})
     *            method that can slower otherwise a rough method that consists
     *            of simply clearing the lists of communications is used.
     * 
     */
    public void removeAllCommunications(boolean softly) {
        // Soft method
        if (softly) {
            // Remove all outgoing
            for (Communication c : this.outgoingCommunications) {
                c.disconnect();
            }
            // Remove all incoming
            for (Communication c : this.incomingCommunications) {
                c.disconnect();
            }
            return;
        }

        this.outgoingCommunications.clear();
        // Fire remove event
        this.setChanged();
        notifyObservers(new MVCNotification(
            MVCNotificationTag.ACTIVE_OBJECT_REMOVE_ALL_OUTGOING_COMMUNICATION, null));

        this.incomingCommunications.clear();
        // Fire remove event
        this.setChanged();
        notifyObservers(new MVCNotification(
            MVCNotificationTag.ACTIVE_OBJECT_REMOVE_ALL_INCOMING_COMMUNICATION, null));
    }

    @Override
    public void destroy() {
        this.internalDestroy();
        // The destroy method of the super-class will remove this object from
        // the parents children
        super.destroy();
    }

    /**
     * Remove the jmx listener, removes this from world then removes communications
     */
    protected void internalDestroy() {
        // First unsubscribe JMX listener
        this.unsubscribeListener();
        // Remove this object from world
        super.getWorldObject().removeActiveObject(this.key);
        // Second remove all communications
        this.removeAllCommunications(true);
    }

    public boolean subscribeListener() {
        // Subscribe to the jmx listener
        try {
            JMXNotificationManager.getInstance().subscribe(super.objectName, this.listener,
                    this.parent.getParent().getUrl());
            return true;
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot subscribe the JMX listener of " + getType() + " " + getName());
            return false;
        }
    }

    public void unsubscribeListener() {
        try {
            // Remove the JMX Listener
            JMXNotificationManager.getInstance().unsubscribe(super.objectName, this.listener);
        } catch (Exception e) {
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Cannot unsubscribe the JMX listener of " + getType() + " " + getName());
        }
    }

    /**
     * Adds an outgoing communication to this active object and fires an
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag.ACTIVE_OBJECT_ADD_OUTGOING_COMMUNICATION}
     * WARNING ! This method MUST be called ONLY from
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication#connect()}
     * method.
     * 
     * @param outgoingCommunication
     *            The outgoing communication to add
     */
    void internalAddOutgoingCommunication(final Communication outgoingCommunication) {
        this.outgoingCommunications.add(outgoingCommunication);
        // Fire a notification
        this.setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_ADD_OUTGOING_COMMUNICATION,
            outgoingCommunication));
    }

    /**
     * Adds an incoming communication to this active object and fires an
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag.ACTIVE_OBJECT_ADD_INCOMING_COMMUNICATION}
     * WARNING ! This method MUST be called ONLY from
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication#connect()}
     * method.
     * 
     * @param incomingCommunication
     *            The incoming communication to add
     */
    void internalAddIncomingCommunication(final Communication incomingCommunication) {
        this.incomingCommunications.add(incomingCommunication);
        // Fire a notification
        this.setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_ADD_INCOMING_COMMUNICATION,
            incomingCommunication));
    }

    /**
     * Removes an outgoing communication of this active object and fires an
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag.ACTIVE_OBJECT_REMOVE_OUTGOING_COMMUNICATION}
     * WARNING ! This method MUST be called ONLY from
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication#disconnect()}
     * method.
     * 
     * @param outgoingCommunication
     *            The outgoing communication to remove
     */
    void internalRemoveOutgoingCommunication(final Communication outgoingCommunication) {
        // Fire a notification
        this.setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_REMOVE_OUTGOING_COMMUNICATION,
            outgoingCommunication));
        this.outgoingCommunications.remove(outgoingCommunication);
    }

    /**
     * Removes an incoming communication to this active object and fires an
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag.ACTIVE_OBJECT_REMOVE_INCOMING_COMMUNICATION}
     * WARNING ! This method MUST be called ONLY from
     * {@link org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication#disconnect()}
     * method.
     * 
     * @param incomingCommunication
     *            The incoming communication to remove
     */
    void internalRemoveIncomingCommunication(final Communication incomingCommunication) {
        // Fire a notification
        this.setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_REMOVE_INCOMING_COMMUNICATION,
            incomingCommunication));
        this.incomingCommunications.remove(incomingCommunication);
    }

    /**
     * The monitored active object adds a request, increments the request queue
     * length.
     */
    public void addRequest() {
        this.requestQueueLength++;
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED,
            requestQueueLength));
    }

    /**
     * The monitored active object removes a request, decrements the request
     * queue length.
     */
    public void removeRequest() {
        this.requestQueueLength--;
        setChanged();
        notifyObservers(new MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED,
            requestQueueLength));
        ;
    }

    public void setRequestQueueLength(int requestQueueLength) {
        if (this.requestQueueLength != requestQueueLength) {
            this.requestQueueLength = requestQueueLength;
            setChanged();
            notifyObservers(new MVCNotification(
                MVCNotificationTag.ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED, requestQueueLength));
            ;
        }
    }

    public String getJobId() {
        return this.getParent().getJobId();
    }

    //
    // -- ALL OVERRIDED METHOD FROM AbstractData class---------------
    //

    /**
     * Returns the name of this active object. (Example: ao#3)
     * 
     * @return The name of this active object.
     */
    @Override
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProActiveNodeObject getParent() {
        return this.parent;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getType() {
        return ActiveObject.ACTIVE_OBJECT_TYPE;
    }

    @Override
    public void explore() {
    }

    @Override
    public String toString() {
        return this.name;
    }

    //
    // -- STEPBYSTEP DEBUG METHODS -----------------------------------------------
    //
    public void enableStepByStep() {
        setChanged();
        proxyMBean.enableStepByStep();
    }

    public void disableStepByStep() {
        setChanged();
        proxyMBean.disableStepByStep();
    }

    public void nextStep() {
        proxyMBean.nextStep();
    }

    public void nextStep(long id) {
        proxyMBean.nextStep(id);
    }

    public void nextStep(Collection<Long> ids) {
        proxyMBean.nextStep(ids);
    }

    public DebugInfo getDebugInfo() {
        try {
            return (DebugInfo) getProActiveConnection().getAttribute(getObjectName(), "DebugInfo");
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void slowMotion(long slowMotionDelay) {
        setChanged();
        proxyMBean.slowMotion(slowMotionDelay);
    }

    public void initBreakpointTypes() {
        proxyMBean.initBreakpointTypes();
    }

    public void enableBreakpointTypes(BreakpointType[] types) {
        proxyMBean.enableBreakpointTypes(types);
    }

    public void disableBreakpointTypes(BreakpointType[] types) {
        proxyMBean.disableBreakpointTypes(types);
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    public static final class ActiveObjectComparator implements Comparator<String> {

        /**
         * Compare two active objects. (For Example: ao#3 and ao#5 give -1
         * because ao#3 has been discovered before ao#5.)
         * 
         * @return -1, 0, or 1 as the first argument is less than, equal to, or
         *         greater than the second.
         */
        public int compare(final String ao1Name, final String ao2Name) {
            return -(ao1Name.compareTo(ao2Name));
        }
    }
}
