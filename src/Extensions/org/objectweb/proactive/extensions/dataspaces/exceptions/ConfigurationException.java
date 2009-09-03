/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

/**
 *
 *
 */
public class ConfigurationException extends DataSpacesException {

    /**
     *
     */
    public ConfigurationException() {
    }

    /**
     * @param message
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}
