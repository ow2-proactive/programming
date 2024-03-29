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
package functionalTests.protointerop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;

import functionalTests.FunctionalTest;


public class TestPamrWithHttp extends AbstractProtoInterop {

    @BeforeClass
    static public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.setValue("pamr");
        PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("localhost");
        FunctionalTest.prepareForTest();
    }

    public TestPamrWithHttp() throws ProActiveException {
        super("http");
    }

    @Test(timeout = 30000)
    public void test() throws ActiveObjectCreationException, NodeException, UnknownProtocolException,
            RemoteObjectSet.NotYetExposedException {
        super.test();
    }

}
