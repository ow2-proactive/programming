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

import org.objectweb.proactive.core.remoteobject.RemoteObjectRequest;


/**
 * Interface for dynamic remoteObject benchmark
 *
 * Any object that implement this interface could
 * be use as a benchmark for multi-protocol selection
 * by using the property : PA_BENCHMARK_CLASS
 *
 * A new BenchmarkObject will be create for each RemoteRemoteObject to test.
 *
 * @see ThroughputBenchmark
 * @see SelectionOnly
 * @see LargeByteArrayBenchmark
 */
public interface BenchmarkObject {

    /**
     * Call before the benchmark
     */
    public void init();

    /**
     * Call at the end. RemoteObject will be sort in ascending order
     */
    public int getResult();

    /**
     * If benchmark result is important, it can be handle here
     *
     * @param o
     *         The response of the benchmark for one remoteObject
     */
    public void receiveResponse(Object o);

    /**
     * Use in benchmark as while (benchmark.doTest())
     */
    public boolean doTest();

    /**
     * Pass a parameter to the benchmark
     */
    public void setParameter(String param);

    /**
     * Return an object that extends a RemoteObjectRequest, this object is
     * send to the remoteObject and then on server side, the method
     * RemoteObjectRequest.execute() is executed.
     */
    public RemoteObjectRequest getRequest();
}
