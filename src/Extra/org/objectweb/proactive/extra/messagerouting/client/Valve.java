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
