package org.objectweb.proactive.core.exceptions;

import java.io.IOException;


public class IOException6 extends IOException {

    public IOException6(Throwable t) {
        super("");
        this.initCause(t);
    }

    public IOException6(String msg, Throwable t) {
        super(msg);
        this.initCause(t);
    }
}
