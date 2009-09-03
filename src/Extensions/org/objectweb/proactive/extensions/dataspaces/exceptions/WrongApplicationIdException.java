/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

/**
 *
 *
 */
public class WrongApplicationIdException extends ConfigurationException {

    /**
     *
     */
    public WrongApplicationIdException() {
    }

    /**
     * @param message
     */
    public WrongApplicationIdException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public WrongApplicationIdException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public WrongApplicationIdException(Throwable cause) {
        super(cause);
    }

}
