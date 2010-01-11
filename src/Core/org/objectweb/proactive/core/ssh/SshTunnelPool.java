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
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.Sleeper;


public class SshTunnelPool {
    /** The SSH configuration to use in this pool */
    final private SshConfig config;
    /** A cache to remember if plain socket connection works for a given destination */
    final private TryCache tryCache;
    /** SSH connection & tunnels cache */
    final private Map<String, Pair> cache;
    /** The thread in charge of tunnel & connection garbage collection */
    final private Thread gcThread;

    public SshTunnelPool(final SshConfig config) {
        logger.debug("Created a new SSH tunnel pool");

        this.config = config;
        this.tryCache = new TryCache();
        this.cache = new HashMap<String, Pair>();

        this.gcThread = new Thread(new GCThread());
        this.gcThread.setDaemon(true);
        this.gcThread.setName("SSH Tunnel pool GC");
        if (this.config.getGcInterval() > 0) {
            logger.debug("Starting SSH GC thread");
            this.gcThread.start();
        }
    }

    /**
     * Return a socket connected to the remote host
     *
     * @param host the remote host to connect to
     * @param port the remote port  to connect to
     * @return A socket connected to the remote endpoint
     * @throws IOException If the connection cannot be opened
     */
    public Socket getSocket(String host, int port) throws IOException {
        Socket socket = null;

        if (config.tryPlainSocket()) {
            // Try plain socket connections
            if (this.tryCache.shouldTryDirect(host, port)) {
                try {
                    // Try direct connection (never tried or was successful)
                    InetSocketAddress address = new InetSocketAddress(host, port);
                    socket = new Socket();
                    socket.connect(address, this.config.getConnectTimeout());
                    this.tryCache.recordTrySuccess(host, port);
                } catch (IOException e) {
                    this.tryCache.recordTryFailure(host, port);
                    socket = null;
                }
            }
        }

        if (socket == null) {
            // SSH tunnel must be used
            synchronized (this.cache) {
                int sshPort = this.config.getPort(host);
                String username = this.config.getUsername(host, sshPort);

                Pair pair = this.cache.get(host);
                if (pair == null) {
                    // Open a SSH connection
                    SshConnection cnx = new SshConnection(username, host, sshPort, config
                            .getPrivateKeys(host));
                    pair = new Pair(cnx);
                    this.cache.put(host, pair);
                }

                SshTunnelStatefull tunnel = pair.getTunnel(host, port);
                if (tunnel == null) {
                    // Open a tunnel
                    tunnel = createSshTunStatefull(pair.cnx, host, port);
                    pair.registerTunnel(tunnel);
                }

                // Grab a socket
                socket = tunnel.getSocket();
            }
        }

        return socket;
    }

    /**
     * This class maintains state which reflects whether a given host has already
     * been contacted through a tunnel or a direct connection or has
     * never been contacted before.
     */
    private static class TryCache {
        /*
         * - Key does not exist:         never tried
         * - Key exists, value is true:  direct connection ok
         * - Key exists, value is false: direct connection nok
         */
        final private ConcurrentHashMap<String, Boolean> _hash;

        TryCache() {
            _hash = new ConcurrentHashMap<String, Boolean>();
        }

        private String getKey(String host, int port) {
            return host + ":" + port;
        }

        boolean everTried(String host, int port) {
            String key = getKey(host, port);
            return (_hash.get(key) != null);
        }

        boolean shouldTryDirect(String host, int port) {
            String key = getKey(host, port);
            Boolean b = _hash.get(key);

            if (b == null)
                return true;

            if (b.booleanValue())
                return true;

            return false;
        }

        void recordTrySuccess(String host, int port) {
            String key = getKey(host, port);
            _hash.put(key, Boolean.valueOf(true));
        }

        void recordTryFailure(String host, int port) {
            String key = getKey(host, port);
            _hash.put(key, Boolean.valueOf(false));
        }
    }

