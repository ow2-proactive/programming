package org.objectweb.proactive.extra.messagerouting.exceptions;

import java.io.IOException;


/** Signals that an error of some sort has occurred.
 *
 * This class is the general class of exceptions produced by failed message sending.
 * 
 * @since ProActive 4.1.0
 */
@SuppressWarnings("serial")
public class MessageRoutingException extends IOException {

    public MessageRoutingException() {
        super();
    }

    public MessageRoutingException(String message) {
        super(message);
    }

    public MessageRoutingException(Throwable cause) {
        super(cause);
    }

    public MessageRoutingException(String message, Throwable cause) {
        super(message, cause);
    }

}
