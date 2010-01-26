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
//@snippet-start state_full
//@tutorial-start
package org.objectweb.proactive.examples.userguide.cmagent.simple;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;


//TODO 4. remove Serializable and run the agent
public class State implements Serializable {
    private long commitedMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
    private long initMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
    private long maxMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
    private long usedMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    private String osArch = ManagementFactory.getOperatingSystemMXBean().getArch();
    private String osName = ManagementFactory.getOperatingSystemMXBean().getName();
    private String osVersion = ManagementFactory.getOperatingSystemMXBean().getVersion();
    private int osProcs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    private int liveThreads = ManagementFactory.getThreadMXBean().getThreadCount();
    private long startedThreads = ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
    private int peakThreads = ManagementFactory.getThreadMXBean().getPeakThreadCount();
    private int deamonThreads = ManagementFactory.getThreadMXBean().getDaemonThreadCount();
    private Date timePoint = new Date();
    private String hostname;
    {
        try {
            hostname = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public State() {
    }

    public String toString() {

        return new String("\n======= [" + "State at " + timePoint + " on " + hostname + "] =======" +
            "\nCommited memory: " + commitedMemory + " bytes\nInitial memory requested: " + initMemory +
            " bytes\nMaximum memory available: " + maxMemory + " bytes\nUsed memory: " + usedMemory +
            " bytes\nOperating System: " + osName + " " + osVersion + " " + osArch + "\nProcessors: " +
            osProcs + "\nCurrent live threads: " + liveThreads + "\nTotal started threads: " +
            startedThreads + "\nPeak number of live threads: " + peakThreads + "\nCurrent daemon threads: " +
            deamonThreads +
            "\n===============================================================================\n");

    }
}
//@snippet-end state_full
//@tutorial-end
