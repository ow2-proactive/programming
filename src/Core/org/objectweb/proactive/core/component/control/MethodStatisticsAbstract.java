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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public abstract class MethodStatisticsAbstract implements MethodStatistics, Serializable {
    protected String itfName;

    protected String methodName;

    protected Class<?>[] parametersTypes;

    protected List<RequestStatistics> requestsStats;

    protected long startTime;

    protected int indexNextDepartureRequest;

    protected int indexNextReply;

    protected int currentLengthQueue;

    // Sometimes a replySent notification is received before the corresponding servingStarted notification
    // Therefore the notification is stored and will be used when the corresponding servingStarted notification will be received
    protected List<Long> replyInAdvance;

    /*
     * Reset all the statistics for the monitored method.
     */
    public void reset() {
        requestsStats.clear();
        startTime = System.nanoTime() / 1000;
        indexNextDepartureRequest = 0;
        indexNextReply = 0;
        currentLengthQueue = 0;
        this.replyInAdvance = new ArrayList<Long>();
    }

    /*
     * Notify the arrival of a new request in the incoming queue related to the monitored method.
     *
     * @param time Time of the arrival of the request in microseconds.
     */
    public void notifyArrivalOfRequest(long arrivalTime) {
        if (!requestsStats.isEmpty()) {
            requestsStats.add(new RequestStatistics(arrivalTime, requestsStats.get(requestsStats.size() - 1)
                    .getArrivalTime()));
        } else {
            requestsStats.add(new RequestStatistics(arrivalTime, startTime));
        }
        currentLengthQueue++;
    }

    /*
     * Notify the departure of a request in the incoming queue related to the monitored method.
     *
     * @param time Time of the departure of the request in microseconds.
     */
    public void notifyDepartureOfRequest(long departureTime) {
        try {
            requestsStats.get(indexNextDepartureRequest).setDepartureTime(departureTime);
            indexNextDepartureRequest++;
            currentLengthQueue--;
            if (!replyInAdvance.isEmpty() && (replyInAdvance.get(0) > departureTime)) {
                notifyReplyOfRequestSent(replyInAdvance.remove(0));
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /*
     * Erase the maxNbRequests oldest requests.
     */
    private void clean() {
        int shift = requestsStats.size() - maxNbRequests;
        if (shift > 0) {
            requestsStats.subList(shift, requestsStats.size()).clear();
            startTime = requestsStats.get(0).getArrivalTime();
            indexNextDepartureRequest = indexNextDepartureRequest - shift;
            if (indexNextDepartureRequest < 0) {
                indexNextDepartureRequest = 0;
            }
            indexNextReply = indexNextReply - shift;
            if (indexNextReply < 0) {
                indexNextReply = 0;
            }
            currentLengthQueue = requestsStats.size() - indexNextDepartureRequest;
        }
    }

    /*
     * Notify that the reply to a request related to the monitored method has been sent.
     *
     * @param time Time of the reply to a request has been sent in microseconds.
     */
    public void notifyReplyOfRequestSent(long replyTime) {
        try {
            if (indexNextReply < indexNextDepartureRequest) {
                requestsStats.get(indexNextReply).setReplyTime(replyTime);
                indexNextReply++;
                if (indexNextReply == (2 * maxNbRequests)) {
                    clean();
                }
            } else {
                replyInAdvance.add(replyTime);
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    protected int findNumberOfRequests(long time, int indexToStart) {
        long currentTime = System.nanoTime() / 1000;
        for (int i = indexToStart - 1; i >= 0; i--) {
            if (((currentTime - requestsStats.get(i).getArrivalTime()) / 1000) > time) {
                return indexToStart - (i + 1);
            }
        }

        return indexToStart;
    }

    public int getCurrentLengthQueue() {
        return currentLengthQueue;
    }

    public double getAverageLengthQueue() {
        return getAverageLengthQueue((System.nanoTime() / 1000 - startTime) / 1000);
    }

    public double getAverageLengthQueue(long pastXMilliseconds) {
        // TODO Is the correct definition of the average length of the queue?
        return findNumberOfRequests(pastXMilliseconds, requestsStats.size()) /
            (((double) pastXMilliseconds) / 1000);
    }

    public long getLatestInterArrivalTime() {
        return requestsStats.get(requestsStats.size() - 1).getInterArrivalTime() / 1000;
    }

    public double getAverageInterArrivalTime() {
        return getAverageInterArrivalTime(requestsStats.size());
    }

    public double getAverageInterArrivalTime(int lastNRequest) {
        if (lastNRequest != 0) {
            double res = 0;
            int indexToReach = Math.max(requestsStats.size() - 1 - lastNRequest, 0); // To avoid to have negative index
            for (int i = requestsStats.size() - 1; i >= indexToReach; i--) {
                res += requestsStats.get(i).getInterArrivalTime();
            }

            return res / lastNRequest / 1000;
        } else {
            return 0;
        }
    }

    public double getAverageInterArrivalTime(long pastXMilliseconds) {
        return getAverageInterArrivalTime(findNumberOfRequests(pastXMilliseconds, requestsStats.size()));
    }

    public double getAveragePermanenceTimeInQueue() {
        return getAveragePermanenceTimeInQueue(indexNextDepartureRequest);
    }

    public double getAveragePermanenceTimeInQueue(int lastNRequest) {
        if (lastNRequest != 0) {
            double res = 0;
            int indexToReach = Math.max(indexNextDepartureRequest - 1 - lastNRequest, 0); // To avoid to have negative index
            for (int i = indexNextDepartureRequest - 1; i >= indexToReach; i--) {
                res += requestsStats.get(i).getPermanenceTimeInQueue();
            }

            return res / lastNRequest / 1000;
        } else {
            return 0;
        }
    }

    public double getAveragePermanenceTimeInQueue(long pastXMilliseconds) {
        return getAveragePermanenceTimeInQueue(findNumberOfRequests(pastXMilliseconds,
                indexNextDepartureRequest));
    }

    public List<String> getInvokedMethodList() {
        // TODO Complete this method
        return null;
    }

    public String toString() {
        String res = "Average statistics for the method " + methodName + "(";
        int nbParameters = parametersTypes.length;
        for (int i = 0; i < nbParameters; i++) {
            res += parametersTypes[i].getName();
            if (i + 1 < nbParameters) {
                res += ", ";
            }
        }
        res += ") of the interface " + itfName + ":\n";
        res += "Average length of the queue: " + getAverageLengthQueue() + "\n";
        res += "Average service time: " + getAverageServiceTime() + "\n";
        res += "Average inter-arrival time: " + getAverageInterArrivalTime() + "\n";
        res += "Average permanence time in queue: " + getAveragePermanenceTimeInQueue() + "\n";

        return res;
    }

    class RequestStatistics implements Serializable {
        private long arrivalTime;

        private long departureTime;

        private long replyTime;

        private long interArrivalTime;

        private long serviceTime;

        private long permanenceTimeInQueue;

        public RequestStatistics(long arrivalTime, long prevArrivalTime) {
            this.arrivalTime = arrivalTime;
            this.departureTime = 0;
            this.replyTime = 0;
            this.interArrivalTime = arrivalTime - prevArrivalTime;
            this.serviceTime = 0;
            this.permanenceTimeInQueue = 0;
        }

        public long getArrivalTime() {
            return arrivalTime;
        }

        public void setDepartureTime(long departureTime) {
            this.departureTime = departureTime;
            this.permanenceTimeInQueue = this.departureTime - this.arrivalTime;
        }

        public long getDepartureTime() {
            return departureTime;
        }

        public void setReplyTime(long replyTime) {
            this.replyTime = replyTime;
            this.serviceTime = this.replyTime - this.departureTime;
        }

        public long getReplyTime() {
            return replyTime;
        }

        public long getInterArrivalTime() {
            return interArrivalTime;
        }

        public long getServiceTime() {
            return serviceTime;
        }

        public long getPermanenceTimeInQueue() {
            return permanenceTimeInQueue;
        }
    }
}
