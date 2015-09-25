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
package org.objectweb.proactive.core.remoteobject.benchmark;

import java.net.URI;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;


/**
 * This class contains all benchmarks results.
 *
 */
public class RemoteObjectBenchmark {

    transient private java.util.Hashtable<String, BenchmarkMonitorThread> benchmarkMonitors;

    private static RemoteObjectBenchmark singleInstance = null;

    public static RemoteObjectBenchmark getInstance() {
        if (singleInstance == null) {
            singleInstance = new RemoteObjectBenchmark();
        }
        return singleInstance;
    }

    private RemoteObjectBenchmark() {
        this.benchmarkMonitors = new java.util.Hashtable<String, BenchmarkMonitorThread>();
    }

    /**
     * If needed launch benchmark or simply add RemoteObjectSet as an observer
     */
    public void subscribeAsObserver(RemoteObjectSet ros, Map<URI, RemoteRemoteObject> remoteObjects,
            String runtimeName, Map<URI, Integer> lastBenchmarkResults) {

        BenchmarkMonitorThread bmt;

        if ((bmt = this.benchmarkMonitors.get(runtimeName)) != null) {
            // Benchmark is in progress or finished
            bmt.addObserver(ros);
            bmt.addOnTheFly(remoteObjects, lastBenchmarkResults);
        } else {
            bmt = new BenchmarkMonitorThread(remoteObjects,
                CentralPAPropertyRepository.PA_BENCHMARK_CLASS.getValue());
            this.benchmarkMonitors.put(runtimeName, bmt);
            bmt.addObserver(ros);
            bmt.launchBenchmark();
        }
    }
}
