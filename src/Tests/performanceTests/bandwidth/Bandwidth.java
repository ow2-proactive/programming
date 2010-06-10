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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package performanceTests.bandwidth;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;

import performanceTests.HudsonReport;
import functionalTests.GCMFunctionalTestDefaultNodes;


public abstract class Bandwidth extends GCMFunctionalTestDefaultNodes {
    /** The buffer included in each message */
    static final public byte buf[] = new byte[10 * 1024 * 1024]; // 10Mo

    private Class<?> cl;

    public Bandwidth(Class<?> cl) {
        super(1, 1);
        this.cl = cl;
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        Server server = PAActiveObject.newActive(Server.class, new Object[] {}, super.getANode());
        Client client = PAActiveObject.newActive(Client.class, new Object[] { server });
        double bandwidth = client.runTest();
        HudsonReport.reportToHudson(this.cl, bandwidth);
    }

    static public class Server implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 430L;
        boolean firstRequest = true;
        long count = 0;
        long startTime;

        public Server() {

        }

        public int serve(byte[] buf) {
            if (firstRequest) {
                startTime = System.currentTimeMillis();
                firstRequest = false;
            }

            count++;
            return 0;
        }

        public double finish() {
            long endTime = System.currentTimeMillis();
            double size = (1.0 * Bandwidth.buf.length * count) / (1024 * 1024);

            System.out.println("Size: " + size);
            System.out.println("Duration: " + (endTime - startTime));

            double bandwith = (1000.0 * size) / (endTime - startTime);
            System.out.println("Bandwidth " + bandwith);
            return bandwith;
        }
    }

    static public class Client implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 430L;
        private Server server;

        public Client() {

        }

        public Client(Server server) {
            this.server = server;
        }

        public double runTest() {
            // Warmup
            for (int i = 0; i < 10; i++) {
                server.serve(TestRMI.buf);
            }
            System.out.println("End of warmup");

            final long testDuration = CentralPAPropertyRepository.PA_TEST_PERF_DURATION.getValue();
            long startTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startTime > testDuration)
                    break;

                server.serve(TestRMI.buf);
            }
            double bandwidth = server.finish();

            // startTest must be sync
            return bandwidth;
        }
    }
}
