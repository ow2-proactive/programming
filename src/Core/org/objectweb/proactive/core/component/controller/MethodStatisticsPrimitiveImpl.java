package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


public class MethodStatisticsPrimitiveImpl extends MethodStatisticsAbstract implements Serializable {

    public MethodStatisticsPrimitiveImpl(String itfName, String methodName, Class<?>[] parametersTypes) {
        this.itfName = itfName;
        this.methodName = methodName;
        this.parametersTypes = parametersTypes;
        this.requestsStats = Collections.synchronizedList(new ArrayList<RequestStatistics>());
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
        } else
            return 0;
    }

    public double getAverageServiceTime(long pastXMilliseconds) {
        return getAverageServiceTime(findNumberOfRequests(pastXMilliseconds, indexNextReply));
    }
}