    // Cannot be static in SshTunnelStatefull 
    private SshTunnelStatefull createSshTunStatefull(SshConnection connection, String remoteHost,
            int remotePort) throws IOException {
        int initialPort = ProActiveRandom.nextInt(65536 - 1024) + 1024;
        for (int localPort = (initialPort == 65535) ? 1024 : (initialPort + 1); localPort != initialPort; localPort = (localPort == 65535) ? 1024
                : (localPort + 1)) {

            try {
                logger.trace("initialPort:" + initialPort + " localPort:" + localPort);
                SshTunnelStatefull tunnel = new SshTunnelStatefull(connection, remoteHost, remotePort,
                    localPort);
                return tunnel;
            } catch (BindException e) {
                // Try another port
                if (logger.isDebugEnabled()) {
                    logger.debug("The port " + localPort + " is not free");
                }
            }
        }

        throw new IOException("Failed to create a SSH Tunnel to " + remoteHost + ":" + remotePort);
    }

    /**
     * A SshTunnel which manage statistics about the number of opened sockets
     */
    private class SshTunnelStatefull extends SshTunnel {
        /** number of currently open sockets */
        final private AtomicInteger users = new AtomicInteger();
        /** If users == 0, the timestamp of the last call to close() */
        final private AtomicLong unusedSince = new AtomicLong();

        SshTunnelStatefull(SshConnection connection, String remoteHost, int remotePort, int localport)
                throws IOException {
            super(connection, remoteHost, remotePort, localport);
        }

        @Override
        public Socket getSocket() throws IOException {
            this.users.incrementAndGet();

            InetSocketAddress address = new InetSocketAddress(this.getPort());
            Socket socket = new Socket() {
                public synchronized void close() throws IOException {
                    synchronized (SshTunnelPool.this.cache) {
                        unusedSince.set(System.currentTimeMillis());
                        users.decrementAndGet();
                    }
                    super.close();
                }
            };
            socket.connect(address);
            return socket;
        }

        public long unusedSince() {
            if (this.users.get() == 0) {
                return this.unusedSince.get();
            } else {
                return Long.MAX_VALUE;
            }
        }
    }

    private static class Pair {
        final private SshConnection cnx;
        final private Map<String, SshTunnelStatefull> tunnels;

        private Pair(SshConnection cnx) {
            this.cnx = cnx;
            this.tunnels = new HashMap<String, SshTunnelStatefull>();
        }

        public SshTunnelStatefull getTunnel(String host, int port) {
            return this.tunnels.get(buildKey(host, port));
        }

        public void registerTunnel(SshTunnelStatefull tunnel) {
            String host = tunnel.getDistantHost();
            int port = tunnel.getRemotePort();

            this.tunnels.put(buildKey(host, port), tunnel);
        }

        private String buildKey(String host, int port) {
            return host + ":" + port;
        }
    }

    /**
     *  Performs garbage collection of the SSH tunnels and SSH connections
     */
    private final class GCThread implements Runnable {
        private Sleeper sleeper;

        public GCThread() {
            this.sleeper = new Sleeper(config.getGcInterval());
        }

        public void run() {
            while (true) {
                sleeper.sleep();

                synchronized (SshTunnelPool.this.cache) {
                    logger.trace("Running garbage collection");
                    long ctime = System.currentTimeMillis(); // Avoid too many context switches

                    // Purge unused tunnels
                    for (Pair p : cache.values()) {
                        for (Iterator<SshTunnelStatefull> iT = p.tunnels.values().iterator(); iT.hasNext();) {
                            SshTunnelStatefull t = iT.next();
                            if (ctime - t.unusedSince() > config.getGcInterval()) {
                                try {
                                    t.close();
                                } catch (Exception e) {
                                    logger.error("", e);
                                }
                                iT.remove();
                            }
                        }
                    }

                    // Purge unused connections (no opened tunnel)
                    for (Iterator<Pair> iP = cache.values().iterator(); iP.hasNext();) {
                        Pair p = iP.next();
                        if (p.tunnels.isEmpty()) {
                            p.cnx.close();
                            iP.remove();
                        }
                    }
                }
            }
        }
    }
}
