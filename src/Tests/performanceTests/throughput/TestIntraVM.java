/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package performanceTests.throughput;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.NodeException;

import performanceTests.HudsonReport;
import functionalTests.FunctionalTest;


public class TestIntraVM extends FunctionalTest {

    static {
        PAProperties.PA_COMMUNICATION_PROTOCOL.setValue("rmi");
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {
        Server server = PAActiveObject.newActive(Server.class, new Object[] {});
        Client client = PAActiveObject.newActive(Client.class, new Object[] { server });
        client.startTest();
    }

    static public class Server implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 420L;
        boolean firstRequest = true;
        long count = 0;
        long startTime;

        public Server() {

        }

        public void serve() {
            if (firstRequest) {
                startTime = System.currentTimeMillis();
                firstRequest = false;
            }

            count++;
        }

        public void finish() {
            long endTime = System.currentTimeMillis();
            double throughput = (1000.0 * count) / (endTime - startTime);

            System.out.println("Count: " + count);
            System.out.println("Duration: " + (endTime - startTime));
            System.out.println("Throughput " + throughput);
            HudsonReport.reportToHudson(TestIntraVM.class, throughput);
        }
    }

    static public class Client implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 420L;
        private Server server;

        public Client() {

        }

        public Client(Server server) {
            this.server = server;
        }

        public int startTest() {
            // Warmup
            for (int i = 0; i < 1000; i++) {
                server.serve();
            }

            long startTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startTime > PAProperties.PA_TEST_PERF_DURATION
                        .getValueAsInt())
                    break;

                for (int i = 0; i < 50; i++) {
                    server.serve();
                }
            }
            server.finish();

            // startTest must be sync 
            return 0;
        }
    }
}
