package org.objectweb.proactive.core.component.controller;

import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * The monitor controller interface. This controller manages the statistics for each methods exposed by the
 * component server interfaces. It's an optional controller.
 * <br>
 * The statistics of a given method are stored in a {@link org.objectweb.proactive.core.component.controller.MethodStatistics}
 * object.
 * <br>
 * The implementation class org.objectweb.proactive.core.component.controller.MonitorControllerImpl provides in
 * immediate services the following methods: getStatistics(String itfName, String methodName),
 * getStatistics(String itfName, String methodName, Class<?>[] parametersTypes), getAllStatistics(). 
 * 
 * @author The ProActive Team
 * @see org.objectweb.proactive.core.component.controller.MonitorControllerImpl
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
     * Use the {@link  org.objectweb.proactive.core.component.controller.MonitorControllerHelper.generateKey}
     * method to retrieve desired method statistics.
     * 
     * @return All the statistics in a map structured like this
     *      Map<itfName-MethodName-ClassNameParam1-ClassNameParam2-..., MethodStatistics>.
     * @see org.objectweb.proactive.core.component.controller.MonitorControllerHelper.generateKey
     */
    public Map<String, MethodStatistics> getAllStatistics();
}
