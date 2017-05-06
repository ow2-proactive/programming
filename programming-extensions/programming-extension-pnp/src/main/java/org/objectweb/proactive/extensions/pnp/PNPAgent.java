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
package org.objectweb.proactive.extensions.pnp;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pnp.exception.PNPException;
import org.objectweb.proactive.extensions.pnp.exception.PNPHeartbeatTimeoutException;
import org.objectweb.proactive.extensions.pnp.exception.PNPIOException;
import org.objectweb.proactive.extensions.pnp.exception.PNPTimeoutException;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.SweetCountDownLatch;


/**
 * The local PNP Agent.
 *
 * The agent is used by the PNP remote objects to send and receive messages.
 * It opens/closes the connections & blocks calling threads until the response
 * is received
 *
 * @since ProActive 4.3.0
 */
@PublicAPI
public class PNPAgent {
    static final private Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP);

    /** Current Call ID
     *
     * This ID must be incremented each time a message is sent.
     * If {@link Long#MAX_VALUE} is reached, then the next value will be {@link Long#MIN_VALUE}.
     * It is ok as long as two contemporary calls does not have the same call id.
     **/
    final private AtomicLong cCallId;

    /** Server Channel */
    /* A reference is kept on the serverChannel to be able to cleanup the ressource if needed */
    final private Channel serverChannel;

    /** Server port */
    final private int port;

    /** A cache of already open PNP Connection
     *
     * Since PNP use an heartbeat mechanism, we try to minimize the number of
     * open connection to reduce the heartbeat overhead.
     */
    final private PNPClientChannelCache channelCache;

    /** Creates a local PNP Agent
     *
     * @param config the PNP config
     * @throws PNPException if the agent cannot be created (ex: port already in use)
     */
    public PNPAgent(PNPConfig config, PNPExtraHandlers extraHandlers) throws PNPException {
        this.cCallId = new AtomicLong(0);

        // Configure Netty
        Executor pnpExecutor;
        pnpExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("PNP shared thread pool"));

        // Server side
        ServerSocketChannelFactory ssocketFactory;
        ssocketFactory = new NioServerSocketChannelFactory(pnpExecutor, pnpExecutor);
        ServerBootstrap sBoostrap = new ServerBootstrap(ssocketFactory);
        sBoostrap.setPipelineFactory(new PNPServerPipelineFactory(extraHandlers, pnpExecutor));
        sBoostrap.setOption("tcpNoDelay", true);
        sBoostrap.setOption("child.tcpNoDelay", true);
        try {
            this.serverChannel = sBoostrap.bind(new InetSocketAddress(config.getPort()));
            SocketAddress sa = this.serverChannel.getLocalAddress();
            if (sa instanceof InetSocketAddress) {
                this.port = ((InetSocketAddress) sa).getPort();
                logger.debug("PNP is listening on " + sa);
            } else {
                this.port = -1;
                throw new PNPException("Failed to setup the server side of PNP. The SocketAddress is not an InetSocketAddress");
            }
        } catch (ChannelException e) {
            throw new PNPException("Failed to setup the server side of PNP", e);
        }

        // Client side
        ClientSocketChannelFactory csocketFactory;
        csocketFactory = new NioClientSocketChannelFactory(pnpExecutor, pnpExecutor);
        ClientBootstrap cBootstrap = new ClientBootstrap(csocketFactory);
        cBootstrap.setPipelineFactory(new PNPClientPipelineFactory(extraHandlers));
        cBootstrap.setOption("tcpNoDelay", true);
        cBootstrap.setOption("child.tcpNoDelay", true);
        this.channelCache = new PNPClientChannelCache(cBootstrap);
    }

    /** Sends a call to a remote PNP server.
     *
     * @param uri The URI of the recipient
     * @param msgReq The call
     * @return The result of the call
     * @throws PNPException If the call failed to execute successfully
     */
    public InputStream sendMsg(URI uri, PNPFrameCall msgReq) throws PNPException {
        InetAddress address;
        InetAddress publicAddress = null;
        String publicAddressString = uri.getUserInfo();

        if (publicAddressString != null) {
            try {
                publicAddress = InetAddress.getByName(publicAddressString);
            } catch (UnknownHostException e) {
                logger.debug("Unknown public address " + publicAddressString, e);
            }
        }

        try {
            address = InetAddress.getByName(uri.getHost());
        } catch (UnknownHostException e) {
            if (publicAddress != null) {
                address = publicAddress;
            } else {
                throw new PNPException("Invalid uri: " + uri, e);
            }
        }

        int port = uri.getPort();
        return sendMsg(address, publicAddress, port, msgReq);
    }

    /** Sends a call to a remote PNP server
     *
     * @param addr The inet address of the recipient
     * @param publicAddr The public inet address of the recipient
     * @param port The port on which the recipient is listening
     * @param msgReq The call
     * @return The result of the call
     * @throws PNPException If the call failed to execute successfully
     */
    public InputStream sendMsg(InetAddress addr, InetAddress publicAddr, int port, PNPFrameCall msgReq)
            throws PNPException {
        PNPClientChannel channel = channelCache.getChannel(addr, publicAddr, port, msgReq.getHearthbeatPeriod());
        return channel.sendMessage(msgReq);
    }

    /** Returns an (almost) unique call ID
     *
     * The call ID is not really unique since the counter can loop if more than {@link Long#MAX_VALUE}
     * calls are performed. But is it admitted that at a given two contemporary calls will never have the same
     * call ID.
     *
     * @return An unique call ID
     */
    public long getCallId() {
        return this.cCallId.getAndIncrement();
    }

    /** Returns the local inet address
     *
     * @return the local inet address of the PNP server
     */
    public InetAddress getInetAddress() {
        return ProActiveInet.getInstance().getInetAddress();
    }

    /**  Returns the local TCP port
     *
     * @return the local TCP port of the PNP server
     */
    public int getPort() {
        return port;
    }

    /** Identify a {@link PNPClientChannel}
     *
     * A {@link PNPClientChannel} is identified by a remote inet address, a remote TCP port and  an heartbeat
     * value.
     */
    static class PNPChannelId {
        /** The remote inet address  */
        final private InetAddress addr;

        /** The remote public inet address  */
        final private InetAddress publicAddr;

        /** The remote port*/
        final private int port;

        /** The heartbeat period of the channel*/
        final private long heartbeat;

        public PNPChannelId(final InetAddress addr, final InetAddress publicAddr, final int port,
                final long heartbeat) {
            this.addr = addr;
            this.publicAddr = publicAddr;
            this.port = port;
            this.heartbeat = heartbeat;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((addr == null) ? 0 : addr.hashCode());
            result = prime * result + ((publicAddr == null) ? 0 : publicAddr.hashCode());
            result = prime * result + (int) (heartbeat ^ (heartbeat >>> 32));
            result = prime * result + port;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PNPChannelId other = (PNPChannelId) obj;
            if (addr == null) {
                if (other.addr != null)
                    return false;
            } else if (!addr.equals(other.addr))
                return false;
            if (publicAddr == null) {
                if (other.publicAddr != null)
                    return false;
            } else if (!publicAddr.equals(other.publicAddr))
                return false;
            if (heartbeat != other.heartbeat)
                return false;
            if (port != other.port)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return (publicAddr != null ? publicAddr + "@" : "") + addr + ":" + port + "(" + heartbeat + ")";
        }
    }

    /** A cache of PNP connections
     *
     * The cache is in charge of opening PNP connections & manage opened PNP connections.
     * One and only one {@link PNPClientChannel} is allowed for a given {@link PNPChannelId}.
     */
    static class PNPClientChannelCache {
        /** The {@link PNPClientChannel} cache
         *
         *  If a channel is in the cache, then a connection is already open for the given {@link PNPChannelId}
         */
        private final ConcurrentHashMap<PNPChannelId, PNPClientChannel> channels;

        /** The netty client boostrap used to create the {@link PNPClientChannel} */
        private final ClientBootstrap clientBootstrap;

        /** The timer used to check heartbeats */
        final private Timer timer;

        public PNPClientChannelCache(final ClientBootstrap clientBootstrap) {
            this.channels = new ConcurrentHashMap<PNPChannelId, PNPClientChannel>();
            this.clientBootstrap = clientBootstrap;
            NamedThreadFactory tf = new NamedThreadFactory("PNP client handler timer (shared)",
                                                           true,
                                                           Thread.MAX_PRIORITY);
            this.timer = new HashedWheelTimer(tf, 10, TimeUnit.MILLISECONDS);
        }

        /** Gets a {@link PNPClientChannel} for this remote endpoint
         *
         * If the channel is not already in the cache, a new connexion is opened.
         *
         * @param addr The remote inet address
         * @param publicAddr The public remote inet address
         * @param port The remote port
         * @param heartbeat The heartbeat period of the channel
         * @return a {@link PNPClientChannel} corresponding to the parameter
         * @throws PNPException If the channel cannot be opened
         */
        public PNPClientChannel getChannel(InetAddress addr, InetAddress publicAddr, int port, long heartbeat)
                throws PNPException {
            return getChannel(new PNPChannelId(addr, publicAddr, port, heartbeat));
        }

        /** Gets a {@link PNPClientChannel} for the channel ID
         *
         * @param channelId The channel ID
         * @return a {@link PNPClientChannel} corresponding to the channel ID
         * @throws PNPException If the channel cannot be opened
         */
        public PNPClientChannel getChannel(PNPChannelId channelId) throws PNPException {
            PNPClientChannel c = this.channels.get(channelId);
            if (c == null) {
                c = new PNPClientChannel(clientBootstrap, channelId, this, timer);
                // A PNPException thrown has been if the channel cannot be created

                PNPClientChannel prev = this.channels.putIfAbsent(channelId, c);
                if (prev != null) {
                    // Destroy the channel, to avoid duplicate channel
                    c.close("duplicate channel", null);
                    c = prev;
                }
            }

            return c;
        }

        public boolean remove(PNPClientChannel channel) {
            PNPClientChannel c = this.channels.remove(channel.channelId);
            if (logger.isTraceEnabled()) {
                if (c != null) {
                    logger.trace("Removed channel " + channel + " from the cache");
                } else {
                    logger.trace("Cannot remove channel " + channel + " from the cache (not in the cache)");
                }
            }
            return c != null;
        }
    }

    /** A client channel connected to a remote PNP server */
    static class PNPClientChannel {
        static final private long DEFAULT_CONNECT_TIMEOUT = 60 * 1000;

        /** Identify the channel*/
        final private PNPChannelId channelId;

        /** The Netty channel */
        private Channel channel;

        /** To park calling thread until the response is received */
        final private Parking parking;

        /** A reference on the channel cache to be able to remove ourself on channel close*/
        final private PNPClientChannelCache cache;

        /** Opens a client channel
         *
         * If an heartbeat period is specified then this method will block no longer than it.
         * If not, a default 60 seconds connect timeout is used
         */
        public PNPClientChannel(final ClientBootstrap bootstrap, final PNPChannelId channelId,
                final PNPClientChannelCache cache, final Timer timer) throws PNPTimeoutException, PNPIOException {
            this.channelId = channelId;
            this.parking = new Parking(this.channelId, timer);
            this.cache = cache;
            try {
                createAndConnectSocket(bootstrap, channelId.addr, channelId.port, channelId.heartbeat);
            } catch (Exception e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Could not connect to address " + channelId.addr + ", trying public address " +
                                 channelId.publicAddr, e);
                }
                createAndConnectSocket(bootstrap, channelId.publicAddr, channelId.port, channelId.heartbeat);
            }
            ChannelFuture cf;
            long timeout;

            PNPClientHandler clientHandler;
            clientHandler = (PNPClientHandler) this.channel.getPipeline().get(PNPClientHandler.NAME);
            clientHandler.setPnpClientChannel(this);

            // Sends the first frame to negociate the heartbeat
            PNPFrameHeartbeatAdvertisement frame = new PNPFrameHeartbeatAdvertisement(this.getHeartbeatPeriod());
            cf = this.channel.write(frame);
            timeout = this.getHeartbeatPeriod() != 0 ? this.getHeartbeatPeriod() : 60 * 1000;
            if (cf.awaitUninterruptibly(timeout)) {
                if (cf.isSuccess()) {
                    logger.trace("Successfully advertised the heartbeat period to the server");
                } else {
                    throw new PNPIOException("Failed to advertise the heartbeat period to the server", cf.getCause());
                }
            } else {
                if (cf.cancel()) {
                    throw new PNPIOException("Failed to advertise the heartbeat period to the server (timeout reached)",
                                             cf.getCause());
                } else {
                    if (cf.isSuccess()) {
                        logger.trace("Successfully advertised the heartbeat period to the server");
                    } else {
                        throw new PNPIOException("Failed to advertise the heartbeat period to the server",
                                                 cf.getCause());
                    }
                }
            }

            logger.debug("Successfully opened channel " + this.channel);
        }

        private SocketAddress createAndConnectSocket(ClientBootstrap bootstrap, InetAddress address, int port,
                long heartbeat) throws PNPIOException, PNPTimeoutException {
            SocketAddress sa = new InetSocketAddress(address, port);
            ChannelFuture cf = bootstrap.connect(sa);

            long timeout = heartbeat > 0 ? heartbeat : DEFAULT_CONNECT_TIMEOUT;
            if (cf.awaitUninterruptibly(timeout)) {
                if (cf.isSuccess()) {
                    this.channel = cf.getChannel();
                } else {
                    throw new PNPIOException("Failed to connect to " + sa, cf.getCause());
                }
            } else {
                // Cancel the IO to avoid resource leak
                if (cf.cancel()) {
                    throw new PNPTimeoutException("Failed to connect to " + sa + ". Timeout Reached");
                } else {
                    // IO already succeeded
                    if (cf.isSuccess()) {
                        this.channel = cf.getChannel();
                    } else {
                        throw new PNPIOException("Failed to connect to " + sa, cf.getCause());
                    }
                }
            }
            return sa;
        }

        /** Perform send a call through this channel
         *
         * This method blocks until the response is received or a failure occurs
         *
         * @param msg The call
         * @return the result of the call
         * @throws PNPException If the call failed (cnx failed, timeout etc.)
         */
        InputStream sendMessage(PNPFrameCall msg) throws PNPException {
            if (msg.isOneWay()) {
                channel.write(msg);
                return null;
            } else {
                ParkingSlot slot = this.parking.enter(msg.getCallId());
                channel.write(msg);
                return slot.waitForResponse(msg.getServiceTimeout());
            }
        }

        /** Set the response of a call & unblock the calling thread
         *
         * @param response the response of the call
         */
        void receiveResponse(PNPFrameCallResponse response) {
            ParkingSlot ps = this.parking.remove(response.getCallId());
            if (ps != null) {
                ps.setAndUnlock(response.getPayload());
            } else {
                logger.debug("Discarded message response since the client is no longer in the parking. " + response);
            }
        }

        /** Closes this channel */
        public void close(final String cause, final Throwable e) {
            this.cache.remove(this);

            if (this.channel.isConnected()) {
                ChannelFuture cf = this.channel.close();
                cf.addListener(new ChannelFutureListener() {

                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (logger.isDebugEnabled()) {
                            String msg = "Successfully closed channel " + channel;
                            if (cause != null) {
                                msg += " (cause: " + cause + ")";
                            }
                            if (e == null) {
                                logger.debug(msg);
                            } else {
                                logger.debug(msg, e);
                            }
                        }
                    }
                });
            }
        }

        /** <b>Must</b> be called when a heartbeat message is received from the remote server
         *
         * It updates the heartbeat deadline. If a message is received then the channel is still up !
         */
        public void signalHeartBeatMessage(ChannelHandlerContext ctx, PNPFrameHeartbeat msg) {
            if (logger.isTraceEnabled()) {
                logger.trace("Received heartbeat message:" + msg + " in channel : " + ctx.getChannel());
            }
            this.parking.updateHeartbeatDeadline();
        }

        /** <b>Must</b> be called when the channel is idle
         *
         * Close the channel is no thread is waiting for a response.
         */
        public void signalIdle() {
            if (this.parking.isEmpty()) {
                this.close("channel idle", null);
            }
        }

        /**
         * @return the heartbeat period of this channel
         */
        public long getHeartbeatPeriod() {
            return this.channelId.heartbeat;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PNPClientChannel other = (PNPClientChannel) obj;
            if (channelId == null) {
                if (other.channelId != null)
                    return false;
            } else if (!channelId.equals(other.channelId))
                return false;
            return true;
        }

    }

    /** To park calling thread until the response is received or the channel goes down
     *
     * The {@link Parking} is in charge of checking that heartbeat period has not expired.
     */
    /*
     * Concurrency:
     * - Non final fields are not atomic nor volatile since the methods are synchronized (see JMM)
     * - Not yet fine grained concurrency. Not sure if it is needed...
     */
    static class Parking implements TimerTask {
        static final private int DEFAULT_EXTRA_TIME = 3;

        /** Map of thread waiting for a response (by call id) */
        final private Map<Long, ParkingSlot> slots;

        /** The heartbeat period in ms */
        final private PNPTimeoutHandler timeoutHandler;

        /** The timer used to check if blocked thread smust be unlocked due to a late heartbeat */
        final private Timer timer;

        private final PNPChannelId channelId;

        /** Is the timer armed ? */
        private boolean scheduled;

        /** The number of times the timer task has been run with all slots empty
         *
         * As soon as a calling thread enters the parking, this value is reset to 0
         */
        private int extraTime;

        /** Create a parking for calling threads
         *
         * All the calling thread are unblocked if no message has been received from the remote
         * server since the given heartbeat period.
         *
         * @param channelId The channel id
         */
        private Parking(PNPChannelId channelId, Timer timer) {
            this.slots = new HashMap<Long, ParkingSlot>();
            this.timer = timer;
            this.timeoutHandler = new PNPTimeoutHandler(channelId.heartbeat,
                                                        PNPConfig.PA_PNP_HEARTBEAT_FACTOR.getValue(),
                                                        PNPConfig.PA_PNP_HEARTBEAT_WINDOW.getValue());
            this.channelId = channelId;

            this.scheduled = false;
            this.extraTime = 0;
        }

        synchronized private ParkingSlot enter(long messageId) {
            if (this.timeoutHandler.getTimeout() > 0) {
                this.extraTime = 0;

                if (!this.scheduled) {
                    long timeoutValue = this.timeoutHandler.getTimeout();
                    logger.trace("Starting timer");
                    this.timer.newTimeout(this, timeoutValue, TimeUnit.MILLISECONDS);
                    this.scheduled = true;
                }
            }

            ParkingSlot mb = new ParkingSlot(this, messageId);
            slots.put(messageId, mb);
            return mb;
        }

        /* called by the timer to unblock the calling threads if the hb a not been received */
        synchronized public void run(Timeout timeout) {
            // reset to default values
            this.scheduled = false;

            PNPTimeoutHandler.HeartBeatNotificationData notificationData = this.timeoutHandler.getNotificationData();
            this.timeoutHandler.resetNotification();

            if (this.slots.isEmpty() && (this.extraTime++ > DEFAULT_EXTRA_TIME)) {
                logger.trace("Parking timer task canceled (#extra_time reached)");
                return;
            }

            if (notificationData.heartBeatReceived()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Heartbeat received in time in channel " + channelId +
                                 ((notificationData.getLastHeartBeatInterval() > 0) ? ", last interval was : " +
                                                                                      notificationData.getLastHeartBeatInterval() +
                                                                                      " ms"
                                                                                    : ""));
                }
                long timeoutValue = notificationData.getTimeout();
                if (logger.isTraceEnabled()) {
                    logger.trace("Next heartbeat timeout for channel " + channelId + " : " + timeoutValue);
                }
                this.timer.newTimeout(this, timeoutValue, TimeUnit.MILLISECONDS);
                this.scheduled = true;
            } else {
                this.unlockDueToDisconnection(notificationData.getTimeout());
            }
        }

        /** Indicates that a message has been received from the remote server */
        public void updateHeartbeatDeadline() {
            this.timeoutHandler.recordHeartBeat(System.currentTimeMillis());
        }

        synchronized private void unlockDueToDisconnection(long timeout) {
            PNPIOException e = new PNPHeartbeatTimeoutException("Heartbeat not received in time (" + timeout + " ms)");
            for (ParkingSlot slot : slots.values()) {
                slot.setAndUnlock(e);
            }
        }

        /** Remove a patient on response arrival */
        synchronized ParkingSlot remove(long messageId) {
            ParkingSlot ps;
            ps = slots.remove(messageId);
            return ps;
        }

        synchronized boolean isEmpty() {
            return this.slots.isEmpty();
        }
    }

    /** Allows calling threads to wait for a response */
    static private class ParkingSlot {
        /** A reference on the parking to remove ourself when the response is received */
        final private Parking parking;

        /** 0 when the response is available or an error occured */
        final private SweetCountDownLatch latch;

        /** The response */
        volatile private InputStream response = null;

        /** Received exception */
        volatile private PNPException exception = null;

        /** message ID of the request */
        final private long callId;

        private ParkingSlot(Parking parking, long msgId) {
            this.parking = parking;
            this.latch = new SweetCountDownLatch(1, logger);
            this.callId = msgId;
        }

        /**
         * Wait until the response is available or an error is received
         *
         * @param timeout
         *            Maximum amount of time to wait before throwing an
         *            exception in milliseconds. 0 means no timeout
         * @return the response
         * @throws PNPException
         *             If the request failed to be send or if the recipient
         *             disconnected before sending the response.
         * @throws TimeoutException
         *             If the timeout is reached
         */
        private InputStream waitForResponse(long timeout) throws PNPException {
            if (timeout == 0) {
                this.latch.await();
            } else {
                boolean b = this.latch.await(timeout, TimeUnit.MILLISECONDS);
                if (!b) {
                    throw new PNPTimeoutException("Timeout reached");
                }
            }

            parking.remove(this.callId);
            if (exception != null) {
                throw exception;
            }
            return response;
        }

        /**
         * Set the response and unlock the waiting thread
         *
         * @param response
         *            the response
         */
        private void setAndUnlock(InputStream response) {
            this.response = response;
            latch.countDown();
        }

        /**
         * Set the exception and unlock the waiting thread
         *
         * @param exception
         *            received error
         */
        public void setAndUnlock(PNPException exception) {
            this.exception = exception;
            latch.countDown();
        }
    }
}
