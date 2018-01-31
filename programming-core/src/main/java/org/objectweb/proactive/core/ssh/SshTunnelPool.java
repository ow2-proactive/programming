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
package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.core.ssh.proxycommand.ProxyCommandConfig;
import org.objectweb.proactive.core.ssh.proxycommand.SshProxyConnection;
import org.objectweb.proactive.core.ssh.proxycommand.SshProxySession;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;


public class SshTunnelPool {
    /** The SSH configuration to use in this pool */
    private SshConfig config;

    /** A cache to remember if plain socket connection works for a given destination */
    final private TryCache tryCache;

    /** SSH connection & tunnels cache */
    final private Map<String, Pair> cache;

    /** SSH proxy connection & sessions cache */
    final private Map<String, List<ProxyPair>> proxyCommandCache;

    /** The thread in charge of tunnel & connection garbage collection */
    private Thread gcThread = null;

    /**
     * The garbage collector thread isn't launch by this constructor because
     * of lack of SshConfig
     *
     * @see SshTunnelPool#setSshConfig(SshConfig)
     * @see SshTunnelPool#createAndStartGCThread()
     */
    public SshTunnelPool() {
        logger.debug("Created a new SSH tunnel pool");

        this.tryCache = new TryCache();
        this.cache = new HashMap<String, Pair>();
        this.proxyCommandCache = new HashMap<String, List<ProxyPair>>();
    }

    /**
     * If the GCThread hasn't been created and launch before do it
     */
    public void createAndStartGCThread() {
        if (config == null && gcThread == null) {
            // Throw an exception ?
            return;
        }
        this.gcThread = new Thread(new GCThread());
        this.gcThread.setDaemon(true);
        this.gcThread.setName("SSH Tunnel pool GC");
        if (this.config.getGcInterval() > 0) {
            logger.debug("Starting SSH GC thread");
            this.gcThread.start();
        }
    }

    public void setSshConfig(SshConfig config) {
        this.config = config;
    }

    /**
     * This constructor launch the garbage collector thread, no need to call
     * method SshTunnelPool#createAndStartGCThread() after instantiation.
     *
     * @param config
     */
    public SshTunnelPool(SshConfig config) {
        this();
        this.config = config;
        this.gcThread = new Thread(new GCThread());
        this.gcThread.setDaemon(true);
        this.gcThread.setName("SSH Tunnel pool GC");
        if (this.config.getGcInterval() > 0) {
            logger.debug("Starting SSH GC thread");
            this.gcThread.start();
        }
    }

