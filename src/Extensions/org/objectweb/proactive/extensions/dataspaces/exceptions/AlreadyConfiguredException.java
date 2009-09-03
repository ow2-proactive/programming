package org.objectweb.proactive.extensions.dataspaces.exceptions;

public class AlreadyConfiguredException extends DataSpacesException {

    public AlreadyConfiguredException() {
    }

    public AlreadyConfiguredException(String message) {
        super(message);
    }

    public AlreadyConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyConfiguredException(Throwable cause) {
        super(cause);
    }

}
