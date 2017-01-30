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
package org.objectweb.proactive.core.jmx.mbean;

import java.io.Serializable;
import java.util.List;

import javax.management.ObjectName;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.notification.NotificationType;


/**
 * This MBean represents a ProActiveRuntime.
 * Since the ProActiveRuntime is an abstraction of a Java virtual machine it may aggregate some
 * information from various MXBeans like {@link java.lang.management.ThreadMXBean} or {@link java.lang.management.MemoryMXBean}. 
 * 
 * @author The ProActive Team
 */
public interface ProActiveRuntimeWrapperMBean extends Serializable {

    /**
     * Returns the url of the ProActiveRuntime associated.
     * @return The url of the ProActiveRuntime associated.
     */
    public String getURL();

    /**
     * Returns a list of Object Name used by the MBeans of the nodes containing in the ProActive Runtime.
     * @return a list of Object Name used by the MBeans of the nodes containing in the ProActive Runtime.
     * @throws ProActiveException
     */
    public List<ObjectName> getNodes() throws ProActiveException;

    /**
     * Returns the number of all bodies registered on this runtime.
     * @return the number of all bodies registered on this runtime
     */
    public int getAllBodiesCount();

    /**
     * Returns the number of internal bodies registered on this runtime.
     * <p>
     * A body is considered internal if its reified object implements {@link org.objectweb.proactive.ProActiveInternalObject}.
     * 
     * @return the number of internal bodies registered on this runtime
     */
    public int getInternalBodiesCount();

    /**
     * Returns the number of user bodies registered on this runtime.
     * @return the number of user bodies registered on this runtime
     */
    public int getUserBodiesCount();

    /**
     * Returns the number of half-bodies registered on this runtime.
     * @return the number of half-bodies registered on this runtime.
     */
    public int getHalfBodiesCount();

    /**
     * Returns the current number of live threads including both 
     * daemon and non-daemon threads. This method calls {@link java.lang.management.ThreadMXBean#getThreadCount()}.     
     * @see java.lang.management.ThreadMXBean
     * @return the current number of live threads.
     */
    public int getThreadCount();

    /**
     * Returns the amount of used memory in bytes. This method calls
     * {@link java.lang.management.MemoryMXBean#getHeapMemoryUsage()} to get the used value from the snapshot of memory usage. 
     * @see java.lang.management.MemoryMXBean    
     * @return the amount of used memory in bytes.
     */
    public long getUsedHeapMemory();

    /**
     * Returns the number of classes that are currently loaded in the Java virtual machine.
     * This method calls {@link java.lang.management.ClassLoadingMXBean#getLoadedClassCount()}.
     * @see java.lang.management.ClassLoadingMXBean
     * @return the number of currently loaded classes.
     */
    public int getLoadedClassCount();

    /**
     * Returns the approximate number of objects for which finalization is pending.
     * This method calls {@link java.lang.management.MemoryMXBean#getObjectPendingFinalizationCount()}.
     * @see java.lang.management.MemoryMXBean
     * @return the approximate number objects for which finalization is pending.
     */
    public int getObjectPendingFinalizationCount();

    /**
     * Sends a new notification.
     * @param type The type of the notification. See {@link NotificationType}
     */
    public void sendNotification(String type);

    /**
     * Sends a new notification.
     * @param type Type of the notification. See {@link NotificationType}
     * @param userData The user data.
     */
    public void sendNotification(String type, Object userData);

    /**
     * Returns the object name used for this MBean.
     * @return The object name used for this MBean.
     */
    public ObjectName getObjectName();

    /**
     * Kills this ProActiveRuntime.
     * @exception Exception if a problem occurs when killing this ProActiveRuntime
     */
    public void killRuntime() throws Exception;
}
