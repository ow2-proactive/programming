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
import java.util.Random;

import org.objectweb.proactive.core.remoteobject.RemoteObjectRequest;


/**
 * This benchmark will send <code> TIME </code> times a byte array of size <code> SIZE </code> KB.
 */
public class LargeByteArrayBenchmark extends RemoteObjectRequest implements BenchmarkObject, Serializable {

    private static final long serialVersionUID = 61L;

    // Size in kB
    private int defaultSize = 300;
    // How many time the test is done
    private int TIME = 10;
    private long beginTime;
    private int count = 0;
    byte tab[] = new byte[defaultSize * 1024];
    private Random rand = new Random();

    public boolean doTest() {
        return count < TIME;
    }

    // Return a negative value, the greater => the faster
    public int getResult() {
        return (int) (beginTime - System.currentTimeMillis());
    }

    public void receiveResponse(Object o) {
        this.count++;
    }

    public void init() {
        rand.nextBytes(tab);
        count = 0;
        beginTime = System.currentTimeMillis();
    }

    public RemoteObjectRequest getRequest() {
        return this;
    }

    public Object execute(Object unused) {
        return this.tab;
    }

    public void setParameter(String param) {
        defaultSize = Integer.parseInt(param);
        tab = new byte[defaultSize * 1024];
    }
}
