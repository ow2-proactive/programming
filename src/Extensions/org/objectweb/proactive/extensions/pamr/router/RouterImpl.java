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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.exceptions.PAMRException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage.ErrorType;
import org.objectweb.proactive.extensions.pamr.protocol.message.HeartbeatMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.HeartbeatRouterMessage;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.SafeTimerTask;
import org.objectweb.proactive.utils.Sleeper;
import org.objectweb.proactive.utils.SweetCountDownLatch;
import org.objectweb.proactive.utils.ThreadPools;


/**
 * 
 * @since ProActive 4.1.0
 */
public class RouterImpl extends RouterInternal implements Runnable {
    public static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER);
    public static final Logger admin_logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER_ADMIN);

    static final public int DEFAULT_PORT = 33647;

    /** Read {@link ByteBuffer} size. */
    private final static int READ_BUFFER_SIZE = 4096;

    public final static long DEFAULT_ROUTER_ID = Long.MIN_VALUE;

    /** True is the router must stop or is stopped*/
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    /** Can pass when the router has been successfully shutdown */
    private final SweetCountDownLatch isStopped = new SweetCountDownLatch(1);
    /** The thread running the select loop */
    private final AtomicReference<Thread> selectThread = new AtomicReference<Thread>();

    /** Thread pool used to execute all asynchronous tasks */
    private final ExecutorService tpe;

    /** All the clients known by {@link AgentID}*/
    private final ConcurrentHashMap<AgentID, Client> clientMap = new ConcurrentHashMap<AgentID, Client>();

    /** The local InetAddress on which the router is listening */
    private InetAddress inetAddress;
    /** The local TCP port on which the router is listening */
    private int port;

    private Selector selector = null;
    private ServerSocketChannel ssc = null;
    private ServerSocket serverSocket = null;

    /** An unique identifier for this router */
    private final long routerId;
    /** The administrator magic cookie 
     *
     * This cookie must be provided to perform remote administrative operations
     */
    volatile private MagicCookie adminMagicCookie;

    private final File configFile;

    private final int heartbeatTimeout;
    private final long clientEvictionTimeout;

    /** Create a new router
     * 
     * When a new router is created it binds onto the given port.
     * 
     * To start handling connections, a thread MUST be spawned by the caller.
     * <code>
     *  RouterImpl this.router = new RouterImpl(0);
     *  Thread t = new Thread(router);
     *  t.setDaemon(true);
     *  t.setName("Router");
     *  t.start();
     * </code>
     * 
     * @param port port number to bind to
     * @throws IOException if the router failed to bind
     */
    RouterImpl(RouterConfig config) throws Exception {
        this.configFile = config.getReservedAgentConfigFile();
        this.heartbeatTimeout = config.getHeartbeatTimeout();
        this.clientEvictionTimeout = config.getClientEvictionTimeout();

        init(config);
        ThreadFactory tf = new NamedThreadFactory("Proactive PAMR router worker");
        tpe = Executors.newFixedThreadPool(config.getNbWorkerThreads(), tf);

        long rand = 0;
        while (rand == 0) {
            rand = ProActiveRandom.nextPosLong(); // can be 0
        }
        this.routerId = rand;
    }

    private void init(RouterConfig config) throws Exception {
        reloadConfigurationFile();

        // Create a new selector
        selector = Selector.open();

        // Open a listener on the right port
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        serverSocket = ssc.socket();

        this.inetAddress = config.getInetAddress();
        this.port = config.getPort();
        InetSocketAddress isa = new InetSocketAddress(this.inetAddress, this.port);
        this.inetAddress = isa.getAddress();
        serverSocket.bind(isa);

        this.port = serverSocket.getLocalPort();
        logger.info("Message router listening on " + serverSocket.toString() + ". Heartbeat timeout is " +
            this.heartbeatTimeout + " ms");
        if (this.clientEvictionTimeout == -1) {
            logger.info("Client eviction is disabled");
        } else {
            logger.info("Client eviction timeout is " + this.clientEvictionTimeout + " ms");
        }

        // register the listener with the selector
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * This timer task checks and sends heartbeats periodically at fixed rate
     *
     * In steady state, the run method and all submitted tasks should complete before
     * the next invocation of the timer task. If overlapping is detected then a warning
     * is printed since it could lead to big troubles.
     */

    private class HeartbeatTimerTask extends SafeTimerTask {
        /** Maximum execution time (ms) */
        final private long maxTime;
        /** Unique id for each heartbeat */
        private long heartbeatId = 0;
        /** Used to send the heartbeat asynchrnously */
        final private ThreadPoolExecutor tpe;

        public HeartbeatTimerTask(long maxTime) {
            this.maxTime = maxTime;
            this.heartbeatId = 0;

            int maxThreads = 32;
            ThreadFactory tf = new NamedThreadFactory("Hearbeat sender", false, Thread.MAX_PRIORITY);
            this.tpe = ThreadPools.newBoundedThreadPool(maxThreads, tf);
        }

        @Override
        public void safeRun() {
            final long begin = System.currentTimeMillis();

            // In steading state tpe should be empty. Busy workers means blocked SendTask
            final int busyWorkers = this.tpe.getActiveCount();
            if (busyWorkers > 0) {
                admin_logger.warn(busyWorkers + " workers [cur:" + this.tpe.getPoolSize() + ",lar:" +
                    this.tpe.getLargestPoolSize() + ",max:" + this.tpe.getMaximumPoolSize() +
                    "] still busy before heartbeats #" + this.heartbeatId + " being send");
            }

            // Snapshot all the client
            final List<Client> clients;
            synchronized (clientMap) {
                clients = new ArrayList<Client>(clientMap.values());
            }

            // The heartbeat to send
            final HeartbeatMessage hbMessage = new HeartbeatRouterMessage(heartbeatId);
            final byte[] bytes = hbMessage.toByteArray();

            // Send asynchronously the heartbeats
            final ArrayList<SendTask> sendTasks = new ArrayList<SendTask>(clients.size());
            final ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>(sendTasks.size());
            for (Client client : clients) {
                if (client.isConnected()) {
                    final SendTask st = new SendTask(client, bytes, this.heartbeatId);
                    sendTasks.add(st);
                    futures.add(tpe.submit(st));
                }
            }

            this.heartbeatId++;

            // Check received heartbeats while the sendTasks are executed
            this.checkHeartbeat(clients);

            // Waiting until maxTime (we could be more aggressive)
            long rtime = (this.maxTime) - (System.currentTimeMillis() - begin);
            if (rtime > 0) {
                new Sleeper(rtime).sleep();
            } else {
                admin_logger.warn("Tooks more than " + this.maxTime +
                    " ms to submit send tasks and check received heartbeats (" + (this.maxTime - rtime) +
                    "ms)");
            }

            // Check all submitted tasks completed
            // We cannot preemptively interrupt tasks but at least the user can be warned that something is wrong
            for (int i = 0; i < futures.size(); i++) {
                final Future<Boolean> f = futures.get(i);
                if (f.isDone()) {
                    try { // Detect failures while sending heartbeat
                        f.get();
                    } catch (Throwable e) {
                        admin_logger
                                .info("Exception occured while sending heartbeat to " + clients.get(i), e);
                    }
                } else {
                    admin_logger.info("Sending heartbeat to " + clients.get(i) + " took longer than " +
                        this.maxTime + "ms.");

                }
            }

        }

        private void checkHeartbeat(Collection<Client> clients) {
            long currentTime = System.currentTimeMillis();

            for (Client client : clients) {
                if (client.isConnected()) {
                    final long diff = currentTime - client.getLastSeen();
                    if (diff > heartbeatTimeout) {
                        // Disconnect
                        try {
                            logger.info("Client " + client + " disconnected due to late heartbeat (" + diff +
                                " ms)");
                            client.disconnect();
                        } catch (IOException e) {
                            logger.info("Failed to disconnected client " + client, e);
                        }

                        // Broadcast the disconnection to every client
                        // If client is null, then the handshake has not completed and we
                        // don't need to broadcast the disconnection
                        AgentID disconnectedAgent = client.getAgentId();
                        tpe.submit(new DisconnectionBroadcaster(clients, disconnectedAgent));
                    }
                }
            }
        }

        private class SendTask implements Callable<Boolean> {
            final Client client;
            final byte[] msg;
            final long heartbeatId;

            public SendTask(Client client, byte[] msg, long heartbeatId) {
                this.client = client;
                this.msg = msg;
                this.heartbeatId = heartbeatId;
            }

            public Boolean call() throws Exception {
                try {
                    if (client.isConnected()) {
                        client.sendMessage(msg);
                    }
                } catch (IOException e) {
                    throw new PAMRException(
                        "Failed to send heartbeat #" + this.heartbeatId + " to " + client, e);
                }

                return true;
            }

        }
    }

    private class EvictClientsTimerTask extends SafeTimerTask {

        @Override
        public void safeRun() {
            evictStaleClients();
        }

        private void evictStaleClients() {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<AgentID, Client> entry : clientMap.entrySet()) {
                AgentID agentID = entry.getKey();
                Client client = entry.getValue();
                if (!agentID.isReserved() && !client.isConnected()) {
                    long timeSinceLastSeen = currentTime - client.getLastSeen();
                    if (timeSinceLastSeen >= clientEvictionTimeout) {
                        logger.info("Evicting client " + client + ": last seen " + timeSinceLastSeen +
                            " ms ago");
                        clientMap.remove(agentID);
                    }
                }
            }
        }
    }

    private void createAndScheduleEvictClientsTimerTask() {
        Timer timer = new Timer("Client eviction timer", true);
        long delay = this.clientEvictionTimeout / 3;
        EvictClientsTimerTask task = new EvictClientsTimerTask();
        timer.scheduleAtFixedRate(task, new Date(), delay);
    }

    public void run() {
        boolean r = this.selectThread.compareAndSet(null, Thread.currentThread());
        if (r == false) {
            logger.error("A select thread has already been started, aborting the current thread ",
                    new Exception());
            return;
        }

        Timer timer = new Timer("Heartbeat timer", true);
        long delay = this.heartbeatTimeout / 3;
        HeartbeatTimerTask hbtt = new HeartbeatTimerTask(delay);
        timer.scheduleAtFixedRate(hbtt, new Date(), delay);

        if (clientEvictionTimeout >= 0) {
            createAndScheduleEvictClientsTimerTask();
        }

        Set<SelectionKey> selectedKeys = null;
        Iterator<SelectionKey> it;
        SelectionKey key;

        while (this.stopped.get() == false) {
            // select new keys
            try {
                selector.select();
                selectedKeys = selector.selectedKeys();
                it = selectedKeys.iterator();
                while (it.hasNext()) {
                    key = (SelectionKey) it.next();
                    it.remove();
                    try {
                        if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                            this.handleAccept(key);
                        } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                            this.handleRead(key);
                        } else {
                            logger.warn("Unhandled SelectionKey operation");
                        }
                    } catch (CancelledKeyException e) {
                        clientDisconnected(key, e.getMessage());
                    }
                }

            } catch (IOException e) {
                logger.warn("Select failed", e);
            }

        }

        this.cleanup();
    }

    /** Stop the router and free all resources*/
    private void cleanup() {
        tpe.shutdown();

        for (Client client : clientMap.values()) {
            client.discardAttachment("Shutting down the router");
        }

        try {
            /* Not sure if we have to set the attachments to null 
             * Possible memory leak
             */
            this.ssc.socket().close();
            this.ssc.close();
            this.selector.close();
        } catch (IOException e) {
            ProActiveLogger.logEatedException(logger, e);
        }
        this.isStopped.countDown();
    }

    /** Accept a new connection */
    private void handleAccept(SelectionKey key) {
        SocketChannel sc;
        try {
            sc = ((ServerSocketChannel) key.channel()).accept();
            sc.configureBlocking(false);

            // Add the new connection to the selector
            sc.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            logger.warn("Failed to accept a new connection", e);
        }
    }

    /** Read available data for this key */
    private void handleRead(SelectionKey key) {
        SocketChannel sc;
        ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_SIZE);

        sc = (SocketChannel) key.channel();

        Attachment attachment = (Attachment) key.attachment();
        if (attachment == null) {
            attachment = new Attachment(this, sc);
            key.attach(attachment);
        }

        // Read all the data available
        try {
            int byteRead;
            do {
                buffer.clear();
                byteRead = sc.read(buffer);
                buffer.flip();

                if (byteRead > 0) {
                    MessageAssembler assembler = attachment.getAssembler();
                    assembler.pushBuffer(buffer);
                }
            } while (byteRead > 0);

            if (byteRead == -1) {
                clientDisconnected(key, "end of stream");
            }
        } catch (MalformedMessageException e) {
            // Disconnect the client to avoid a disaster
            clientDisconnected(key, e.getMessage());
        } catch (IOException e) {
            clientDisconnected(key, e.getMessage());
        }
    }

    /** clean everything when a client disconnect */
    private void clientDisconnected(SelectionKey key, String cause) {
        Attachment attachment = (Attachment) key.attachment();

        key.cancel();
        key.attach(null);
        SocketChannel sc = (SocketChannel) key.channel();
        try {
            sc.socket().close();
        } catch (IOException e) {
            // Miam Miam Miam
            ProActiveLogger.logEatedException(logger, e);
        }

        try {
            sc.close();
        } catch (IOException e) {
            // Miam Miam Miam
            ProActiveLogger.logEatedException(logger, e);
        }
        Client client = attachment.getClient();
        if (client != null) {
            client.discardAttachment(cause);

            // Broadcast the disconnection to every client
            // If client is null, then the handshake has not completed and we
            // don't need to broadcast the disconnection
            AgentID disconnectedAgent = client.getAgentId();
            Collection<Client> clients = clientMap.values();
            tpe.submit(new DisconnectionBroadcaster(clients, disconnectedAgent));
        }
        logger.debug("Client " + attachment.getRemoteEndpoint() + " disconnected: " + cause);

    }

    /* @@@@@@@@@@ ROUTER PACKAGE INTERFACE 
     * 
     * Theses methods cannot be package private due to the processor sub package 
     */

    public void handleAsynchronously(ByteBuffer message, Attachment attachment) {
        TopLevelProcessor tlp = new TopLevelProcessor(message, attachment, this);
        tpe.execute(tlp);
    }

    public Client getClient(AgentID agentId) {
        synchronized (clientMap) {
            return clientMap.get(agentId);
        }
    }

    public void addClient(Client client) {
        synchronized (clientMap) {
            clientMap.put(client.getAgentId(), client);
        }
    }

    /* @@@@@@@@@@ ROUTER PUBLIC INTERFACE: Router */

    public int getPort() {
        return this.port;
    }

    public InetAddress getInetAddr() {
        return this.inetAddress;
    }

    public void stop() {
        if (this.stopped.get() == true)
            throw new IllegalStateException("Router already stopped");

        this.stopped.set(true);

        Thread t = this.selectThread.get();
        if (t != null) {
            t.interrupt();
            this.isStopped.await();
        }
    }

    private static class DisconnectionBroadcaster implements Runnable {
        final private List<Client> clients;
        final private AgentID disconnectedAgent;

        public DisconnectionBroadcaster(Collection<Client> clients, AgentID disconnectedAgent) {
            this.clients = new ArrayList<Client>(clients);
            this.disconnectedAgent = disconnectedAgent;
        }

        public void run() {
            for (Client client : this.clients) {
                if (this.disconnectedAgent.equals(client.getAgentId()))
                    continue;

                ErrorMessage error = new ErrorMessage(ErrorType.ERR_DISCONNECTION_BROADCAST, client
                        .getAgentId(), this.disconnectedAgent, 0);
                try {
                    client.sendMessage(error.toByteArray());
                } catch (Exception e) {
                    ProActiveLogger.logEatedException(logger, e);
                }
            }
        }
    }

    public long getId() {
        return this.routerId;
    }

    private Map<AgentID, MagicCookie> validateConfigFile() throws Exception {
        Properties properties = new Properties();
        MagicCookie configMagicCookie = null;

        try {
            FileInputStream fis = new FileInputStream(this.configFile);
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new IOException("Router configuration file does not exist: " + this.configFile);
        } catch (IOException e) {
            throw new IOException6("Failed to read the router configuration file: " + this.configFile, e);
        } catch (IllegalArgumentException e) {
            throw new IOException6("Failed to read the router configuation file: " + this.configFile, e);
        }

        Map<AgentID, MagicCookie> map = new HashMap<AgentID, MagicCookie>();
        for (Object o : properties.keySet()) {
            String sId = (String) o;
            String sCookie = (String) properties.get(o);

            if ("configuration".equals(sId)) {
                if (configMagicCookie != null) {
                    throw new Exception("Duplicated configuration magic cookie");
                } else {
                    try {
                        configMagicCookie = new MagicCookie(properties.getProperty(sId));
                    } catch (IllegalArgumentException e) {
                        throw new Exception("Invalid configuration magic cookie", e);
                    }
                }
            } else {

                AgentID agentId = null;
                try {
                    agentId = new AgentID(Long.parseLong(sId));
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid configuration file" + this.configFile +
                        ": Keys must be an integer but " + sId + " is not");
                }

                MagicCookie cookie = null;
                try {
                    cookie = new MagicCookie(sCookie);
                } catch (IllegalArgumentException e) {
                    throw new Exception("Invalid configuration file " + this.configFile +
                        ": invalid cookie value  " + sCookie + ". " + e.getMessage());
                }

                if (!agentId.isReserved()) {
                    throw new Exception("Invalid configuration file " + this.configFile +
                        ": invalid Agent ID " + sId + "Agent ID must be between 0 and " +
                        (AgentID.MIN_DYNAMIC_AGENT_ID - 1));
                }
                map.put(agentId, cookie);
            }
        }

        if (configMagicCookie == null) {
            throw new Exception(
                "Configuration magic cookie must be defined in the configuration file (key: configuration)");
        }

        admin_logger.debug("Set config magic cookie to: " + configMagicCookie);
        this.adminMagicCookie = configMagicCookie;

        return map;
    }

    synchronized public void reloadConfigurationFile() throws Exception {
        if (this.configFile == null) {
            return;
        }

        Map<AgentID, MagicCookie> map = validateConfigFile();
        synchronized (this.clientMap) {
            for (AgentID agentId : this.clientMap.keySet()) {
                if (agentId.isReserved() && !map.containsKey(agentId)) {
                    try {
                        this.clientMap.get(agentId).disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        this.clientMap.remove(agentId);
                        admin_logger.debug("Removed reserved agent " + agentId + " (configuration change)");
                    }
                }
            }

            for (AgentID agentID : map.keySet()) {
                Client client = this.clientMap.get(agentID);
                if (client != null) {
                    // Disconnect the client and change the id
                    client.discardAttachment("Configuration file reloaded");
                }
                client = new Client(agentID, map.get(agentID));
                this.clientMap.put(agentID, client);
                admin_logger.debug("Disconnected reserved agent " + agentID +
                    " and updated magic cookie (configuration change)");
            }
        }
    }

    public MagicCookie getAdminMagicCookie() {
        return this.adminMagicCookie;
    }

    public File getConfigurationFile() {
        return this.configFile;
    }

    public int getHeartbeatTimeout() {
        return this.heartbeatTimeout;
    }
}
