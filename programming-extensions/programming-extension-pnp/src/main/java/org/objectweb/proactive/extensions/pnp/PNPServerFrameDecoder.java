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
package org.objectweb.proactive.extensions.pnp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.util.Timer;
import org.objectweb.proactive.extensions.pnp.PNPFrame.MessageType;
import org.objectweb.proactive.extensions.pnp.PNPServerHandler.Heartbeater;
import org.objectweb.proactive.extensions.pnp.exception.PNPException;

import java.net.SocketAddress;


/** A PNP server frame decoder
 *
 * The server side frame decoder is an advanced version of the client side decoder.
 * It decodes the first received frame, and configure the {@link PNPServerHandler}
 * accordingly to this first frame (heartbeat period)
 *
 * We use our custom frame decoder instead of the standard one provided by Netty
 * to achieve zero copy. It leads to major performances improvements (both 
 * bandwidth and throughput)
 * 
 * @since ProActive 4.3.0
 */
@ChannelHandler.Sharable
class PNPServerFrameDecoder implements ChannelUpstreamHandler {
    boolean firstFrame;
    final private PNPServerHandler pnpServerHandler;
    final private Timer timer;

    private final int maxFrameLength = Integer.MAX_VALUE;
    private final int lengthFieldLength = 4;

    private volatile int lengthBytesToRead;
    private volatile ChannelBuffer lengthBuffer;
    private volatile long frameBytesToRead;
    private volatile ChannelBuffer frameBuffer;
    private volatile boolean skipFrame;

    public PNPServerFrameDecoder(PNPServerHandler pnpServerHandler, Timer timer) {
        this.pnpServerHandler = pnpServerHandler;
        this.timer = timer;
        this.firstFrame = true;
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent event) throws Exception {
        if (event instanceof MessageEvent) {
            MessageEvent msgEvent = (MessageEvent) event;
            Object msg = msgEvent.getMessage();
            if (msg instanceof ChannelBuffer) {
                callDecode(ctx, (ChannelBuffer) msg, msgEvent.getRemoteAddress());
                return;
            }
        } else if (event instanceof ChannelStateEvent) {
            ChannelStateEvent stateEvent = (ChannelStateEvent) event;
            if (stateEvent.getState() == ChannelState.CONNECTED) {
                if (stateEvent.getValue() != null) {
                    lengthBytesToRead = lengthFieldLength;
                    lengthBuffer = getBuffer(ctx.getChannel().getConfig().getBufferFactory(),
                            lengthBytesToRead);
                }
            }
        }
        ctx.sendUpstream(event);
    }

    private void callDecode(ChannelHandlerContext ctx, ChannelBuffer buffer, SocketAddress remoteAddress)
            throws Exception {
        while (buffer.readableBytes() > 0) {
            Object o = decode(ctx, buffer);
            if (o != null)
                Channels.fireMessageReceived(ctx, o, remoteAddress);
        }
    }

    protected Object decode(ChannelHandlerContext ctx, ChannelBuffer buffer) throws Exception {
        if (!firstFrame) {
            this.pnpServerHandler.bytesAvailable();
        }

        if (lengthBytesToRead > 0) {
            if (lengthBytesToRead > buffer.readableBytes()) {
                lengthBytesToRead -= buffer.readableBytes();
                lengthBuffer.writeBytes(buffer);
                return null;
            } else {
                lengthBuffer.writeBytes(buffer, lengthBytesToRead);
                lengthBytesToRead = 0;

                frameBytesToRead = lengthBuffer.getUnsignedInt(0);
                if (frameBytesToRead < 0) {
                    skipFrame = true;
                    frameBytesToRead = 0;
                    Channels.fireExceptionCaught(ctx, new CorruptedFrameException("negative frame length: " +
                        frameBytesToRead));
                } else if (frameBytesToRead > maxFrameLength) {
                    skipFrame = true;
                    Channels.fireExceptionCaught(ctx, new TooLongFrameException("frame length exceeds " +
                        maxFrameLength + ": " + frameBytesToRead));
                } else {
                    skipFrame = false;
                    frameBuffer = getBuffer(ctx.getChannel().getConfig().getBufferFactory(),
                            (int) frameBytesToRead);
                }
            }
        }

        if (frameBytesToRead > buffer.readableBytes()) {
            frameBytesToRead -= buffer.readableBytes();
            if (skipFrame) {
                buffer.skipBytes(buffer.readableBytes());
            } else {
                frameBuffer.writeBytes(buffer);
            }
            return null;
        } else {
            lengthBuffer.setIndex(0, 0);
            lengthBytesToRead = lengthFieldLength;
            if (skipFrame) {
                buffer.skipBytes((int) frameBytesToRead);
                frameBytesToRead = 0;
                return null;
            } else {
                frameBuffer.writeBytes(buffer, (int) frameBytesToRead);
                frameBytesToRead = 0;

                PNPFrame m = null;
                try {
                    m = PNPFrame.constructMessage(frameBuffer, 0);
                } catch (Exception e) {
                    this.pnpServerHandler.clientLeave();
                    throw e;
                }

                frameBuffer = null;

                if (firstFrame) {
                    if (m.getType() == PNPFrame.MessageType.HEARTBEAT_ADV) {
                        PNPFrameHeartbeatAdvertisement ahFrame = (PNPFrameHeartbeatAdvertisement) m;

                        long heartbeatPeriod = ahFrame.getHeartbeatPeriod();
                        Heartbeater heartbeater = new Heartbeater(ctx.getChannel(), timer, heartbeatPeriod);
                        this.pnpServerHandler.setHeartBeater(heartbeater);

                        this.firstFrame = false;
                        return null; // Do not sent the first frame to the handler
                    } else {
                        throw new PNPException("Invalid first frame type must be " +
                            MessageType.HEARTBEAT_ADV + " but is " + m.getType());
                    }
                }

                return m;
            }
        }
    }

    protected ChannelBuffer getBuffer(ChannelBufferFactory factory, int capacity) {
        return factory.getBuffer(capacity);
    }
}
