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

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;


/** A PNP frame decoder
 *
 * We use our custom frame decoder instead of the standard one provided by Netty
 * to achieve zero copy. It leads to major performances improvements (both 
 * bandwidth and throughput)
 * 
 * since ProActive 4.3.0
 */

@ChannelHandler.Sharable
class PNPClientFrameDecoder implements ChannelUpstreamHandler {

    private final int maxFrameLength = Integer.MAX_VALUE;

    private final int lengthFieldLength = 4;

    private volatile int lengthBytesToRead;

    private volatile ChannelBuffer lengthBuffer;

    private volatile long frameBytesToRead;

    private volatile ChannelBuffer frameBuffer;

    private volatile boolean skipFrame;

    public PNPClientFrameDecoder() {
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
                    lengthBuffer = getBuffer(ctx.getChannel().getConfig().getBufferFactory(), lengthBytesToRead);
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
                    Channels.fireExceptionCaught(ctx,
                                                 new CorruptedFrameException("negative frame length: " +
                                                                             frameBytesToRead));
                } else if (frameBytesToRead > maxFrameLength) {
                    skipFrame = true;
                    Channels.fireExceptionCaught(ctx,
                                                 new TooLongFrameException("frame length exceeds " + maxFrameLength +
                                                                           ": " + frameBytesToRead));
                } else {
                    skipFrame = false;
                    frameBuffer = getBuffer(ctx.getChannel().getConfig().getBufferFactory(), (int) frameBytesToRead);
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
                PNPFrame m = PNPFrame.constructMessage(frameBuffer, 0);
                frameBuffer = null;
                return m;
            }
        }
    }

    protected ChannelBuffer getBuffer(ChannelBufferFactory factory, int capacity) {
        return factory.getBuffer(capacity);
    }
}
