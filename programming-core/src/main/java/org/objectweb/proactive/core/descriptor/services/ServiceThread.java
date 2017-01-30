/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.descriptor.services;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RuntimeNotificationData;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class ServiceThread extends Thread {
    private VirtualNodeInternal vn;

    private UniversalService service;

    private VirtualMachine vm;

    private ProActiveRuntime localRuntime;

    int nodeCount = 0;

    long timeout = 0;

    int nodeRequested;

    public static Logger loggerDeployment = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);

    private long expirationTime;

    public ServiceThread(VirtualNodeInternal vn, VirtualMachine vm) {
        this.vn = vn;
        this.service = vm.getService();
        this.vm = vm;
        this.localRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Override
    public void run() {
        ProActiveRuntime[] part = null;

        try {
            part = service.startService();
            nodeCount = nodeCount + ((part != null) ? part.length : 0);
            if (part != null) {
                notifyVirtualNode(part);
            }
        } catch (ProActiveException e) {
            loggerDeployment.error("An exception occured while starting the service " + service.getServiceName() +
                                   " for the VirtualNode " + vn.getName() + " \n" + e.getMessage());
        }
    }

    public void notifyVirtualNode(ProActiveRuntime[] part) {
        for (int i = 0; i < part.length; i++) {
            String url = part[i].getURL();
            String protocol = URIBuilder.getProtocol(url);

            // JMX Notification
            ProActiveRuntimeWrapperMBean mbean = ProActiveRuntimeImpl.getProActiveRuntime().getMBean();
            if (mbean != null) {
                RuntimeNotificationData notificationData = new RuntimeNotificationData(vn.getName(),
                                                                                       url,
                                                                                       protocol,
                                                                                       vm.getName());
                mbean.sendNotification(NotificationType.runtimeAcquired, notificationData);
            }

            // END JMX Notification
        }
    }
}
