/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;


public class MethodStatisticsCompositeImpl extends MethodStatisticsAbstract implements Serializable {
    private List<MonitorController> subcomponentMonitors;

    public MethodStatisticsCompositeImpl(String itfName, String methodName, Class<?>[] parametersTypes,
            List<MonitorController> subcomponentMonitors) {
        this.itfName = itfName;
        this.methodName = methodName;
        this.parametersTypes = parametersTypes;
        this.subcomponentMonitors = subcomponentMonitors;
        this.requestsStats = new Vector<RequestStatistics>();
        reset();
    }

    public long getLatestServiceTime() {
        long latestServiceTime = 0;
        for (int i = 0; i < subcomponentMonitors.size(); i++) {
            latestServiceTime = Math.max(latestServiceTime, subcomponentMonitors.get(i).getStatistics(
                    itfName, methodName, parametersTypes).getLatestServiceTime());
        }
        return latestServiceTime;
    }

    public double getAverageServiceTime() {
        double averageServiceTime = 0;
        for (int i = 0; i < subcomponentMonitors.size(); i++) {
            averageServiceTime = Math.max(averageServiceTime, subcomponentMonitors.get(i).getStatistics(
                    itfName, methodName, parametersTypes).getAverageServiceTime());
        }
        return averageServiceTime;
    }

    public double getAverageServiceTime(int lastNRequest) {
        if (lastNRequest != 0) {
            double averageServiceTime = 0;
            for (int i = 0; i < subcomponentMonitors.size(); i++) {
                averageServiceTime = Math.max(averageServiceTime, subcomponentMonitors.get(i).getStatistics(
                        itfName, methodName, parametersTypes).getAverageServiceTime(lastNRequest));
            }
            return averageServiceTime;
        } else {
            return 0;
        }
    }

    public double getAverageServiceTime(long pastXMilliseconds) {
        double averageServiceTime = 0;
        for (int i = 0; i < subcomponentMonitors.size(); i++) {
            averageServiceTime = Math.max(averageServiceTime, subcomponentMonitors.get(i).getStatistics(
                    itfName, methodName, parametersTypes).getAverageServiceTime(pastXMilliseconds));
        }
        return averageServiceTime;
    }
}