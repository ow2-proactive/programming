package org.objectweb.proactive.multiactivity.execution;

import org.objectweb.proactive.core.body.future.Future;

/**
 * Interface for classes to whom a future proxy can delegate the task of waiting for a future value.
 * The proxy has to announce ({@link #futureArrived(Future)}) the arrival of the value to the waiter, thus this can return from the {@link #waitForFuture(Future)}
 * call.
 * @author Zsolt Istvan
 *
 */
public interface FutureWaiter {
    
    /**
     * Can be used to replace the waiting inside a futre's proxy. This method should return only when the future's value has arrived. This is signaled
     * to the waiter by the proxy with the {@link #futureArrived(Future)} method.
     * @param future The future upon which the wait is performed.
     */
    public void waitForFuture(Future future);
    
    /**
     * Can be used to 'notify' the future waiter about the arrival of an awaited future from the future's proxy. 
     * All {@link #waitForFuture(Future)}s on the same future should return as a consequence.
     * @param future The future whose value has arrived.
     */
    public void futureArrived(Future future);
    
}
