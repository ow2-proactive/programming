/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

/**
 *
 *
 */
public class ApplicationAlreadyRegisteredException extends ConfigurationException {

    /**
     *
     */
    public ApplicationAlreadyRegisteredException() {
    }

    /**
     * @param message
     */
    public ApplicationAlreadyRegisteredException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ApplicationAlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public ApplicationAlreadyRegisteredException(Throwable cause) {
        super(cause);
    }

}
