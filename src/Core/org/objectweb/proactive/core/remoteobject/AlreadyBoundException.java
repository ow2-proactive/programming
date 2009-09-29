package org.objectweb.proactive.core.remoteobject;

import org.objectweb.proactive.core.ProActiveException;


public class AlreadyBoundException extends ProActiveException {
    public AlreadyBoundException() {
        super();
    }

    public AlreadyBoundException(String message) {
        super(message);
    }

    public AlreadyBoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyBoundException(Throwable cause) {
        super(cause);
    }
}
