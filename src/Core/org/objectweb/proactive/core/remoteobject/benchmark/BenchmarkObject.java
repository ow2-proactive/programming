/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
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
 * $$ACTIVEEON_INITIAL_DEV$$
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
