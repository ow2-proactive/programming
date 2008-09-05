package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MethodStatisticsCompositeImpl extends MethodStatisticsAbstract implements Serializable {
    private List<MonitorController> subcomponentMonitors;

    public MethodStatisticsCompositeImpl(String itfName, String methodName, Class<?>[] parametersTypes,
            List<MonitorController> subcomponentMonitors) {
        this.itfName = itfName;
        this.methodName = methodName;
        this.parametersTypes = parametersTypes;
        this.subcomponentMonitors = subcomponentMonitors;
        this.requestsStats = Collections.synchronizedList(new ArrayList<RequestStatistics>());
        reset();
    }

    public long getLatestServiceTime() {
        long latestServiceTime = 0;
        for (int i = 0; i < subcomponentMonitors.size(); i++)
            latestServiceTime = Math.max(latestServiceTime, subcomponentMonitors.get(i).getStatistics(
                    itfName, methodName, parametersTypes).getLatestServiceTime());
        return latestServiceTime;
    }

    public double getAverageServiceTime() {
        double averageServiceTime = 0;
        for (int i = 0; i < subcomponentMonitors.size(); i++)
            averageServiceTime = Math.max(averageServiceTime, subcomponentMonitors.get(i).getStatistics(
                    itfName, methodName, parametersTypes).getAverageServiceTime());
        return averageServiceTime;
    }

    public double getAverageServiceTime(int lastNRequest) {
        if (lastNRequest != 0) {
            double averageServiceTime = 0;
            for (int i = 0; i < subcomponentMonitors.size(); i++)
                averageServiceTime = Math.max(averageServiceTime, subcomponentMonitors.get(i).getStatistics(
                        itfName, methodName, parametersTypes).getAverageServiceTime(lastNRequest));
            return averageServiceTime;
        } else
            return 0;
    }

    public double getAverageServiceTime(long pastXMilliseconds) {
        double averageServiceTime = 0;
        for (int i = 0; i < subcomponentMonitors.size(); i++)
            averageServiceTime = Math.max(averageServiceTime, subcomponentMonitors.get(i).getStatistics(
                    itfName, methodName, parametersTypes).getAverageServiceTime(pastXMilliseconds));
        return averageServiceTime;
    }
}