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

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelHandler;
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

    final private PNPExtraHandlers extraHandlers;

    public PNPClientPipelineFactory(PNPExtraHandlers extraHandlers) {
        this.extraHandlers = extraHandlers;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = Channels.pipeline();

        if (extraHandlers != null) {
            for (final ChannelHandler handler : this.extraHandlers.getClientHandlers()) {
                p.addLast("" + handler.hashCode(), handler);
            }
        }

        // Do not use FixedLengthFrameDecoder provided by netty to avoid
        // copy and an extra handler to parse the messages
        //        p.addLast("pnpDecoder", new PNPClientFrameDecoder());
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
