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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
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
        AgentImpl agt = new AgentImpl(localhost, 12423, null, new MagicCookie(),
            ProActiveMessageHandler.class, new PAMRPlainSocketFactory());
        Assert.assertNull(agt.getAgentID());
    }
}
