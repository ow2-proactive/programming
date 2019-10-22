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
package functionalTests.activeobject.futuremonitoring.iservice;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.GCMFunctionalTest;


/**
 * Scenario with two active objects.
 * First one, the server returns a future asynchronously (and takes forever to return the value)
 * Second one, the proxy forwards this future to the caller, via an immediate service.
 * This test checks that when the server is destroyed, the client receives an exception and does not block forever.
 * @author ActiveEon Team
 * @since 22/10/2019
 */
public class TestFutureMonitoringWithProxy extends GCMFunctionalTest {

    public TestFutureMonitoringWithProxy() throws ProActiveException {
        super(1, 1);
        super.startDeployment();
    }

    @Test(expected = FutureMonitoringPingFailureException.class)
    public void action() throws Throwable {
        Node node = super.getANode();
        // create the server on the remote node
        AOServer server = PAActiveObject.newActive(AOServer.class, new Object[0], node);
        // create the proxy locally
        AOProxy proxy = PAActiveObject.newActive(AOProxy.class, new Object[] { server });
        // Future asynchronous call
        BooleanWrapper future = proxy.forwardFuture();
        // Kill the remote node
        try {
            node.getProActiveRuntime().killRT(false);
        } catch (Exception e) {

        }
        Assert.assertFalse("Result should not be true", PAFuture.getFutureValue(future).getBooleanValue());
    }
}
