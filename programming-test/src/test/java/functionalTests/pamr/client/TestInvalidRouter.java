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
package functionalTests.pamr.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.pamr.client.AgentImpl;
import org.objectweb.proactive.extensions.pamr.client.ProActiveMessageHandler;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.remoteobject.util.socketfactory.PAMRPlainSocketFactory;

import functionalTests.FunctionalTest;


public class TestInvalidRouter extends FunctionalTest {

    @Test
    public void test() throws ProActiveException, UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        AgentImpl agt = new AgentImpl(localhost,
                                      12423,
                                      null,
                                      new MagicCookie(),
                                      ProActiveMessageHandler.class,
                                      new PAMRPlainSocketFactory());
        Assert.assertNull(agt.getAgentID());
    }
}
