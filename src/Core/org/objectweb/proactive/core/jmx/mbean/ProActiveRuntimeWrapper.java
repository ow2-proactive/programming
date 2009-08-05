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
package org.objectweb.proactive.core.jmx.mbean;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.debug.dconnection.DebuggerConnection;
import org.objectweb.proactive.core.debug.dconnection.DebuggerInformation;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of a ProActiveRuntimeWrapper MBean
 * @author The ProActive Team
 */
public class ProActiveRuntimeWrapper extends NotificationBroadcasterSupport implements
        ProActiveRuntimeWrapperMBean, Serializable {

    /** JMX Logger */
    // private transient Logger logger = ProActiveLogger.getLogger(Loggers.JMX_MBEAN);
    private static final Logger notificationsLogger = ProActiveLogger.getLogger(Loggers.JMX_NOTIFICATION);

    /** ObjectName of this MBean */
    private ObjectName objectName;

    /** The ProActiveRuntime wrapped in this MBean */
    private ProActiveRuntime runtime;

    /** The url of the ProActiveRuntime */
    private String url;

    /** Used by the JMX notifications */
    private long counter = 0;

    public ProActiveRuntimeWrapper() {

        /* Empty Constructor required by JMX */
    }

    /**
     * Creates a new ProActiveRuntimeWrapper MBean, representing a ProActive Runtime.
     * @param runtime
     */
    public ProActiveRuntimeWrapper(ProActiveRuntime runtime) {
        this.runtime = runtime;
        this.url = runtime.getURL();
        this.objectName = FactoryName.createRuntimeObjectName(url);
    }

    public String getURL() {
        return this.url;
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }

    public void killRuntime() throws Exception {
        if (notificationsLogger.isDebugEnabled()) {
            notificationsLogger.debug("ProActiveRuntimeWrapper.killRuntime()");
        }
        runtime.killRT(true);
    }

    public List<ObjectName> getNodes() throws ProActiveException {
        final String[] nodeNames = this.runtime.getLocalNodeNames();

        final List<ObjectName> onames = new ArrayList<ObjectName>(nodeNames.length);

        for (final String nodeName : nodeNames) {
            onames.add(FactoryName.createNodeObjectName(this.url, nodeName));
        }
        return onames;
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getAllBodiesCount()
     */
    public int getAllBodiesCount() {
        return LocalBodyStore.getInstance().getLocalBodiesCount();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getInternalBodiesCount()
     */
    public int getInternalBodiesCount() {
        return this.getAllBodiesCount() - this.getUserBodiesCount();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getUserBodiesCount()
     */
    public int getUserBodiesCount() {
        // A query name on active object domain with a wild card
        final ObjectName activeObjectDomainName = FactoryName.createActiveObjectDomainName();
        // The size of the set will be the count of all user bodies since body wrappers are created only
        // if the reified object of a body does NOT implement {@link org.objectweb.proactive.ProActiveInternalObject}    	
        return ManagementFactory.getPlatformMBeanServer().queryMBeans(activeObjectDomainName, null).size();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getHalfBodiesCount()
     */
    public int getHalfBodiesCount() {
        return LocalBodyStore.getInstance().getLocalHalfBodiesCount();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getThreadCount()
     */
    public int getThreadCount() {
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getUsedHeapMemory()
     */
    public long getUsedHeapMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getLoadedClassCount()
     */
    public int getLoadedClassCount() {
        return ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getObjectPendingFinalizationCount()
     */
    public int getObjectPendingFinalizationCount() {
        return ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount();
    }

    public void sendNotification(String type) {
        this.sendNotification(type, null);
    }

    public void sendNotification(String type, Object userData) {
        ObjectName source = getObjectName();
        if (notificationsLogger.isDebugEnabled()) {
            notificationsLogger.debug("[" + type + "]#[ProActiveRuntimeWrapper.sendNotification] source=" +
                source + ", userData=" + userData);
        }

        Notification notification = new Notification(type, source, counter++);
        notification.setUserData(userData);
        this.sendNotification(notification);
    }

    public ProActiveSecurityManager getSecurityManager(Entity user) {
        try {
            return this.runtime.getProActiveSecurityManager(user);
        } catch (AccessControlException e) {
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setSecurityManager(Entity user, PolicyServer policyServer) {
        try {
            this.runtime.setProActiveSecurityManager(user, policyServer);
        } catch (AccessControlException e) {
            e.printStackTrace();
        } catch (SecurityNotAvailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //
    // -- DEBUGGERCONNECTION METHODS -----------------------------------------------
    //
    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getDebugInfo()
     */
    public DebuggerInformation getDebugInfo() {
        return DebuggerConnection.getDebuggerConnection().getDebugInfo();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#removeDebugger()
     */
    public void removeDebugger() {
        DebuggerConnection.getDebuggerConnection().removeDebugger();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#hasDebuggerConnected()
     */
    public boolean hasDebuggerConnected() {
        return DebuggerConnection.getDebuggerConnection().hasDebuggerConnected();
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#canBeDebugged()
     */
    public boolean canBeDebugged() {
        return System.getProperty("debugID") != null;
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#getLoggers()
     */
    @SuppressWarnings("unchecked")
    public List<String> getLoggers() {
        List<String> loggersList = new ArrayList<String>();
        LoggerRepository r = LogManager.getLoggerRepository();
        Enumeration<Logger> e = r.getCurrentLoggers();
        Logger logger = null;
        while (e.hasMoreElements()) {
            logger = e.nextElement();
            loggersList.add(logger.getName());
        }
        return loggersList;
    }

    /**
     * @see org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean#setLogLevel(java.lang.String, java.lang.String)
     */
    public void setLogLevel(String loggerName, String level) {
        Logger.getLogger(loggerName).setLevel(Level.toLevel(level));
    }

}
