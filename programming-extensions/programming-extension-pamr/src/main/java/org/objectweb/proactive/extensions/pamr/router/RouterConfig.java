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
package org.objectweb.proactive.extensions.pamr.router;

import java.io.File;
import java.net.InetAddress;
import org.objectweb.proactive.annotation.PublicAPI;


/** A bean for router configuration. 
 * 
 * This bean can be used to pass options to the router. 
 * 
 * Once the bean has been passed to a router is cannot be modified anymore. Any subsequent 
 * call to a setter will throw a {@link IllegalStateException}
 * 
 * @since ProActive 4.1.0
 */
@PublicAPI
public class RouterConfig {

    volatile private boolean readyOnly;

    private int port;

    private boolean isDaemon;

    private int nbWorkerThreads;

    private InetAddress inetAddress;

    private File reservedAgentConfigFile;

    private int heartbeatTimeout;

    private long clientEvictionTimeout;

    public RouterConfig() {
        this.port = 0;
        this.isDaemon = false;
        this.nbWorkerThreads = 4;
        this.inetAddress = null;
        this.heartbeatTimeout = 9000;
        this.clientEvictionTimeout = -1;
    }

    public void setReadOnly() {
        this.readyOnly = true;
    }

    private void checkReadOnly() {
        if (this.readyOnly)
            throw new IllegalStateException(RouterConfig.class.getName() + "beans is read only");
    }

    int getPort() {
        return port;
    }

    /** The port on which the server will bind 
     * 
     * If 0 the router will bind to a random free port
     * 
     * @throws IllegalArgumentException if the port number is invalid
     */
    public void setPort(int port) {
        checkReadOnly();

        if (port < 0 || port > 65535)
            throw new IllegalArgumentException("port must be between 0 and 65535");

        this.port = port;
    }

    boolean isDaemon() {
        return isDaemon;
    }

    /** Set if the router is a daemon thread */
    public void setDaemon(boolean isDaemon) {
        checkReadOnly();
        this.isDaemon = isDaemon;
    }

    int getNbWorkerThreads() {
        return nbWorkerThreads;
    }

    /** Set the number of worker threads
     * 
     * Each received message is handled asynchronously by a pool of workers. 
     * Increasing the amount of worker will increase the parallelism of message
     * handling and sending. 
     * 
     * Incoming messages are read by a single thread.
     * 
     */
    public void setNbWorkerThreads(int nbWorkerThreads) {
        checkReadOnly();
        this.nbWorkerThreads = nbWorkerThreads;
    }

    InetAddress getInetAddress() {
        return inetAddress;
    }

    /** The {@link InetAddress} on which the router will listen */
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setReservedAgentConfigFile(File file) {
        this.reservedAgentConfigFile = file;
    }

    public File getReservedAgentConfigFile() {
        return this.reservedAgentConfigFile;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public long getClientEvictionTimeout() {
        return clientEvictionTimeout;
    }

    public void setClientEvictionTimeout(long timeout) {
        this.clientEvictionTimeout = timeout;
    }

}
