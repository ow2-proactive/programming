/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.controller;

import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * The monitor controller interface. This controller manages the statistics for each methods exposed by the
 * component server interfaces. It's an optional controller.
 * <br>
 * The statistics of a given method are stored in a {@link MethodStatistics} object.
 * <br>
 * The implementation class org.objectweb.proactive.core.component.controller.MonitorControllerImpl provides in
 * immediate services the following methods: getStatistics(String itfName, String methodName),
 * getStatistics(String itfName, String methodName, Class<?>[] parametersTypes), getAllStatistics(). 
 * 
 * @author The ProActive Team
 *
 */
@PublicAPI
public interface MonitorController {
    /**
     * Check if the monitoring of the component is started.
     * 
     * @return True if the monitoring is started, false otherwise.
     */
    public BooleanWrapper isMonitoringStarted();

    /**
     * Start the monitoring of the component.
     */
    public void startMonitoring();

    /**
     * Stop the monitoring of the component.
     */
    public void stopMonitoring();

    /**
     * Reset the monitoring. All the previous data registered are erased.
     */
    public void resetMonitoring();

    /**
     * Get the statistics of a method exposed by a component server interface.
     * 
     * @param itfName Name of the server interface where the method is exposed.
     * @param methodName Name of the method.
     * @return MethodStatistics instance containing all the statistics of the desired methods.
     * @throws Exception If the method cannot be identified or found.
     */
    public MethodStatistics getStatistics(String itfName, String methodName) throws ProActiveRuntimeException;

    /**
     * Get the statistics of a method exposed by a component server interface.
     * 
     * @param itfName Name of the server interface where the method is exposed.
     * @param methodName Name of the method.
     * @param parametersTypes Types of the parameters of the method.
     * @return MethodStatistics instance containing all the statistics of the desired methods.
     * @throws Exception If the method cannot be identified or found.
     */
    public MethodStatistics getStatistics(String itfName, String methodName, Class<?>[] parametersTypes)
            throws ProActiveRuntimeException;

    /**
     * Get the statistics for each methods exposed by the component server interfaces.
     * Use the {@link MonitorControllerHelper#generateKey(String, String, Class[])}
     * method to retrieve desired method statistics.
     * 
     * @return All the statistics in a map structured like this
     *      Map<itfName-MethodName-ClassNameParam1-ClassNameParam2-..., MethodStatistics>.
     * @see MonitorControllerHelper#generateKey(String, String, Class[])
     */
    public Map<String, MethodStatistics> getAllStatistics();
}
