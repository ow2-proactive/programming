/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.extra.pnp;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.Timer;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.pnp.PNPFrame.MessageType;
import org.objectweb.proactive.extra.pnp.PNPServerHandler.Heartbeater;
import org.objectweb.proactive.extra.pnp.exception.PNPException;


/** A PNP server frame decoder
 *
 * The server side frame decoder is an advanced version of the client side decoder.
 * It decodes the first received frame, and configure the {@link PNPServerHandler}
 * accordingly to this first frame (heartbeat period)
 *
 * @since ProActive 4.3.0
 */
/* TODO: Implement a zero-copy frame decoder.
 *
 * The FrameDecoder provided by netty, reuse a cumulation buffer and pass a
 * copy of the ChannelBuffer to the next upstream handler. This is a
 * performance killer when big messages are exchanged. We could rewrite a simple
 * zero-copy frame decoder.
 */
@ChannelPipelineCoverage("one")
class PNPServerFrameDecoder extends FrameDecoder {
    static final private Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP_CODEC);

    final private PNPServerHandler pnpServerHandler;
    final private Timer timer;
    boolean firstFrame;
    boolean frameInProgress;

    public PNPServerFrameDecoder(PNPServerHandler pnpServerHandler, Timer timer) {
        this.pnpServerHandler = pnpServerHandler;
        this.timer = timer;
        this.firstFrame = true;
        this.frameInProgress = false;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {
        if (buf.readableBytes() < 4) {
            return null;
        }

        if (!this.firstFrame && !this.frameInProgress) {
            this.frameInProgress = true;
            this.pnpServerHandler.bytesAvailable();
        }

        buf.markReaderIndex();

        int length = buf.readInt();
        if (buf.readableBytes() < length) {
            buf.resetReaderIndex();
            return null;
        }

        PNPFrame m = null;
        try {
            ChannelBuffer cb = buf.readBytes(length); // FIXME: COPY !!!
            m = PNPFrame.constructMessage(cb, 0);
        } catch (Exception e) {
            this.pnpServerHandler.clientLeave();
            throw e;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("DECODED  " + m);
        }

        // Handle the first frame in the decoder
        // It allows us to setup the Heartbeater ASAP and trigger heartbeat from the decoder.
        // If done in the handler, then heartbeats cannot be sent until the message if fully decoded
        if (firstFrame) {
            if (m.getType() == PNPFrame.MessageType.HEARTBEAT_ADV) {
                PNPFrameHeartbeatAdvertisement ahFrame = (PNPFrameHeartbeatAdvertisement) m;

                long heartbeatPeriod = ahFrame.getHeartbeatPeriod();
                Heartbeater heartbeater = new Heartbeater(channel, timer, heartbeatPeriod);
                this.pnpServerHandler.setHeartBeater(heartbeater);

                this.firstFrame = false;
                return null; // Do not sent the first frame to the handler
            } else {
                throw new PNPException("Invalid first frame type must be " + MessageType.HEARTBEAT_ADV +
                    " but is " + m.getType());
            }
        }

        this.frameInProgress = false;
        return m;
    }
}
