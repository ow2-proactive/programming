package org.objectweb.proactive.extra.montecarlo.core;

import java.io.Serializable;


/**
 * SubMasterLock
 *
 * @author The ProActive Team
 */
public class SubMasterLock implements Serializable {

    private boolean simulatorInUse = false;
    private boolean executorInUse = false;

    private final static String message = "Concurrent use of Executor and Simulator is not possible.";

    public SubMasterLock() {

    }

    void useSimulator() throws IllegalStateException {
        if (executorInUse) {
            throw new IllegalStateException(message);
        } else {
            simulatorInUse = true;
        }
    }

    void releaseSimulator() throws IllegalStateException {
        simulatorInUse = false;
    }

    void useExecutor() throws IllegalStateException {
        if (simulatorInUse) {
            throw new IllegalStateException(message);
        } else {
            executorInUse = true;
        }
    }

    void releaseExecutor() throws IllegalStateException {
        executorInUse = false;
    }

}
