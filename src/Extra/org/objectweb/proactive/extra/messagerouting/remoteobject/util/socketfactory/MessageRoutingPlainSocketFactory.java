/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.remoteobject.util.socketfactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.objectweb.proactive.core.config.PAProperties;


/**
 * The default implementation for message routing socket factory
 * It offers plain(simple), unsecured sockets
 *
 * @since ProActive 4.2.0
 */
public class MessageRoutingPlainSocketFactory implements MessageRoutingSocketFactorySPI {

    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket();
        int timeout = PAProperties.PA_PAMR_CONNECT_TIMEOUT.isSet() ? PAProperties.PA_PAMR_CONNECT_TIMEOUT
                .getValueAsInt() : 0;
        socket.connect(new InetSocketAddress(host, port), timeout);
        return socket;
    }

    public String getAlias() {
        return "plain";
    }
}
