package org.objectweb.proactive.core.component.exceptions;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exception thrown if results cannot be reduced from a multicast interface.
 *
 * @author The ProActive Team
 *
 */
@PublicAPI
public class ReductionException extends Exception {

    public ReductionException() {
        super();
    }

    public ReductionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReductionException(String message) {
        super(message);
    }

    public ReductionException(Throwable cause) {
        super(cause);
    }

}
