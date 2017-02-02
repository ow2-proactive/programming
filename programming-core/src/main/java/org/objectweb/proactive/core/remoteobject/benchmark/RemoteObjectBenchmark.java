/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
    public void subscribeAsObserver(RemoteObjectSet ros, Map<URI, RemoteRemoteObject> remoteObjects, String runtimeName,
            Map<URI, Integer> lastBenchmarkResults) {

        BenchmarkMonitorThread bmt;

        if ((bmt = this.benchmarkMonitors.get(runtimeName)) != null) {
            // Benchmark is in progress or finished
            bmt.addObserver(ros);
            bmt.addOnTheFly(remoteObjects, lastBenchmarkResults);
        } else {
            bmt = new BenchmarkMonitorThread(remoteObjects, CentralPAPropertyRepository.PA_BENCHMARK_CLASS.getValue());
            this.benchmarkMonitors.put(runtimeName, bmt);
            bmt.addObserver(ros);
            bmt.launchBenchmark();
        }
    }
}
