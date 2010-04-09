/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


public class SshConfig {
    private final AtomicBoolean readOnly = new AtomicBoolean(false);

    private String username;
    private boolean tryPlainSocket;
    private long gcInterval;
    private long gcIdleTime;
    private int connectTimeout;
    private int port;
    private String knowHostFile;
    private String keyDir;

    private String[] keys;

    public SshConfig() {
        this.username = System.getProperty("user.name");
        this.tryPlainSocket = false;
        this.keyDir = System.getProperty("user.home") + "/.ssh";
        this.port = 22;
        this.gcInterval = 5000;
        this.gcIdleTime = 10000;

    }

    /** Seals the configuration */
    final void setReadOnly() {
        this.readOnly.set(true);
    }

    final private void checkReadOnly() {
        if (this.readOnly.get())
            throw new IllegalStateException(SshConfig.class.getName() +
                " bean is now read only, cannot be modified");
    }

    /** Returns the user name to be used for this host:port */
    final public String getUsername(String host, int port) {
        // FIXME: Parse the configuration file ?
        return username;
    }

    final public void setUsername(String username) {
        checkReadOnly();
        this.username = username;
    }

    /** Should plain socket be used if direction is possible ? */
    final public boolean tryPlainSocket() {
        return tryPlainSocket;
    }

    final public void setTryPlainSocket(boolean tryNormalFirst) {
        checkReadOnly();
        this.tryPlainSocket = tryNormalFirst;
    }

    /** The amount of time to wait between each garbage collection */
    public long getGcInterval() {
        return gcInterval;
    }

    public void setGcInterval(long gcInterval) {
        checkReadOnly();
        this.gcInterval = gcInterval;
    }

    /** The connect timeout for plain and ssh socket */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        checkReadOnly();
        this.connectTimeout = connectTimeout;
    }

    /** Returns the port on which the SSH server is listening */
    public int getPort(String host) {
        // FIXME: Parse a configuration file ?
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /** Returns the location of the know host file */
    public String getKnowHostFile() {
        return knowHostFile;
    }

    public void setKnowHostFile(String knowHostFile) {
        checkReadOnly();
        this.knowHostFile = knowHostFile;
    }

    /** Returns the location of the .ssh directory */
    public String getKeyDir() {
        return keyDir;
    }

    public void setKeyDir(String keyDir) {
        checkReadOnly();
        this.keyDir = keyDir;
    }

    synchronized public String[] getPrivateKeys(String host) {
        if (this.keys == null) {
            try {
                SSHKeys sshKeys = new SSHKeys(this.keyDir);
                this.keys = sshKeys.getKeys();
            } catch (IOException e) {
                logger.info("Failed to open the SSH keydir: " + this.keyDir);
                return new String[] {};
            }
        }

        return this.keys;
    }

    /** SSH tunnels and connections are garbage collected if unused longer than this amount of time (ms) */
    public long getGcIdleTime() {
        return gcIdleTime;
    }

    public void setGcIdleTime(long gcIdleTime) {
        checkReadOnly();
        this.gcIdleTime = gcIdleTime;
    }
}
