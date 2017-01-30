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
package org.objectweb.proactive.core.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Random;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This factory creates server socket with randomly choosen port number
 * it tries 5 different ports before reporting a failure
 */
public class RandomPortSocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory, Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.RMI);

    static protected final int MAX = 5;

    static protected Random random = new Random();

    protected int basePort = 35000;

    protected int range = 5000;

    public RandomPortSocketFactory() {
        logger.debug("RandomPortSocketFactory constructor()");
    }

    public RandomPortSocketFactory(int basePort, int range) {
        logger.debug("RandomPortSocketFactory constructor(2) basePort = " + basePort + " range " + range);
        this.basePort = basePort;
        this.range = range;
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        int tries = 0;
        logger.debug("RandomPortSocketFactory: createServerSocket " + port + " requested");
        while (true) {
            try {
                int offset = random.nextInt(range);
                ServerSocket socket = new ServerSocket(basePort + offset);
                logger.debug("RandomPortSocketFactory: success for port " + (basePort + offset));
                return socket;
            } catch (IOException e) {
                tries++;
                if (tries > MAX) {
                    throw new IOException();
                }
            }
        }
    }

    public Socket createSocket(String host, int port) throws IOException {
        logger.debug("RandomPortServerSocketFactory: createSocket to host " + host + " on port " + port);
        return new Socket(host, port);
    }
}
