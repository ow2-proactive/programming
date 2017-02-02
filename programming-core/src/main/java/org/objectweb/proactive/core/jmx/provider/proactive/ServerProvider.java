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
package org.objectweb.proactive.core.jmx.provider.proactive;

import java.io.IOException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.core.jmx.server.ProActiveConnectorServer;


/**
 *  <p>A provider for creating JMX API connector servers using a given
 * protocol.  Instances of this interface are created by {@link
 * JMXConnectorServerFactory} as part of its {@link
 * JMXConnectorServerFactory#newJMXConnectorServer(JMXServiceURL,Map,MBeanServer)
 * newJMXConnectorServer} method.</p>
 *
 * @author The ProActive Team
 */
public class ServerProvider implements JMXConnectorServerProvider {

    /**
     * <p>Creates a new connector server at the given address.  Each
     * successful call to this method produces a different
     * <code>JMXConnectorServer</code> object.</p>
     *
     * @param serviceURL the address of the new connector server.  The
     * actual address of the new connector server, as returned by its
     * {@link JMXConnectorServer#getAddress() getAddress} method, will
     * not necessarily be exactly the same.  For example, it might
     * include a port number if the original address did not.
     *
     * @param environment a read-only Map containing named attributes
     * to control the new connector server's behaviour.  Keys in this
     * map must be Strings.  The appropriate type of each associated
     * value depends on the attribute.
     *
     * @param mbeanServer the MBean server that this connector server
     * is attached to.  Null if this connector server will be attached
     * to an MBean server by being registered in it.
     *
     * @return a <code>JMXConnectorServer</code> representing the new
     * connector server.  Each successful call to this method produces
     * a different object.
     *
     * @exception NullPointerException if <code>serviceURL</code> or
     * <code>environment</code> is null.
     *
     * @exception IOException if the connector server cannot be
     * created.
     */
    public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map<String, ?> env, MBeanServer mbs)
            throws IOException {
        return new ProActiveConnectorServer(url, env, mbs);
    }
}
