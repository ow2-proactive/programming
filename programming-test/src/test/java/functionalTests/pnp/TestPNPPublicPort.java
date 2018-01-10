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
package functionalTests.pnp;

import java.net.InetAddress;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.pnp.PNPConfig;

import functionalTests.FunctionalTest;


public class TestPNPPublicPort extends FunctionalTest {
    @Test
    public void testPNPNewActivePublicAddress() throws Exception {
        final int mockPublicPort = 45678;
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.setValue("pnp");
        CentralPAPropertyRepository.PA_PUBLIC_ADDRESS.setValue(InetAddress.getLocalHost().getHostName());
        PNPConfig.PA_PNP_PUBLIC_PORT.setValue(mockPublicPort);
        AOTest ao = PAActiveObject.newActive(AOTest.class, new Object[0]);

        String aoUrl = PAActiveObject.getUrl(ao);
        Assert.assertTrue(aoUrl + " should contain public address and public port inside user info",
                          aoUrl.startsWith("pnp://" + InetAddress.getLocalHost().getHostName() + ':' + mockPublicPort +
                                           "@"));

        ao.sayHello().getBooleanValue();

        AOTest ao2 = PAActiveObject.lookupActive(AOTest.class, aoUrl);
        ao2.sayHello().getBooleanValue();
    }

    public static class AOTest {
        public AOTest() {

        }

        BooleanWrapper sayHello() {
            return new BooleanWrapper(true);
        }
    }
}
