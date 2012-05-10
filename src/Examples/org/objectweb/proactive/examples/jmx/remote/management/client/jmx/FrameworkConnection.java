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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.jmx.remote.management.client.jmx;

import java.io.IOException;
import java.util.HashMap;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.jmx.client.ClientConnector;
import org.objectweb.proactive.core.util.URIBuilder;


public class FrameworkConnection {
    private ProActiveConnection connection;
    private String url;
    private boolean connected;
    private static HashMap<String, ProActiveConnection> connectionsMap = new HashMap<String, ProActiveConnection>();

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return the connection
     */
    public ProActiveConnection getConnection() {
        return connection;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    public FrameworkConnection() {
    }

    public FrameworkConnection(String url) {
        this.url = url;
    }

    public void connect() throws IOException {
        this.connection = connectionsMap.get(this.url);
        if (this.connection == null) {

            String serverName = URIBuilder.getNameFromURI(this.url);

            if ((serverName == null) || serverName.equals("")) {
                serverName = "serverName";
            }
            ClientConnector cc = new ClientConnector(this.url, serverName);
            cc.connect();
            this.connection = cc.getConnection();

            connectionsMap.put(this.url, this.connection);
            this.connected = true;
        }
    }

    public void close() throws IOException {
        try {
            if (this.connected) {
                connectionsMap.remove(this.url);
                this.connected = false;
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    public static void main(String[] args) {
        FrameworkConnection fc = new FrameworkConnection("//localhost/");
    }
}
