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

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** Encodes a {@link PNPFrame} into a {@link ChannelBuffer}
 *
 * @since ProActive 4.3.0
 */
@ChannelHandler.Sharable
class PNPEncoder extends OneToOneEncoder {
    static final private Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP_CODEC);

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof PNPFrame)) {
            logger.warn("Invalid msg object type" + msg.getClass().getName() + " should be " +
                        PNPFrame.class.getName() + ". Object discarded.");
            return null;
        }

        PNPFrame message = (PNPFrame) msg;
        if (logger.isTraceEnabled()) {
            logger.trace("encoded message: " + message);
        }

        return message.toChannelBuffer();
    }
}
