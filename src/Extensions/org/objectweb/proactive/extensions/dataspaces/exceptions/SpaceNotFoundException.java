/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.exceptions;

/**
 * Represents exception caused by request for non-existing data space.
 */
public class SpaceNotFoundException extends DataSpacesException {

    /**
     *
     */
    private static final long serialVersionUID = -127241878737940387L;

    public SpaceNotFoundException(String code) {
        super(code);
    }

    public SpaceNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public SpaceNotFoundException(String code, Object info0) {
        // super(code, info0);
    }

    public SpaceNotFoundException(String code, Object[] info) {
        // super(code, info);
    }

    public SpaceNotFoundException(String code, Throwable throwable) {
        super(code, throwable);
    }

    public SpaceNotFoundException(String code, Object info0, Throwable throwable) {
        // super(code, info0, throwable);
    }

    public SpaceNotFoundException(String code, Object[] info, Throwable throwable) {
        // super(code, info, throwable);
    }
}
