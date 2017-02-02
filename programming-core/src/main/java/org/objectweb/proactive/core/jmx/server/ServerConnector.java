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
package org.objectweb.proactive.core.jmx.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveJMXConstants;


/**
 * Creates and register a ProActive JMX Connector Server
 * @author The ProActive Team
 *
 */
public class ServerConnector {
    private MBeanServer mbs;

    private JMXConnectorServer cs;

    private String serverName;

    /**
     * Creates and register a ProActive JMX Connector attached to the platform MBean Server.
     *
     */
    public ServerConnector() {
        /* Retrieve the Platform MBean Server */
        this("serverName");
    }

    public ServerConnector(String serverName) {
        this.mbs = ManagementFactory.getPlatformMBeanServer();
        this.serverName = serverName;

        String url = "service:jmx:proactive:///jndi/proactive://localhost/" +
                     ProActiveJMXConstants.SERVER_REGISTERED_NAME + "_" + this.serverName;
        JMXServiceURL jmxUrl;
        try {
            jmxUrl = new JMXServiceURL(url);
            Thread.currentThread().setContextClassLoader(ServerConnector.class.getClassLoader());
            cs = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl,
                                                                 ProActiveJMXConstants.PROACTIVE_JMX_ENV,
                                                                 this.mbs);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the JMX Connector
     * @throws IOException
     */
    public void start() throws IOException {
        this.cs.start();
    }

    public UniqueID getUniqueID() {
        return ((ProActiveConnectorServer) cs).getUniqueID();
    }

    public ProActiveConnectorServer getConnectorServer() {
        return (ProActiveConnectorServer) cs;
    }
}
