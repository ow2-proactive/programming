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

import java.io.Serializable;

import org.objectweb.proactive.core.remoteobject.RemoteObjectRequest;


/**
 * This benchmark will try to send as much message as possible in a finite time.
 */
public class ThroughputBenchmark extends RemoteObjectRequest implements BenchmarkObject, Serializable {

    private static final long serialVersionUID = 61L;

    private long beginTime;
    // The duration in ms
    private long duration = 2000;
    private int count = 0;

    // Benchmark Part
    public boolean doTest() {
        return (System.currentTimeMillis() - beginTime) < duration;
    }

    public int getResult() {
        return count;
    }

    public void receiveResponse(Object o) {
        this.count++;
    }

    public void init() {
        beginTime = System.currentTimeMillis();
    }

    public RemoteObjectRequest getRequest() {
        return this;
    }

    // End Benchmark

    // RemoteObjectRequest Part
    public Object execute(Object unused) {
        return new Integer(1);
    }

    // End RemoteObjectRequest

    public void setParameter(String param) {
        this.duration = Integer.parseInt(param);
    }
}
