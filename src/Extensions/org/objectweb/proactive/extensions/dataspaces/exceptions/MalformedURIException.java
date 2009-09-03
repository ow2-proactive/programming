/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

/**
 * Represents exception caused by usage or request for Data Spaces URI with
 * wrong format.
 */
public class MalformedURIException extends DataSpacesException {

    /**
     *
     */
    private static final long serialVersionUID = 3678271228087635694L;

    /**
     * Empty constructor with no information.
     */
    public MalformedURIException() {
    }

    /**
     * @param message
     *            message describing source of problem
     */
    public MalformedURIException(String message) {
        super(message);
    }

    /**
     * @param cause
     *            underlying cause of the exception
     */
    public MalformedURIException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     *            message describing source of problem
     * @param cause
     *            underlying cause of the exception
     */
    public MalformedURIException(String message, Throwable cause) {
        super(message, cause);
    }
}
