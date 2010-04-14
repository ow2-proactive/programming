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
package org.objectweb.proactive.extra.messagerouting.router.processor;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.PAMRConfig;
import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;
import org.objectweb.proactive.extra.messagerouting.router.RouterImpl;


/** Asynchronous handler for a given {@link MessageType}
 * 
 * @since ProActive 4.1.0
 */
public abstract class Processor {

    final static protected Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.FORWARDING_ROUTER);
    final protected RouterImpl router;
    final protected ByteBuffer rawMessage;

    protected Processor(ByteBuffer rawMessage, RouterImpl router) {
        this.router = router;
        this.rawMessage = rawMessage;
    }

    /**
     * The implementations will provide the logic of handling the messages
     * @throws MalformedMessageException - if the message received from the client does not comply with the message routing protocol
     */
    abstract public void process() throws MalformedMessageException;
}
