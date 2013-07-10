/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.core.component.control;

import java.io.Serializable;
import java.util.Vector;


public class MethodStatisticsPrimitiveImpl extends MethodStatisticsAbstract implements Serializable {
    public MethodStatisticsPrimitiveImpl(String itfName, String methodName, Class<?>[] parametersTypes) {
        this.itfName = itfName;
        this.methodName = methodName;
        this.parametersTypes = parametersTypes;
        this.requestsStats = new Vector<RequestStatistics>();
        reset();
    }

    public long getLatestServiceTime() {
        return requestsStats.get(indexNextReply - 1).getServiceTime() / 1000;
    }

    public double getAverageServiceTime() {
        return getAverageServiceTime(indexNextReply);
    }

    public double getAverageServiceTime(int lastNRequest) {
        if (lastNRequest != 0) {
            double res = 0;
            int indexToReach = Math.max(indexNextReply - 1 - lastNRequest, 0); // To avoid to have negative index
            for (int i = indexNextReply - 1; i >= indexToReach; i--) {
                res += requestsStats.get(i).getServiceTime();
            }

            return res / lastNRequest / 1000;
        } else {
            return 0;
        }
    }

    public double getAverageServiceTime(long pastXMilliseconds) {
        return getAverageServiceTime(findNumberOfRequests(pastXMilliseconds, indexNextReply));
    }
}
