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
package org.objectweb.proactive.extensions.pamr.router.processor;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;


/** Asynchronous handler for a given {@link MessageType}
 * 
 * @since ProActive 4.1.0
 */
public abstract class Processor {
    final static protected Logger admin_logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER_ADMIN);

    final static protected Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER);

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
