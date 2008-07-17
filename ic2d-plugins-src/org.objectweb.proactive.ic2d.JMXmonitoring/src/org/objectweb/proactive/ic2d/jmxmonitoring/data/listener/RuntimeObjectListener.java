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
package org.objectweb.proactive.ic2d.jmxmonitoring.data.listener;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.notification.BodyNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;


/**
 * 
 * Listener for a RuntimeObject. Listens for notifications concerning a
 * ProActive Runtime and updates the IC2D model object representation of it. For
 * each IC2D representation a ProActive Runtime, a RuntimeObjectListener will be
 * created and subscribed to the </code>org.objectweb.proactive.core.jmx.util.JMXNotificationManager</code>
 * (singleton). Each time an event occur related to the ProActive Runtime, a
 * notification will be sent to this listener. This listener will update the
 * model representation of the Runtime which will send notification for its own
 * listener(s) (the edit part(s))
 * 
 * @author The ProActive Team
 * 
 */
public class RuntimeObjectListener implements NotificationListener {
    private final RuntimeObject runtimeObject;

    public RuntimeObjectListener(RuntimeObject runtimeObject) {
        this.runtimeObject = runtimeObject;
    }

    public void handleNotification(Notification notification, Object handback) {
        final String type = notification.getType();

        // In priority handle runtime destroyed notification EVEN IF this
        // runtime object is not monitored
        if (type.equals(NotificationType.runtimeDestroyed)) {
            runtimeObject.runtimeKilled();
            return;
        }

        // All other notification can be handle IF and ONLY IF this runtime
        // object is monitored
        if (!this.runtimeObject.isMonitored()) {
            return;
        }

        // This notification is emitted when a body is created on the listening
        // runtime
        if (type.equals(NotificationType.bodyCreated)) {
            // THE RETREIVAL OF NEW BODIES IS HANDLED BY AN AUTOMATIC REFRESH THREAD
            // so there is no point to handle it here see ProActiveNodeObject#explore() method

            // // First get the notification data
            // final BodyNotificationData notificationData =
            // (BodyNotificationData) notification
            // .getUserData();
            // // From the notification data get the node url where the body was
            // // created
            // final String nodeUrl = notificationData.getNodeUrl();
            // // Find the model of the node from the given url
            // final ProActiveNodeObject node = (ProActiveNodeObject)
            // this.runtimeObject
            // .getChild(nodeUrl);
            // if (node != null) {
            // // Once the correct node has been found
            // final UniqueID id = notificationData.getId();
            // System.out.println(notification.getTimeStamp()
            // + " Body Created " + notification.getSource()
            // + " of the body " + id.toString());
            // final String className = notificationData.getClassName();
            // // Add the active object to the node
            // node.addActiveObjectByID(id, className);
            // } else {
            // // If the node is unknown TODO : TREAT IT !
            // System.out
            // .println("RuntimeObjectListener.handleNotification() BODY CREATED
            // node pas trouve nodeUrl = "
            // + nodeUrl);
            // }
        } else if (type.equals(NotificationType.bodyDestroyed)) {
            // First get the notification data
            final BodyNotificationData notificationData = (BodyNotificationData) notification.getUserData();
            // Get the id of the active object
            final UniqueID id = notificationData.getId();
            // First find the concerned active object model
            final ActiveObject activeObject = this.runtimeObject.getWorldObject().findActiveObject(
                    id.getCanonString());
            // Check if is known and if is not migrating
            if (activeObject != null && !activeObject.isMigrating()) {
                activeObject.destroy();
            }
        } else if (type.equals(NotificationType.runtimeRegistered)) {
            this.runtimeObject.getParent().explore();
        } else if (type.equals(NotificationType.runtimeUnregistered)) {
            this.runtimeObject.getParent().explore();
        }
        // --- NodeEvent ----------------
        else if (type.equals(NotificationType.nodeCreated)) {
            this.runtimeObject.getParent().explore();
        } else if (type.equals(NotificationType.nodeDestroyed)) {
            this.runtimeObject.getParent().explore();
        } else {
            System.out.println(runtimeObject + " => " + type);
        }
    }
}
