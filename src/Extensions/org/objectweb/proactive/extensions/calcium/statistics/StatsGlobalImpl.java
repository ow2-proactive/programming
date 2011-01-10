/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.calcium.statistics;

import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class contains a snapshot of the current Skernel statistics.
 * Among others, the following information is kept:
 *
 * Number of solved root tasks, Number of solved tasks,
 * Length of the ready queue, Wallclock time, Computation time
 *
 * @author The ProActive Team
 */
public class StatsGlobalImpl implements java.io.Serializable, StatsGlobal {
    /**
     * 
     */
    private static final long serialVersionUID = 500L;
    private int processingQueueLength;
    private int waitingQueueLength;
    private int readyNewQueueLength;
    private int readyQueueLength;
    private int resultsQueueLength;
    private int solvedNumberTasks;
    private int solvedRootTasks;
    private long processingTime;
    private long waitingTime;
    private long readyTime;
    private long resultsTime;
    private long wallclockTime;
    private long computationTime;

    public StatsGlobalImpl() {
        solvedNumberTasks = solvedRootTasks = readyQueueLength = readyNewQueueLength = 0;
    }

    public void setQueueLengths(int readQNew, int readyQ, int processingQ, int waitingQ, int resultsQ) {
        this.readyNewQueueLength = readQNew;
        this.readyQueueLength = readyQ;
        this.processingQueueLength = processingQ;
        this.waitingQueueLength = waitingQ;
        this.resultsQueueLength = resultsQ;
    }

    public synchronized void increaseSolvedTasks(Task<?> task) {
        solvedNumberTasks++;
        Stats taskStats = task.getStats();
        computationTime += taskStats.getComputationTime();
        processingTime += taskStats.getProcessingTime();
        readyTime += taskStats.getReadyTime();
        resultsTime += taskStats.getResultsTime();
        waitingTime += taskStats.getWaitingTime();
        wallclockTime += taskStats.getWallClockTime();

        if (task.isRootTask()) {
            solvedRootTasks++;
        }
    }

    @Override
    public String toString() {
        String ls = System.getProperty("line.separator");

        return "Queue Lengths:  " + readyNewQueueLength + "/" + readyQueueLength + "R " +
            processingQueueLength + "P " + waitingQueueLength + "W " + resultsQueueLength + "F" + ", " +
            /*
            "Average Times:  "+getAverageReadyTime()+"R "+getAverageProcessingTime()+"P "+
            getAverageWaitingTime()+"W "+getAverageResultsTime()+"F "+
            getAverageWallClockTime()+"L "+getAverageComputationTime()+"C [ms]"+ls+
             */
            "Computed Tasks: " + getSolvedNumberOfRootTasks() + "[Root] " + getSolvedNumberOfTasks() +
            "[Total]";
    }

    // ********* INTERFACE METHODS BEGIN HERE   ************
    public int getReadyQueueLength() {
        return readyQueueLength;
    }

    public int getSolvedNumberOfTasks() {
        return solvedNumberTasks;
    }

    public int getSolvedNumberOfRootTasks() {
        return solvedRootTasks;
    }

    public int getProccessingQueueLength() {
        return processingQueueLength;
    }

    public int getWaitingQueueLength() {
        return waitingQueueLength;
    }

    public int getResultsQueueLength() {
        return resultsQueueLength;
    }

    public long getAverageWallClockTime() {
        if (solvedNumberTasks == 0) {
            return 0;
        }
        return wallclockTime / solvedNumberTasks;
    }

    public long getAverageProcessingTime() {
        if (solvedNumberTasks == 0) {
            return 0;
        }
        return processingTime / solvedNumberTasks;
    }

    public long getAverageWaitingTime() {
        if (solvedNumberTasks == 0) {
            return 0;
        }
        return waitingTime / solvedNumberTasks;
    }

    public long getAverageResultsTime() {
        if (solvedNumberTasks == 0) {
            return 0;
        }
        return resultsTime / solvedNumberTasks;
    }

    public long getAverageReadyTime() {
        if (solvedNumberTasks == 0) {
            return 0;
        }
        return readyTime / solvedNumberTasks;
    }

    public long getAverageComputationTime() {
        if (solvedNumberTasks == 0) {
            return 0;
        }
        return computationTime / solvedNumberTasks;
    }
}
