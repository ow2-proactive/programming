package org.objectweb.proactive.extensions.vfsprovider.exceptions;

public class WrongStreamTypeException extends Exception {

    public WrongStreamTypeException() {
        super("Operation not allowed for this stream type");
    }

    /**
     *
     */
    private static final long serialVersionUID = -6361321020427385102L;

}
