/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.control;

import java.util.List;

import org.etsi.uri.gcm.api.control.MonitorController;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This class manages the statistics for a method exposed by a component server interface.
 * <br>
 * The MethodStatistics instances of each method are managed by the monitor controller.
 *
 * @author The ProActive Team
 * @see MonitorController
 *
 */
@PublicAPI
public interface MethodStatistics {
    /**
     * Maximum number of requests saved.
     */
    public static int maxNbRequests = 1000;

    //@snippet-start methodstatistics
    /**
     * Get the current length of the requests incoming queue related to the monitored method.
     *
     * @return The current number of pending request in the queue.
     */
    public int getCurrentLengthQueue();

    /**
     * Get the average number of requests incoming queue per second related to the monitored
     * method since the monitoring has been started.
     *
     * @return The average number of requests per second.
     */
    public double getAverageLengthQueue();

    /**
     * Get the average number of requests incoming queue per second related to the monitored
     * method in the last past X milliseconds.
     *
     * @param pastXMilliseconds The last past X milliseconds.
     * @return The average number of requests per second.
     */
    public double getAverageLengthQueue(long pastXMilliseconds);

    /**
     * Get the latest service time for the monitored method.
     *
     * @return The latest service time in milliseconds.
     */
    public long getLatestServiceTime();

    /**
     * Get the average service time for the monitored method since the monitoring has been started.
     *
     * @return The average service time in milliseconds.
     */
    public double getAverageServiceTime();

    /**
     * Get the average service time for the monitored method during the last N method calls.
     *
     * @param lastNRequest The last N method calls.
     * @return The average service time in milliseconds.
     */
    public double getAverageServiceTime(int lastNRequest);

    /**
     * Get the average service time for the monitored method in the last past X milliseconds.
     *
     * @param pastXMilliseconds The last past X milliseconds.
     * @return The average service time in milliseconds.
     */
    public double getAverageServiceTime(long pastXMilliseconds);

    /**
     * Get the latest inter-arrival time for the monitored method.
     *
     * @return The latest inter-arrival time in milliseconds.
     */
    public long getLatestInterArrivalTime();

    /**
     * Get the average inter-arrival time for the monitored method since the monitoring has
     * been started.
     *
     * @return The average inter-arrival time in milliseconds.
     */
    public double getAverageInterArrivalTime();

    /**
     * Get the average inter-arrival time for the monitored method during the last
     * N method calls.
     *
     * @param lastNRequest The last N method calls.
     * @return The average inter-arrival time in milliseconds.
     */
    public double getAverageInterArrivalTime(int lastNRequest);

    /**
     * Get the average inter-arrival time for the monitored method in the last past X
     * milliseconds.
     *
     * @param pastXMilliseconds The last past X milliseconds.
     * @return The average inter-arrival time in milliseconds.
     */
    public double getAverageInterArrivalTime(long pastXMilliseconds);

    /**
     * Get the average permanence time in the incoming queue for a request of the monitored method
     * since the monitoring has been started.
     *
     * @return The average permanence time in the incoming queue in milliseconds.
     */
    public double getAveragePermanenceTimeInQueue();

    /**
     * Get the average permanence time in the incoming queue for a request of the monitored method
     * during the last N method calls.
     *
     * @param lastNRequest The last N method calls.
     * @return The average permanence time in the incoming queue in milliseconds.
     */
    public double getAveragePermanenceTimeInQueue(int lastNRequest);

    /**
     * Get the average permanence time in the incoming queue for a request of the monitored method
     * in the last past X milliseconds.
     *
     * @param pastXMilliseconds The last past X milliseconds.
     * @return The average permanence time in the incoming queue in milliseconds.
     */
    public double getAveragePermanenceTimeInQueue(long pastXMilliseconds);

    /**
      * Get the list of all the method calls (server interfaces) invoked by a given invocation.
      *
      * @return The list of the used interfaces.
      * TODO which kind of information do you need (Interface reference, name, ...?)
      */
    public List<String> getInvokedMethodList();
    //@snippet-end methodstatistics

    /*
     * The fourth information "the list of all the method calls (server interfaces) invoked by a
     * given invocation" will be provided later with the DSO as described in Pisa. But, you can
     * already dependencies
     */
}
