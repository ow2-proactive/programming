package org.objectweb.proactive.multiactivity;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.AnnotationProcessor;
import org.objectweb.proactive.multiactivity.compatibility.CompatibilityTracker;
import org.objectweb.proactive.multiactivity.compatibility.MethodGroup;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;
import org.objectweb.proactive.multiactivity.execution.FutureWaiter;
import org.objectweb.proactive.multiactivity.execution.FutureWaiterRegistry;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;
import org.objectweb.proactive.multiactivity.execution.ControlledRequestExecutor;
import org.objectweb.proactive.multiactivity.execution.MinimalRequestExecutor;
import org.objectweb.proactive.multiactivity.execution.RequestSupplier;

import sun.security.action.GetBooleanAction;


/**
 * This class extends the {@link Service} class and adds the capability of
 * serving more methods in parallel. <br>
 * The decision of which methods can run in parallel is made based on
 * annotations set by the user. These annotations are to be found in the <i>
 * org.objectweb.proactive.annotation.multiactivity</i> package.
 * 
 * @author Zsolt István
 * 
 */
public class MultiActiveService extends Service implements RequestSupplier {

    public int activeServes = 0;
    public LinkedList<Integer> serveHistory = new LinkedList<Integer>();
    public LinkedList<Integer> serveTsts = new LinkedList<Integer>();

    Logger logger = Logger.getLogger(this.getClass());

    CompatibilityTracker compatibility;
    RequestExecutor threadManager;

    /**
     * MultiActiveService that will be able to optionally use a policy, and will deploy each serving request on a 
     * separate physical thread.
     * @param body
     */
    public MultiActiveService(Body body) {
        super(body);

        compatibility = new CompatibilityTracker(new AnnotationProcessor(body.getReifiedObject().getClass()), requestQueue);
        threadManager = new MinimalRequestExecutor(this);

    }
    
    /**
     * MultiActiveService that will be able to optionally use a policy, and will control the deployment of requests on  
     * physical threads. The maximum number of concurrently active servings is limited. The total
     * number of started threads is unbounded.
     * @param body
     * @param maxActiveThreads maximum number of active serves
     */
    public MultiActiveService(Body body, int maxActiveThreads) {
        this(body);
        
        threadManager = new ControlledRequestExecutor(this, maxActiveThreads, false);

        FutureWaiterRegistry.putForBody(body.getID(), (FutureWaiter) threadManager);

    }
    
    /**
     * MultiActiveService that will be able to optionally use a policy, and will control the deployment of requests on  
     * physical threads. The maximum number of concurrently active servings or physical threads 
     * is limited -- depending on the value of a flag.
     * @param body
     * @param maxActiveThreads number of maximum active serves (hardLimit is false), or maximum number of threads (hardLimit is true)
     * @param hardLimit flag for limiting the number of physical threads or only of those which are active.
     */
    public MultiActiveService(Body body, int maxActiveThreads, boolean hardLimit) {
        this(body);
        
        threadManager = new ControlledRequestExecutor(this, maxActiveThreads, hardLimit);

        FutureWaiterRegistry.putForBody(body.getID(), (FutureWaiter) threadManager);

    }

    /**
     * Invoke the default parallel policy to pick up the requests from the
     * request queue. This does not return until the body terminate, as the
     * active thread enters in an infinite loop for processing the request in
     * the FIFO order, and parallelizing where possible.
     */
    public void multiActiveServing() {
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

    /**
     * Scheduling based on a user-defined policy
     * 
     * @param policy
     */
    public void policyServing(ServingPolicy policy) {
        List<Request> chosen;
        int launched;

        synchronized (requestQueue) {
            while (body.isActive()) {

                launched = 0;
                // get the list of requests to run -- as given by the policy

                chosen = policy.runPolicy(compatibility);

                if (chosen != null) {
                    for (Request mf : chosen) {
                        internalParallelServe(mf);
                        requestQueue.getInternalQueue().remove(mf);
                        launched++;
                    }
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

    /**
     * This method will find the first method in the request queue that is
     * compatible with the currently running methods. In case nothing is
     * executing, it will take the first, thus acting like the default strategy.
     * 
     * @return
     */
    public boolean parallelServeFirstCompatible() {
        synchronized (requestQueue) {
            List<Request> reqs = requestQueue.getInternalQueue();
            for (int i = 0; i < reqs.size(); i++) {
                if (compatibility.isCompatibleWithExecuting(reqs.get(i))) {
                    internalParallelServe(reqs.remove(i));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method will find the first method in the request queue that is
     * compatible with the currently running methods. In case nothing is
     * executing, it will take the first, thus acting like the default strategy.
     * 
     * @return
     */
    public boolean parallelServeMaxParallel() {

        List<Request> reqs = requestQueue.getInternalQueue();
        List<Integer> served = new LinkedList<Integer>();
        if (reqs.size() == 0)
            return false;

        for (int i = 0; i < reqs.size(); i++) {
            if (compatibility.isCompatibleWithExecuting(reqs.get(i)) &&
                compatibility.isCompatibleWithRequests(reqs.get(i), reqs.subList(0, i))) {
                served.add(i);
                internalParallelServe(reqs.get(i));
            }
        }

        for (int i = 0; i < served.size(); i++) {
            reqs.remove(served.get(i) - i);
        }
        return false;
    }

    /**
     * This method will try to start the oldest waiting method in parallel with
     * the currently running ones. The decision is made based on the information
     * extracted from annotations.
     * 
     * @return whether the oldest request could be started or not
     */
    public boolean parallelServeOldest() {

        Request r = requestQueue.removeOldest();
        if (r != null) {
            if (compatibility.isCompatibleWithExecuting(r)) {
                return internalParallelServe(r);
            } else {
                // otherwise put it back
                requestQueue.addToFront(r);
            }
        }
        return false;
    }

    public void startServe(Request r) {
        internalParallelServe(r);
    }

    protected boolean internalParallelServe(Request r) {
        compatibility.addRunning(r);
        activeServes++;
        serveTsts.add((int) (new Date().getTime()));
        serveHistory.add(activeServes);
        threadManager.submit(r);
        return true;
    }

    /**
     * Method called from a request wrapper to signal the end of a
     * serving. State is updated here, and the scheduler is announced, so
     * a new request can be scheduled.
     * 
     * @param r
     * @param asserve
     */
    public void finished(Request r) {
        synchronized (requestQueue) {
            compatibility.removeRunning(r);
            activeServes--;
            serveTsts.add((int) (new Date().getTime()));
            serveHistory.add(activeServes);
            requestQueue.notifyAll();
        }
    }
    
    @Override
    public Body getServingBody() {
        return body;
    }

}
