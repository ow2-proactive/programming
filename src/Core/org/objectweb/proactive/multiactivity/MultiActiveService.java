package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.multiactivity.compatibility.AnnotationProcessor;
import org.objectweb.proactive.multiactivity.compatibility.CompatibilityTracker;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;


/**
 * This class extends the  {@link Service}  class and adds the capability of serving more methods in parallel. 
 * <br> The decision of which methods can run in parallel is made based on annotations set by the user. 
 * These annotations are to be found in the <i> org.objectweb.proactive.annotation.multiactivity</i> package.
 * @author  Zsolt Istvan
 */
public class MultiActiveService extends Service {

    public static final boolean LIMIT_ALL_THREADS = true;
    public static final boolean LIMIT_ACTIVE_THREADS = false;
    public static final boolean REENTRANT_SAME_THREAD = true;
    public static final boolean REENTRANT_SEPARATE_THREAD = false;
    
    public int activeServes = 0;
    public LinkedList<Integer> serveHistory = new LinkedList<Integer>();
    public LinkedList<Integer> serveTsts = new LinkedList<Integer>();

    Logger logger = Logger.getLogger(this.getClass());

    CompatibilityTracker compatibility;
    RequestExecutor executor = null;

    /**
     * MultiActiveService that will be able to optionally use a policy, and will deploy each serving request on a 
     * separate physical thread.
     * @param body
     */
    public MultiActiveService(Body body) {
        super(body);
        
        //not doing the initialization here because after creating the request executor
        //we are not compatible with 'fifoServing' any more.
        
    }
    
    //initializing the compatibility info and the executor
    private void init(){
    	if (executor!=null) return;
    	
    	compatibility = new CompatibilityTracker(new AnnotationProcessor(body.getReifiedObject().getClass()), requestQueue);
        executor = new RequestExecutor(body, compatibility);
    }

    /**
     * Service that relies on the default parallel policy to extract requests from the queue.
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     * @param hardLimit false if the above limit is applicable only to active (running) threads, but not the waiting ones
     * @param hostReentrant true if re-entrant calls should be hosted on the issuer's thread
     */
    public void multiActiveServing(int maxActiveThreads, boolean hardLimit, boolean hostReentrant) {
    	init();
        executor.configure(maxActiveThreads, hardLimit, hostReentrant);
        executor.execute();
    }
    
    /**
     * Service that relies on the default parallel policy to extract requests from the queue. Threads are soft-limited and re-entrance on the same thread is disabled.
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     */
    public void multiActiveServing(int maxActiveThreads){
    	init();
        executor.configure(maxActiveThreads, false, false);
        executor.execute();
        
    }
    
    /**
     * Service that relies on the default parallel policy to extract requests from the queue. Threads are not limited and re-entrance on the same thread is disabled.
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     */
    public void multiActiveServing() {
    	init();
        executor.configure(Integer.MAX_VALUE, false, false);
        executor.execute();
    }
    
    /**
     * Service that relies on a user-defined policy to extract requests from the queue.
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     * @param hardLimit false if the above limit is applicable only to active (running) threads, but not the waiting ones
     * @param hostReentrant true if re-entrant calls should be hosted on the issuer's thread
     */
    public void policyServing(ServingPolicy policy, int maxActiveThreads, boolean hardLimit, boolean hostReentrant) {
    	init();
        executor.configure(maxActiveThreads, hardLimit, hostReentrant);
        executor.execute(policy);
    }
    
    /**
     * Service that relies on a user-defined policy to extract requests from the queue. Threads are soft-limited and re-entrance on the same thread is disabled.
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     */
    public void policyServing(ServingPolicy policy, int maxActiveThreads) {
    	init();
        executor.configure(maxActiveThreads, false, false);
        executor.execute(policy);
    }
    
    /**
     * Service that relies on a user-defined policy to extract requests from the queue. Threads are not limited and re-entrance on the same thread is disabled.
     * @param maxActiveThreads maximum number of allowed threads inside the multi-active object
     */
    public void policyServing(ServingPolicy policy) {
    	init();
        executor.configure(Integer.MAX_VALUE, false, false);
        executor.execute(policy);
    }
    
    /**
     * Returns the object through which the service's properties can be modified at run-time.
     * @return
     */
    public ServingController getServingController() {
    	//this init runs only once even if invoked many times
    	init(); 
    	
        return executor;
    }
}
