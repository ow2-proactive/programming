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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collection;
import java.util.Map;

import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.jmxmonitoring.finder.RemoteObjectHostRTFinder;
import org.objectweb.proactive.ic2d.jmxmonitoring.finder.RuntimeFinder;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


/**
 * Holder class for the host data representation.
 */
public final class HostObject extends AbstractData<WorldObject, RuntimeObject> {
    private final static String DEFAULT_NAME = "undefined";

    /**
     * The parent world object
     */
    private final WorldObject parent;
    /**
     * The hostname of this host
     */
    private final String hostName;
    /**
     * The port used to communicate with this host
     */
    private final int port;
    /**
     * The protocol used to communicate with this host
     */
    private final String protocol;
    /**
     * The url of this host
     */
    private final String url;
    /**
     * The rank of this host
     */
    private final int rank;
    /**
     * This variable is used to avoid blocking calls to explore method ie lock-free call to explore method
     */
    private volatile boolean isExploring = false;

    //Warning: Don't use this variable directly, use getOSName().
    private String osName;

    //Warning: Don't use this variable directly, use getOSVersion().
    private String osVersion;

    /**
     * Creates a new HostObject. Use the method addHost(String url) of the WorldObject class instead.	
     * @param parent The parent world object
     * @param url The url of this host
     * @param rank The rank of this host
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     */
    protected HostObject(WorldObject parent, String url, int rank) throws MalformedObjectNameException,
            NullPointerException {
        super(new ObjectName(FactoryName.HOST));

        this.parent = parent;
        final String name = URIBuilder.getNameFromURI(url);
        this.hostName = URIBuilder.getHostNameFromUrl(url);
        this.port = URIBuilder.getPortNumber(url);
        this.protocol = URIBuilder.getProtocol(url);
        this.url = URIBuilder.buildURI(hostName, name, protocol, port).toString();
        this.rank = rank;
    }

    @Override
    public WorldObject getParent() {
        return this.parent;
    }

    @Override
    public String getKey() {
        return this.url;
    }

    @Override
    public String getType() {
        return "host";
    }

    @Override
    public String getName() {
        return getHostName();
    }

    /**
     * Returns the url of this object.
     * @return An url.
     */
    public String getUrl() {
        return this.url;
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getPort() {
        return this.port;
    }

    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Returns the operating system name.
     * @return The operating system name.
     */
    public String getOSName() {
        if (this.osName == null) {
            return DEFAULT_NAME;
        } else {
            return this.osName;
        }
    }

    /**
     * Returns the operating system version.
     * @return The operating system version.
     */
    public String getOSVersion() {
        if (osVersion == null) {
            return DEFAULT_NAME;
        } else {
            return this.osVersion;
        }
    }

    @Override
    public void explore() {
        // If this host is not monitored or already being explored then return silently
        if (!super.isMonitored || this.isExploring) {
            return;
        }
        this.isExploring = true;
        this.refreshRuntimes();
        this.isExploring = false;
    }

    @Override
    public int getHostRank() {
        return this.rank;
    }

    /**
     * Updates the set of IC2D's RuntimeObjects so that it is in sync with the ProActive Runtimes on the monitored Host.
     * The update is performed by comparing the existing RuntimeObjects with the set of ProActive Runtimes
     * returned from <code> RemoteObjectHostRTFinder.getRuntimeObjects(HostObject host) </code>
     *
     */
    private void refreshRuntimes() {
        RuntimeFinder rfinder = RemoteObjectHostRTFinder.getInstance();
        Collection<RuntimeObject> runtimeObjects = rfinder.getRuntimeObjects(this);

        Map<String, RuntimeObject> childrenToRemoved = this.getMonitoredChildrenAsMap();

        for (final RuntimeObject runtimeObject : runtimeObjects) {
            RuntimeObject child = (RuntimeObject) this.getChild(runtimeObject.getKey());

            // If this child is not yet monitored.
            if (child == null) {
                child = runtimeObject;
                addChild(runtimeObject);
                updateOSNameAndVersion(runtimeObject.getProActiveConnection());
            }
            // This child is already monitored, but this child maybe contains some not monitored objects.
            child.explore();

            // Removes from the model the not monitored or terminated runtimes.
            childrenToRemoved.remove(child.getKey());
        }

        // Some child have to be removed
        for (final RuntimeObject data : childrenToRemoved.values()) {
            ((RuntimeObject) data).destroy();
        }
    }

    private void updateOSNameAndVersion(ProActiveConnection connection) {
        if ((this.osName == null) || (this.osVersion == null)) {
            ObjectName OSoname = null;
            try {
                OSoname = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            OperatingSystemMXBean proxyMXBean = (OperatingSystemMXBean) MBeanServerInvocationHandler
                    .newProxyInstance(connection, OSoname, OperatingSystemMXBean.class, false);
            this.osName = proxyMXBean.getName();
            this.osVersion = proxyMXBean.getVersion();

            setChanged();
            notifyObservers(new MVCNotification(MVCNotificationTag.HOST_OBJECT_UPDATED_OSNAME_AND_VERSON,
                this.toString()));
        }
    }

    @Override
    public ProActiveConnection getProActiveConnection() {
        // A host object has no JMX ProActiveConnection
        return null;
    }

    @Override
    public synchronized void addChild(RuntimeObject child) {
        if (child instanceof RuntimeObject) {
            RuntimeObject runtimeObject = (RuntimeObject) child;
            if (runtimeObject.subscribeListener()) {
                super.addChild(child);
            }
        }
    }

    @Override
    public String toString() {
        String result = this.hostName + ":" + this.port;
        if (!getOSName().equals(DEFAULT_NAME)) {
            result += (":" + getOSName());
        }
        if (!getOSVersion().equals(DEFAULT_NAME)) {
            result += ("(OS version: " + getOSVersion() + ")");
        }
        return result;
    }
}
