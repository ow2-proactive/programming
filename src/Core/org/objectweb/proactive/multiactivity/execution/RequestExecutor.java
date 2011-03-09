package org.objectweb.proactive.multiactivity.execution;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.MultiActiveService;

//TODO -- should define a pair interface for RequestSubmitter with finishedRequest callback functions, to
//        remove the awful coupling inside MultiActiveService
/**
 * Interface for classes that can be used to execute requests inside a {@link MultiActiveService} in the body.
 * @author Izso
 *
 */
public interface RequestExecutor {
    
    /**
     * Submit a request for execution on the body of the service.
     * There are no guarantees on when a serving will begin, neither on its execution time. It is
     * however guaranteed that all previously started requests will finish in finite time. 
     * @param r
     */
    public void submit(Request r);
    
    /**
     * Returns the number of ready request. These are the ones that have been submitted, but not 
     * started executing yet.
     * @return
     */
    public int getNumberOfReady();
    
    /**
     * Returns the number of currently active (executing) requests. These requests are the ones that
     * are currently using the CPU.
     * @return
     */
    public int getNumberOfActive();
    
    /**
     * Returns the number of requests that even though have started executing have blocked for some reason 
     * (like wait-by-necessity).
     * @return
     */
    public int getNumberOfWaiting();

}
