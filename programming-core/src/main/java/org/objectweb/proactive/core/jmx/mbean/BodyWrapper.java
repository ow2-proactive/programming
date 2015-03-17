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
package org.objectweb.proactive.core.jmx.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of a BodyWrapperMBean.
 * <p>
 * Such wrapper is NOT created if the reified object of a body implements
 * {@link org.objectweb.proactive.ProActiveInternalObject}.
 *
 * @author The ProActive Team
 */
public class BodyWrapper extends NotificationBroadcasterSupport implements Serializable, BodyWrapperMBean {

    private static final long serialVersionUID = 61L;

    /**
     * The name of the attribute used to know if the reified object implements
     * {@link java.io.Serializable}
     */
    public static final String IS_REIFIED_OBJECT_SERIALIZABLE_ATTRIBUTE_NAME = "IsReifiedObjectSerializable";

    /** JMX Logger */
    private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
    private transient Logger notificationsLogger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

    /** ObjectName of this MBean */
    private transient ObjectName objectName;

    /** Unique id of the active object */
    private UniqueID id;

    /** The body wrapped in this MBean */
    private AbstractBody body;

    /** The url of node containing this active object */
    private transient String nodeUrl;

    /** The name of the body of the active object */
    private String bodyName;

    /**
     * A <code>Boolean</code> attribute used to know if the reified object
     * implements {@link java.io.Serializable}
     */
    private boolean isReifiedObjectSerializable;

    // -- JMX Datas --

    /** Timeout between updates */
    private long updateFrequence = 300;

    /** Used by the JMX notifications */
    private long counter = 1;

    /**
     * A list of jmx notifications. The current MBean sends a list of
     * notifications in order to not overload the network
     */
    private transient ConcurrentLinkedQueue<Notification> notifications;

    /**
     * Empty constructor required by JMX
     */
    public BodyWrapper() {
        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new BodyWrapper MBean, representing an active object.
     *
     * @param oname
     *            The JMX name of this wrapper
     * @param body
     *            The wrapped active object's body
     */
    public BodyWrapper(ObjectName oname, AbstractBody body) {
        this.objectName = oname;
        this.id = body.getID();
        this.body = body;
        this.nodeUrl = body.getNodeURL();
        this.isReifiedObjectSerializable = body.getReifiedObject() instanceof Serializable;
        this.notifications = new ConcurrentLinkedQueue<Notification>();
        this.launchNotificationsThread();
    }

    public UniqueID getID() {
        return this.id;
    }

    public String getName() {
        if (this.bodyName == null) {
            this.bodyName = this.body.getName();
        }
        return this.bodyName;
    }

    public ObjectName getObjectName() {
        if (this.objectName == null) {
            this.objectName = FactoryName.createActiveObjectName(this.id);
        }
        return this.objectName;
    }

    public String getNodeUrl() {
        return this.nodeUrl;
    }

    public void sendNotification(String type) {
        this.sendNotification(type, null);
    }

    public void sendNotification(String type, Object userData) {
        ObjectName source = getObjectName();
        if (notificationsLogger.isDebugEnabled()) {
            notificationsLogger.debug("[" + type + "]#[BodyWrapper.sendNotification] source=" + source +
                ", userData=" + userData);
        }

        Notification notification = new Notification(type, source, counter++, System.nanoTime() / 1000); // timeStamp in microseconds
        notification.setUserData(userData);
        // If the migration is finished, we need to inform the
        // JMXNotificationManager
        if (type.equals(NotificationType.migrationFinished)) {
            sendNotifications();
            notifications.add(notification);
            sendNotifications(NotificationType.migrationMessage);
        } else {
            notifications.add(notification);
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------------
    //

    /**
     * Creates a new thread which sends JMX notifications. A BodyWrapperMBean
     * keeps all the notifications, and the NotificationsThread sends every
     * 'updateFrequence' a list of notifications.
     */
    private void launchNotificationsThread() {
        Thread t = new Thread("JMXNotificationThread for " + BodyWrapper.this.objectName) {
            @Override
            public void run() {
                // first we wait for the creation of the body
                while (!BodyWrapper.this.body.isActive() && BodyWrapper.this.body.isAlive()) {
                    try {
                        Thread.sleep(updateFrequence);
                    } catch (InterruptedException e) {
                        logger.error("The JMX notifications sender thread was interrupted", e);
                    }
                }

                // and once the body is activated, we can forward the
                // notifications
                while (BodyWrapper.this.body.isActive()) {
                    try {
                        Thread.sleep(updateFrequence);
                        sendNotifications();
                    } catch (InterruptedException e) {
                        logger.error("The JMX notifications sender thread was interrupted", e);
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * Sends a notification containing all stored notifications.
     */
    private void sendNotifications() {
        this.sendNotifications(null);
    }

    /**
     * Sends a notification containing all stored notifications.
     *
     * @param userMessage
     *            The message to send with the set of notifications.
     */
    private void sendNotifications(String userMessage) {
        if (this.notifications == null) {
            this.notifications = new ConcurrentLinkedQueue<Notification>();
        }

        // not sure if the synchronize is needed here, let's see ...
        // synchronized (notifications) {
        if (!this.notifications.isEmpty()) {
            ObjectName source = getObjectName();
            Notification n = new Notification(NotificationType.setOfNotifications, source, counter++,
                userMessage);
            n.setUserData(this.notifications);
            super.sendNotification(n);
            this.notifications.clear();
            // }
        }
    }

    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("[Serialisation.writeObject]#Serialization of the MBean :" + objectName);
        }

        // Send the notifications before migrates.
        if (!this.notifications.isEmpty()) {
            sendNotifications();
        }

        // Unregister the MBean from the MBean Server.
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs.isRegistered(this.objectName)) {
            try {
                mbs.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                logger.error("The objectName " + objectName +
                    " was not found during the serialization of the MBean", e);
            } catch (MBeanRegistrationException e) {
                logger.error("The MBean " + objectName +
                    " can't be unregistered from the MBean server during the serialization of the MBean", e);
            }
        }

        // Default Serialization
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        // Warning loggers is transient
        logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
        notificationsLogger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

        if ((logger != null) && logger.isDebugEnabled()) {
            logger.debug("[Serialisation.readObject]#Deserialization of the MBean");
        }

        in.defaultReadObject();

        // Warning objectName is transient
        this.objectName = FactoryName.createActiveObjectName(this.id);

        // Warning nodeUrl is transient
        // We get the url of the new node.
        this.nodeUrl = this.body.getNodeURL();
        logger.debug("BodyWrapper.readObject() nodeUrl=" + nodeUrl);

        // Warning notifications is transient
        this.notifications = new ConcurrentLinkedQueue<Notification>();

        // Register the MBean into the MBean Server
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
        } catch (InstanceAlreadyExistsException e) {
            logger.error("A Mean is already registered with this objectName " + objectName, e);
        } catch (MBeanRegistrationException e) {
            logger.error("The MBean " + objectName +
                " can't be registered on the MBean server during the deserialization of the MBean", e);
        } catch (NotCompliantMBeanException e) {
            logger.error("Exception throws during the deserialization of the MBean", e);
        }

        launchNotificationsThread();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean#getIsReifiedObjectSerializable()
     */
    public boolean getIsReifiedObjectSerializable() {
        return this.isReifiedObjectSerializable;
    }
}
