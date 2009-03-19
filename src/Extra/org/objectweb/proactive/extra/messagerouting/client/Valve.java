/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.client;

import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;


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
