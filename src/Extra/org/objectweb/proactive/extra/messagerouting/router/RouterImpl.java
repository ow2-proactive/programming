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
package org.objectweb.proactive.extra.messagerouting.router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.Sleeper;
import org.objectweb.proactive.core.util.SweetCountDownLatch;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.PAMRConfig;
import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.HeartbeatMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.HeartbeatRouterMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage.ErrorType;


/**
 * 
 * @since ProActive 4.1.0
 */
public class RouterImpl extends RouterInternal implements Runnable {
    public static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.FORWARDING_ROUTER);
    public static final Logger admin_logger = ProActiveLogger
            .getLogger(PAMRConfig.Loggers.FORWARDING_ROUTER_ADMIN);

    static final public int DEFAULT_PORT = 33647;

    /** Read {@link ByteBuffer} size. */
    private final static int READ_BUFFER_SIZE = 4096;

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
    RouterImpl(RouterConfig config) throws IOException {

        init(config);
        tpe = Executors.newFixedThreadPool(config.getNbWorkerThreads());

        long rand = 0;
        while (rand == 0) {
            rand = ProActiveRandom.nextPosLong(); // can be 0
        }
        this.routerId = rand;
    }

    private void init(RouterConfig config) throws IOException {
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
        logger.info("Message router listening on " + serverSocket.toString());

        // register the listener with the selector
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        boolean r = this.selectThread.compareAndSet(null, Thread.currentThread());
        if (r == false) {
            logger.error("A select thread has already been started, aborting the current thread ",
                    new Exception());
            return;
        }

        // Start the thread in charge of sending the heartbeat
        final int heartbeatPeriod = PAMRConfig.PA_PAMR_HEARTBEAT_TIMEOUT.getValue();
        final int period = heartbeatPeriod / 3;
        if (period > 0) {
            Thread t = new Thread() {
                public void run() {
                    long heartbeatId = 0;

                    while (!stopped.get()) {
                        try { // preventive try/catch. This thread MUST NOT stop or exit
                            long startTime = System.currentTimeMillis();

                            Collection<Client> clients;
                            synchronized (clientMap) {
                                clients = clientMap.values();
                            }

                            sendHeartbeat(clients, heartbeatId);
                            checkHeartbeat(clients);

                            long willSleep = period - (System.currentTimeMillis() - startTime);
                            if (willSleep > 0) {
                                new Sleeper(willSleep).sleep();
                            } else {
                                logger
                                        .info("Router is late. Sending heartbeat to every clients took more than " +
                                            period + "ms");
                            }
                        } catch (Throwable t) {
                            logger.warn("Failed to send heartbeat #" + heartbeatId, t);
                        } finally {
                            heartbeatId++;
                        }
                    }
                }

                public void sendHeartbeat(final Collection<Client> clients, long heartbeatId) {
                    HeartbeatMessage hbMessage = new HeartbeatRouterMessage(heartbeatId);
                    byte[] msg = hbMessage.toByteArray();
                    for (Client client : clients) {
                        try {
                            if (client.isConnected()) {
                                client.sendMessage(msg);
                            }
                        } catch (IOException e) {
                            admin_logger.debug("Failed to send heartbeat #" + heartbeatId + " to " + client);
                        }
                    }
                }

                public void checkHeartbeat(final Collection<Client> clients) {
                    long currentTime = System.currentTimeMillis();

                    for (Client client : clients) {
                        if (client.isConnected()) {
                            if ((currentTime - client.getLastSeen()) > heartbeatPeriod) {
                                // Disconnect
                                logger.info("Client " + client + " disconnected due to late heartbeat");
                                try {
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
            };
            t.setDaemon(true);
            t.setPriority(Thread.MAX_PRIORITY);
            t.setName("PAMR: heartbeat sender");
            t.start();
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
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                        this.handleAccept(key);
                    } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                        this.handleRead(key);
                    } else {
                        logger.warn("Unhandled SelectionKey operation");
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
            client.discardAttachment();
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
                clientDisconnected(key);
            }
        } catch (MalformedMessageException e) {
            // Disconnect the client to avoid a disaster
            clientDisconnected(key);
        } catch (IOException e) {
            clientDisconnected(key);
        }
    }

    /** clean everything when a client disconnect */
    private void clientDisconnected(SelectionKey key) {
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
            client.discardAttachment();

            // Broadcast the disconnection to every client
            // If client is null, then the handshake has not completed and we
            // don't need to broadcast the disconnection
            AgentID disconnectedAgent = client.getAgentId();
            Collection<Client> clients = clientMap.values();
            tpe.submit(new DisconnectionBroadcaster(clients, disconnectedAgent));
        }
        logger.debug("Client " + attachment.getRemoteEndpoint() + " disconnected");

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
}
