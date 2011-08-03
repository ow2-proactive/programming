package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.multiactivity.compatibility.AnnotationProcessor;
import org.objectweb.proactive.multiactivity.compatibility.CompatibilityTracker;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;


/**
 * This class extends the  {@link Service}  class and adds the capability of serving more methods in parallel. <br> The decision of which methods can run in parallel is made based on annotations set by the user. These annotations are to be found in the <i> org.objectweb.proactive.annotation.multiactivity</i> package.
 * @author  Zsolt Istv�n
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
    
    public void multiActiveServing(int maxActiveThreads){
    	init();
        executor.configure(maxActiveThreads, false, false);
        executor.execute();
        
    }
    
    public void multiActiveServing() {
    	init();
        executor.configure(Runtime.getRuntime().availableProcessors(), false, false);
        executor.execute();
    }
    
/*    private void internalMultiactiveServing() {
        boolean success;
        synchronized (requestQueue) {
            while (body.isActive()) {
                // try to launch next request -- synchronized inside
                success = parallelServeMaxParallel();

                // if we were not successful, let's wait until a new request arrives

                if (!success) {// && (activeServes > 0 || requestQueue.size() == 0)) {
                    try {
                        requestQueue.wait();
                    } catch (InterruptedException e) {
                        //just re-lopp
                    }
                }
            }
        }
    }
*/
    /**
     * Scheduling based on a user-defined policy
     * 
     * @param policy
     */
    public void policyServing(ServingPolicy policy, int maxActiveThreads, boolean hardLimit, boolean hostReentrant) {
    	init();
        executor.configure(maxActiveThreads, hardLimit, hostReentrant);
        executor.execute(policy);
    }
    
    public void policyServing(ServingPolicy policy, int maxActiveThreads) {
    	init();
        executor.configure(maxActiveThreads, false, false);
        executor.execute(policy);
    }
    
    public void policyServing(ServingPolicy policy) {
    	init();
        executor.configure(Runtime.getRuntime().availableProcessors(), false, false);
        executor.execute(policy);
    }
    
    public ServingController getServingController() {
    	//this init runs only once even if invoked many times
    	init(); 
    	
        return executor;
    }
    
  /*  private void internalPolicyServing(ServingPolicy policy) {
        List<Request> chosen;
        int launched;

        synchronized (requestQueue) {
            while (body.isActive()) {

                launched = 0;
                // get the list of requests to run -- as given by the policy

                chosen = policy.runPolicy(compatibility);

                if (chosen != null) {
                    launched = chosen.size();
                    requestQueue.getInternalQueue().removeAll(chosen);
                    startServe(chosen);
                }

                // if we could not launch anything, than we wait until
                // either a running serve finishes or a new req. arrives

                if (launched == 0) { // && (activeServes > 0 || requestQueue.size() == 0)) {
                    try {
                        requestQueue.wait();
                    } catch (InterruptedException e) {
                        //re-loop
                    }
                }
            }
        }
    }

    *//**
     * This method will find the first method in the request queue that is
     * compatible with the currently running methods. In case nothing is
     * executing, it will take the first, thus acting like the default strategy.
     * 
     * @return
     *//*
    private boolean parallelServeFirstCompatible() {
        synchronized (requestQueue) {
            List<Request> reqs = requestQueue.getInternalQueue();
            for (int i = 0; i < reqs.size(); i++) {
                if (compatibility.isCompatibleWithExecuting(reqs.get(i))) {
                    startServe(reqs.remove(i));
                    return true;
                }
            }
        }
        return false;
    }

    *//**
     * This method will find the first method in the request queue that is
     * compatible with the currently running methods. In case nothing is
     * executing, it will take the first, thus acting like the default strategy.
     * 
     * @return
     *//*
    private boolean parallelServeMaxParallel() {

        List<Request> reqs = requestQueue.getInternalQueue();
        
        if (reqs.size()==0) {
            return false;
        }
        
        List<Integer> served = new LinkedList<Integer>();
        if (reqs.size() == 0)
            return false;
        
        for (int i = 0; i < reqs.size(); i++) {
            if (compatibility.isCompatibleWithExecuting(reqs.get(i)) &&
                compatibility.isCompatibleWithRequests(reqs.get(i), reqs.subList(0, i))) {
                served.add(i);
                startServe(reqs.get(i));
            }
        }

        for (int i = 0; i < served.size(); i++) {
            reqs.remove(served.get(i) - i);
        }
        
        return false;
    }

    *//**
     * This method will try to start the oldest waiting method in parallel with
     * the currently running ones. The decision is made based on the information
     * extracted from annotations.
     * 
     * @return whether the oldest request could be started or not
     *//*
    private boolean parallelServeOldest() {

        Request r = requestQueue.removeOldest();
        if (r != null) {
            if (compatibility.isCompatibleWithExecuting(r)) {
                return startServe(r);
            } else {
                // otherwise put it back
                requestQueue.addToFront(r);
            }
        }
        return false;
    }

    protected boolean startServe(Request r) {
        compatibility.addRunning(r);
        //activeServes++;
        //serveTsts.add((int) (new Date().getTime()));
        //serveHistory.add(activeServes);
        threadManager.submit(r);
        return true;
    }
    
    protected boolean startServe(Collection<Request> reqs) {
        for (Request r : reqs) {
            compatibility.addRunning(r);
        }
        
        threadManager.submit(reqs);
        return true;
    }

    *//**
     * Method called from a request wrapper to signal the end of a
     * serving. State is updated here, and the scheduler is announced, so
     * a new request can be scheduled.
     * 
     * @param r
     * @param asserve
     *//*
    public void finished(Request r) {
        synchronized (requestQueue) {
            compatibility.removeRunning(r);
            //activeServes--;
           // serveTsts.add((int) (new Date().getTime()));
            //serveHistory.add(activeServes);
            requestQueue.notifyAll();
        }
    }*/

}
