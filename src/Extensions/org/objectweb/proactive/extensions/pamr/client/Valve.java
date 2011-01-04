/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.pamr.client;

import org.objectweb.proactive.extensions.pamr.protocol.message.Message;


/** A Valve is a message processing component associated with a particular Agent.
 *
 * A series of Valves are generally associated with each other into a Pipeline.
 * 
 * @since ProActive 4.1.0
 */
public interface Valve {

    /** Return descriptive information about this Valve implementation. */
    public String getInfo();

    /**
     * Perform incoming message processing as required by this Valve.
     *
     * An individual Valve MAY perform the following actions:
     * <ul>
     * 	<li>Examine and/or modify the properties of the specified incoming message</li>
     * 	<li>Examine the properties of the specified incoming message, wrap it to supplement its functionality</li>
     * </ul>
     *
     * @param message The incoming message. It <b>can</b> be modified by this Valve
     * @return The result of the processing performed by this Valve
     */
    public Message invokeIncoming(Message message);

    /**
     * Perform outgoing message processing as required by this Valve.
     *
     * An individual Valve MAY perform the following actions:
     * <ul>
     * 	<li>Examine and/or modify the properties of the specified outgoing message</li>
     * 	<li>Examine the properties of the specified outgoing message, wrap it to supplement its functionality</li>
     * </ul>
     *
     * @param message The outgoing message. It <b>can</b> be modified by this Valve
     * @return The result of the processing performed by this Valve
     */
    public Message invokeOutgoing(Message message);
}
