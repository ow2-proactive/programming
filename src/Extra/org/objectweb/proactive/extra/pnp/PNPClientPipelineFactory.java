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

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;


/**
 * The client side pipeline factory
 * <ul>
 *  <li>A dedicated frame decoder (to avoid buffer copy)</li>
 *  <li>A standard frame encoder</li>
 *  <li>A {@link PNPFrame} to bytebuffer encoder</li>
 *  <li>An idle state handler if tunnel auto-closing is enabled (by default)</li>
 *  <li>A client side protocol handler</li>
 * </ul>
 *
 * @since ProActive 4.3.0
 */
class PNPClientPipelineFactory implements ChannelPipelineFactory {
    Timer timer = new HashedWheelTimer();

    public PNPClientPipelineFactory() {
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();
        // Do not use FixedLengthFrameDecoder provided by netty to avoid
        // copy and an extra handler to parse the messages
        p.addLast("pnpDecoder", new PNPClientFrameDecoder());

        p.addLast("frameEncoder", new LengthFieldPrepender(4));
        p.addLast("pnpEncoder", new PNPEncoder());

        long idle_timeout = PNPConfig.PA_PNP_IDLE_TIMEOUT.getValue();
        if (idle_timeout != 0) {
            p.addLast("timer", new IdleStateHandler(timer, 0, idle_timeout, 0, TimeUnit.MILLISECONDS));
        }

        p.addLast(PNPClientHandler.NAME, new PNPClientHandler());
        return p;
    }

}
