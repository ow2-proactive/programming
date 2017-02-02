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
package performanceTests.bandwidth;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.GCMFunctionalTest;
import performanceTests.HudsonReport;


public abstract class Bandwidth extends GCMFunctionalTest {
    /** The buffer included in each message */
    static final public byte buf[] = new byte[10 * 1024 * 1024]; // 10Mo

    private Class<?> cl;

    public Bandwidth(Class<?> cl) throws ProActiveException {
        super(1, 1);
        super.startDeployment();
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
