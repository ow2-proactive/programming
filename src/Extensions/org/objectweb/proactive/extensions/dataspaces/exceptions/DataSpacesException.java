/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

import org.objectweb.proactive.core.ProActiveException;


/**
 *
 *
 */
public class DataSpacesException extends ProActiveException {

    /**
     *
     */
    public DataSpacesException() {
    }

    /**
     * @param message
     */
    public DataSpacesException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public DataSpacesException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public DataSpacesException(Throwable cause) {
        super(cause);
    }

}
