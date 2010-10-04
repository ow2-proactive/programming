package org.objectweb.proactive.extensions.ssl;

import org.objectweb.proactive.core.ProActiveException;


public class SslException extends ProActiveException {

    public SslException() {
    }

    public SslException(String message) {
        super(message);
    }

    public SslException(Throwable cause) {
        super(cause);
    }

    public SslException(String message, Throwable cause) {
        super(message, cause);
    }
}
