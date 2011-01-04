/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.jmx.listeners;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * @author The ProActive Team
 *
 */
public class ListenerAdapter implements NotificationListener {
    private Logger JMX_NOTIFICATION = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);
    private NotificationListener listener;
    private transient MBeanServer mbs;
    private ObjectName name;

    /**
     *
     * @param listener
     */
    public ListenerAdapter(NotificationListener listener, MBeanServer mbs, ObjectName name) {
        this.listener = listener;
        this.mbs = mbs;
        this.name = name;
    }

    /**
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification notification, Object handback) {
        try {
            this.listener.handleNotification(notification, handback);
        } catch (Exception e) {
            JMX_NOTIFICATION.debug("an exception occured (" + e.getMessage() +
                ") while sending the notification -- removing the listener");
            try {
                mbs.removeNotificationListener(name, this);
            } catch (InstanceNotFoundException e1) {
                e1.printStackTrace();
            } catch (ListenerNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }
}
