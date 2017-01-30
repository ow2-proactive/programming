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
package functionalTests.pamr;

import java.net.InetAddress;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extensions.pamr.client.Tunnel;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;

import functionalTests.FunctionalTest;


public class BlackBox extends FunctionalTest {
    protected Router router;

    protected Tunnel tunnel;

    protected RouterConfig config;

    @Before
    public void beforeBlackbox() throws Exception {
        config = new RouterConfig();
        this.router = Router.createAndStart(config);

        Socket s = new Socket(InetAddress.getLocalHost(), this.router.getPort());
        this.tunnel = new Tunnel(s);
    }

    @After
    public void afterBlackBox() {
        this.tunnel.shutdown();
        this.router.stop();
    }
}
