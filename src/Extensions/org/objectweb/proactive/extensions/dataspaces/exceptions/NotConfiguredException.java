/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

/**
 *
 *
 */
public class NotConfiguredException extends ConfigurationException {

    /**
     *
     */
    public NotConfiguredException() {
    }

    /**
     * @param message
     */
    public NotConfiguredException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public NotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public NotConfiguredException(Throwable cause) {
        super(cause);
    }

}
