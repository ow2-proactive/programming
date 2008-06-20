package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides methods to react when a future is updated.
 */
@PublicAPI
public class PAEventProgramming {
    /**
     * Register a method in the calling active object to be called when the specified future is
     * updated. The registered method takes a java.util.concurrent.Future as parameter.
     * 
     * @param future
     *            the future to watch
     * @param methodName
     *            the name of the method to call on the current active object
     * @throws IllegalArgumentException
     *             if the first argument is not a future or if the method could not be found
     */
    public static void addActionOnFuture(Object future, String methodName) {
        FutureProxy f;
        try {
            f = (FutureProxy) ((StubObject) future).getProxy();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a future, got a " + future.getClass());
        }

        f.addCallback(methodName);
    }
}