    /**
     * Return a socket connected to the remote sshHost
     *
     * @param sshHost the remote SSH host to connect to
     * @param port the remote port  to connect to
     * @return A socket connected to the remote endpoint
     * @throws IOException If the connection cannot be opened
     */
    public Socket getSocket(String sshHost, int port) throws IOException {
        Socket socket = null;

        if (config.tryPlainSocket()) {
            // Try plain socket connections
            if (this.tryCache.shouldTryDirect(sshHost, port)) {
                try {
                    // Try direct connection (never tried or was successful)
                    InetSocketAddress address = new InetSocketAddress(sshHost, port);
                    socket = new Socket();
                    socket.connect(address, this.config.getConnectTimeout());
                    this.tryCache.recordTrySuccess(sshHost, port);
                } catch (IOException ioe) {
                    this.tryCache.recordTryFailure(sshHost, port);
                    socket = null;
                }
            }
        }

        // Try proxy command
        if (socket == null && config.tryProxyCommand() &&
            !InetAddress.getByName(sshHost).equals(ProActiveInet.getInstance().getInetAddress())) {
            String gateway = config.getGateway(sshHost);
            String outGateway = ProxyCommandConfig.PA_SSH_PROXY_USE_GATEWAY_OUT.isSet() ? ProxyCommandConfig.PA_SSH_PROXY_USE_GATEWAY_OUT.getValue()
                                                                                        : null;
            // if proxyCommand command mechanism is needed
            if (gateway != null || outGateway != null) {
                synchronized (this.proxyCommandCache) {
                    SshProxyConnection cnx = null;
                    List<ProxyPair> pairs = this.proxyCommandCache.get(gateway);
                    if (pairs == null) {
                        cnx = SshProxyConnection.getInstance(gateway, outGateway, config);
                        pairs = new ArrayList<ProxyPair>();
                        ProxyPair p = new ProxyPair(cnx);
                        pairs.add(p);
                        this.proxyCommandCache.put(gateway, pairs);
                    }

                    SshProxySession session = null;
                    // For each connection, try to open a session
                    for (int i = 0; i < pairs.size(); i++) {
                        try {
                            cnx = (SshProxyConnection) pairs.get(i).cnx;
                            // Always create a new session because there are not Thread-Safe
                            session = cnx.getSession(sshHost, port);
                            pairs.get(i).registerSession(session);
                            break;
                        } catch (IOException channelException) {
                            continue;
                        }
                    }

                    if (session == null) {
                        // No Connections permit to open a new session
                        // Create a new connection
                        cnx = SshProxyConnection.getInstance(gateway, outGateway, (SshConfig) config);
                        session = cnx.getSession(sshHost, port);
                        ProxyPair pair = new ProxyPair(cnx);
                        pair.registerSession(session);
                        pairs.add(pair);
                    }

                    // Grab a socket
                    socket = session.getSocket();
                }
            }
        }

        if (socket == null) {
            // SSH tunnel must be used
            synchronized (this.cache) {
                // if a remote-side-resolvable hostname is defined, use it instead for the tunnel
                String remoteHost = this.config.getHostName(sshHost);
                int sshPort = this.config.getPort(sshHost);
                String username = this.config.getUsername(sshHost);
                Pair pair = this.cache.get(sshHost);
                if (pair == null) {
                    // Open a SSH connection
                    SshConnection cnx = new SshConnection(username,
                                                          sshHost,
                                                          sshPort,
                                                          config.getPrivateKeyPath(sshHost));
                    pair = new Pair(cnx);
                    this.cache.put(sshHost, pair);
                }
                logger.debug("Ssh connection " + username + "@" + sshHost + ":" + sshPort + " established");
                SshTunnelStateful tunnel = pair.getTunnel(remoteHost, port);
                if (tunnel == null) {
                    // Open a tunnel
                    tunnel = createSshTunStateful(pair.cnx, remoteHost, port);
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
         * - Key does not exist: never tried
         * - Key exists, value is true: direct connection ok
         * - Key exists, value is false: direct connection nok
         */
        final private ConcurrentHashMap<String, Boolean> _hash;

        TryCache() {
            _hash = new ConcurrentHashMap<String, Boolean>();
        }

        private String getKey(String host, int port) {
            // port changes a lot, if normal socket works
            // on one port shouldn't it works on all ?
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

    // Cannot be static in SshTunnelStateful
    private SshTunnelStateful createSshTunStateful(SshConnection connection, String remoteHost, int remotePort)
            throws IOException {
        int initialPort = ProActiveRandom.nextInt(65536 - 1024) + 1024;
        for (int localPort = (initialPort == 65535) ? 1024
                                                    : (initialPort +
                                                       1); localPort != initialPort; localPort = (localPort == 65535) ? 1024
                                                                                                                      : (localPort +
                                                                                                                         1)) {

            try {
                logger.trace("initialPort:" + initialPort + " localPort:" + localPort);
                SshTunnelStateful tunnel = new SshTunnelStateful(connection, remoteHost, remotePort, localPort);
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
    private class SshTunnelStateful extends SshTunnel {
        /** number of currently open sockets */
        final private AtomicInteger users = new AtomicInteger();

        /** If users == 0, the timestamp of the last call to close() */
        final private AtomicLong unusedSince = new AtomicLong();

        SshTunnelStateful(SshConnection connection, String distantHost, int distantPort, int localPort)
                throws IOException {
            super(connection, distantHost, distantPort, localPort);
        }

        @Override
        public Socket getSocket() throws IOException {
            this.users.incrementAndGet();

            InetSocketAddress address = new InetSocketAddress(ProActiveInet.getInstance().getInetAddress(),
                                                              this.getPort());
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

        final private Map<String, SshTunnelStateful> tunnels;

        private Pair(SshConnection cnx) {
            this.cnx = cnx;
            this.tunnels = new HashMap<String, SshTunnelStateful>();
        }

        public SshTunnelStateful getTunnel(String host, int port) {
            return this.tunnels.get(buildKey(host, port));
        }

        public void registerTunnel(SshTunnelStateful tunnel) {
            String host = tunnel.getDistantHost();
            int port = tunnel.getRemotePort();

            this.tunnels.put(buildKey(host, port), tunnel);
        }

        private String buildKey(String host, int port) {
            return host + ":" + port;
        }
    }

    private static class ProxyPair {
        final private SshProxyConnection cnx;

        final private List<SshProxySession> sessions;

        private ProxyPair(SshProxyConnection cnx) {
            this.cnx = cnx;
            this.sessions = new ArrayList<SshProxySession>();
        }

        /**
         * Sessions are stored in order to check if they are used or not
         * and garbage collect them
         */
        public void registerSession(SshProxySession sess) {
            this.sessions.add(sess);
        }
    }

    /**
     *  Performs garbage collection of the SSH tunnels and SSH connections
     */
    private final class GCThread implements Runnable {
        private Sleeper sleeper;

        public GCThread() {
            this.sleeper = new Sleeper(config.getGcInterval(), ProActiveLogger.getLogger(Loggers.SLEEPER));
        }

        public void run() {
            while (true) {
                sleeper.sleep();

                synchronized (SshTunnelPool.this.cache) {
                    logger.trace("Running garbage collection");
                    long ctime = System.currentTimeMillis(); // Avoid too many context switches

                    // Purge unused tunnels
                    for (Pair p : cache.values()) {
                        for (Iterator<SshTunnelStateful> iT = p.tunnels.values().iterator(); iT.hasNext();) {
                            SshTunnelStateful t = iT.next();
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

                    // Purge unused proxyCommand session
                    for (List<ProxyPair> lp : proxyCommandCache.values()) {
                        for (ProxyPair p : lp) {
                            for (int i = 0; i < p.sessions.size(); i++) {
                                SshProxySession s = p.sessions.get(i);
                                if (s.isUnused()) {
                                    p.sessions.remove(i);
                                }
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

                    // Purge unused proxyCommand connections (no opened session)
                    for (Iterator<List<ProxyPair>> iLP = proxyCommandCache.values().iterator(); iLP.hasNext();) {
                        List<ProxyPair> lP = iLP.next();
                        for (int i = 0; i < lP.size(); i++) {
                            ProxyPair p = lP.get(i);
                            if (p.sessions.isEmpty()) {
                                p.cnx.close();
                                lP.remove(i);
                            }
                        }
                        if (lP.size() == 0) {
                            iLP.remove();
                        }
                    }

                }
            }
        }
    }
}
