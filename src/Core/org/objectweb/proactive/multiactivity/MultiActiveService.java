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
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.Request;

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
public class MultiActiveService extends Service {

    public int activeServes = 0;
    public LinkedList<Integer> serveHistory = new LinkedList<Integer>();
    public LinkedList<Integer> serveTsts = new LinkedList<Integer>();

    Logger logger = Logger.getLogger(this.getClass());

    CompatibilityTracker compatibility;
    ThreadManager threadManager;

    public MultiActiveService(Body body) {
        super(body);

        compatibility = new CompatibilityTracker(new AnnotationProcessor(body.getReifiedObject().getClass()));
        threadManager = new NoLimitTM();

    }
    
    public MultiActiveService(Body body, int maxActiveThreads) {
        super(body);

        compatibility = new CompatibilityTracker(new AnnotationProcessor(body.getReifiedObject().getClass()));
        threadManager = new LimitActiveTM(maxActiveThreads);

        FutureListenerRegistry.put(body.getID(), (FutureListener) threadManager);

    }

    /**
     * Invoke the default parallel policy to pick up the requests from the
     * request queue. This does not return until the body terminate, as the
     * active thread enters in an infinite loop for processing the request in
     * the FIFO order, and parallelizing where possible.
     */
    public void multiActiveServing() {
        boolean success;

        while (body.isActive()) {
            // try to launch next request -- synchronized inside
            success = parallelServeMaxParallel();

            // if we were not successful, let's wait until a new request arrives
            synchronized (requestQueue) {
                if (!success && (activeServes > 0 || requestQueue.size() == 0)) {
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

        while (body.isActive()) {

            launched = 0;
            // get the list of requests to run -- as given by the policy
            synchronized (requestQueue) {
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

                if (launched == 0 && (activeServes > 0 || requestQueue.size() == 0)) {
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

        synchronized (requestQueue) {
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

            /*
             * if (compatibility.isCompatibleWithExecuting(reqs.get(0))) {
             * internalParallelServe(reqs.remove(0)); return true; } else if (reqs.size() > 1 &&
             * compatibility.areCompatible(reqs.get(0), reqs.get(1)) &&
             * compatibility.isCompatibleWithExecuting(reqs.get(1))) {
             * internalParallelServe(reqs.remove(1)); return true; }
             */
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

        synchronized (requestQueue) {

            Request r = requestQueue.removeOldest();
            if (r != null) {
                if (compatibility.isCompatibleWithExecuting(r)) {
                    return internalParallelServe(r);
                } else {
                    // otherwise put it back
                    requestQueue.addToFront(r);
                }
            }
        }
        return false;
    }

    public void startServe(Request r) {
        internalParallelServe(r);
    }

    protected boolean internalParallelServe(Request r) {

        threadManager.submit(r);
        compatibility.addRunning(r);
        activeServes++;
        serveTsts.add((int) (new Date().getTime()));
        serveHistory.add(activeServes);

        return true;
    }

    /**
     * Method called from the {@link RunnableRequest} to signal the end of a
     * serving. State is updated here, and also a new request will be attempted
     * to be started.
     * 
     * @param r
     * @param asserve
     */
    protected void asynchronousServeFinished(Request r) {
        synchronized (requestQueue) {
            compatibility.removeRunning(r);
            requestQueue.notifyAll();
            activeServes--;
            serveTsts.add((int) (new Date().getTime()));
            serveHistory.add(activeServes);
        }
    }

    protected class CompatibilityTracker extends SchedulerCompatibilityMap {
        private Map<MethodGroup, Integer> compats = new HashMap<MethodGroup, Integer>();
        private Map<String, List<Request>> runningMethods = new HashMap<String, List<Request>>();
        private int runningCount = 0;

        public CompatibilityTracker(AnnotationProcessor annotProc) {
            super(annotProc);

            for (MethodGroup groupName : groups.values()) {
                compats.put(groupName, 0);
            }
        }

        /*
         * adds the request to the running set adds one to the compatibility count of all groups
         * this request's group is compatible with
         * 
         * Time: O(g) -- g is the number of groups
         */
        protected void addRunning(Request request) {
            String method = request.getMethodName();
            if (methods.containsKey(method)) {
                for (MethodGroup mg : methods.get(method).getCompatibleWith()) {
                    compats.put(mg, compats.get(mg) + 1);
                }
            }
            if (!runningMethods.containsKey(method)) {
                runningMethods.put(method, new LinkedList<Request>());
            }
            runningMethods.get(method).add(request);
            runningCount++;
        }

        /*
         * removes the request from the running set removes one from the compatibility count of all
         * groups this request's group is compatible with
         * 
         * Time: O(g) -- g is the number of groups
         */
        protected void removeRunning(Request request) {
            String method = request.getMethodName();
            if (methods.containsKey(method)) {
                for (MethodGroup mg : methods.get(method).getCompatibleWith()) {
                    compats.put(mg, compats.get(mg) - 1);
                }
            }
            runningMethods.get(method).remove(request);
            runningCount--;
        }

        @Override
        public boolean isCompatibleWithExecuting(Request r) {
            return isCompatibleWithExecuting(r.getMethodName());
        }

        /*
         * Alternative for the isCompatibleWithRequests in case we check with the running ones. This
         * will return the answer in O(1) time, as opposed to O(n) worst-case time of the other.
         * Thsi works by checking if the count of compatible methods is equal to the number of
         * running methods. The count is stored for each group and updated upon addition/removal of
         * requests to/from the running set.
         * 
         * Time: O(1)
         */
        public boolean isCompatibleWithExecuting(String method) {
            if (runningCount == 0)
                return true;

            MethodGroup mg = methods.get(method);
            return (mg != null && compats.containsKey(mg)) && (compats.get(mg) == runningCount);
        }

        public Set<String> getExecutingMethodNameSet() {
            return runningMethods.keySet();
        }

        @Override
        public List<String> getExecutingMethodNames() {
            List<String> names = new LinkedList<String>();
            for (String m : runningMethods.keySet()) {
                for (int i = 0; i < runningMethods.get(m).size(); i++) {
                    names.add(m);
                }
            }

            return names;
        }

        @Override
        public List<Request> getExecutingRequests() {
            List<Request> reqs = new LinkedList<Request>();
            for (List<Request> lrr : runningMethods.values()) {
                reqs.addAll(lrr);
            }

            return reqs;
        }

        @Override
        public List<Request> getExecutingRequestsFor(String method) {
            return (runningMethods.containsKey(method)) ? runningMethods.get(method)
                    : new LinkedList<Request>();
        }

        @Override
        public Request getOldestInTheQueue() {
            return requestQueue.getOldest();
        }

        @Override
        public List<Request> getQueueContents() {
            return requestQueue.getInternalQueue();
        }

        @Override
        public int getNumberOfExecutingRequests() {
            return runningCount;
        }

    }

    public class LimitActiveTM implements ThreadManager, FutureListener {
        protected int MAX_ACTIVE = 1;
        private ExecutorService executorService = Executors.newCachedThreadPool();

        protected HashSet<RunnableRequest> ready = new HashSet<RunnableRequest>();
        protected HashSet<RunnableRequest> active = new HashSet<RunnableRequest>();
        protected HashSet<RunnableRequest> waiting = new HashSet<RunnableRequest>();

        protected HashMap<Long, RunnableRequest> threads = new HashMap<Long, RunnableRequest>();
        protected HashMap<Future, Integer> missedNotifs = new HashMap<Future, Integer>();

        public LimitActiveTM() {
        }
        
        public LimitActiveTM(int maxActiveThreads) {
            MAX_ACTIVE = maxActiveThreads;
        }

        /*
        private boolean activateOtherInline(RunnableRequest r) {
            //return false;
            RunnableRequest toRun = null;
            synchronized (this) {
                if (ready.size() > 0) {
                    toRun = findInlineFor(r);
                    if (toRun!=null) {
                        ready.remove(toRun);
                        active.add(toRun);
                    }
                }
            }
            
            if (toRun!=null) {
                //System.out.println("                                    INLINE");
                toRun.run();
                return true;
            }
            
            //if (canServeOther() && ready.size()==0){
                //System.out.println("no-one else to activate");\
                wakeMissedNotifs();
                return false;
            //}
            
            //return false;
        }
        
        private RunnableRequest findInlineFor(RunnableRequest r) {
            Iterator<RunnableRequest> i = ready.iterator();
            while (i.hasNext()) {
                RunnableRequest candidate = i.next();
                //if (candidate.getRequest().getMethodName().equals(r.getRequest().getMethodName())) {
                    return candidate;
                //}
            }
            
            return null;
        }*/
        
        @Override
        public int getNumberOfReady() {
            return ready.size();
        }

        @Override
        public int getNumberOfActive() {
            return active.size();
        }

        @Override
        public int getNumberOfWaiting() {
            return waiting.size();
        }

        protected boolean canServeOther() {
            return (getNumberOfActive() < MAX_ACTIVE || MAX_ACTIVE == -1);
        }

        @Override
        public void submit(Request request) {
            ///* */System.out.println("submit "+request.getMethodName()+" in "+body.getID().hashCode());
            RunnableRequest r = wrapRequest(request);
            synchronized (this) {
                if (canServeOther()) {
                    serve(r);
                    return;
                } else {
                    wakeMissedNotifs();
                    ready.add(r);
                }
            }
        }

        protected void serve(RunnableRequest r) {
            ready.remove(r);
            active.add(r);
            //System.out.println("found to activate "+r.getRequest().getMethodName()+" in "+body.getID().hashCode());
            executorService.submit(r);
        }

        protected boolean serveOneOnNewThread() {
            if (ready.size() > 0) {
                serve(ready.iterator().next());
                return true;
            }

            return false;
        }
        
        protected boolean cleanupAfter(RunnableRequest r) {
            boolean result;
            synchronized (this) {
                result = active.remove(r) || waiting.remove(r) || ready.remove(r);
                if (canServeOther()) {
                    if (!wakeMissedNotifs()) {
                        serveOneOnNewThread();
                    }
                    return true;
                }
            }
            return result;
        }

        protected boolean yield() {
            synchronized (this) {
                if (canServeOther()) {
                    if (!serveOneOnNewThread()) {
                        wakeMissedNotifs();
                    }
                    return true;
                }
            }
            return false;
        }

        public void arrived(Future future) {
            ///* */System.out.println("arrived result for "+future+" in "+body.getID().hashCode());
            //futureState.put(future, true);
            future.notifyAll();
        }

        @Override
        public void waitingFor(Future future) {
            //find out who I am
            RunnableRequest r = threads.get(Thread.currentThread().getId());

            active.remove(r);
            waiting.add(r);

            //activateOther();
            //give the opportunity to others

            boolean canRun = false;
            boolean firstWake = true;

            if (((FutureProxy) future).isAvailable()) {
                return;
            }

            while (!canRun) {

                yield();
                
                synchronized (this) {
                    if (canServeOther() && ((FutureProxy) future).isAvailable()) {
                        waiting.remove(r);
                        active.add(r);
                        canRun = true;
                        continue;
                    }
                }
                
                if (firstWake) {
                    addMissedNotify(future);
                }

                synchronized (future) {
                    //TODO -- check can serve other                    
                    
                    try {
                        ///* */System.out.println("sleeping "+r.r.getMethodName()+" in "+body.getID().hashCode());
                        future.wait();
                        ///* */System.out.println("woke up "+r.r.getMethodName()+" in "+body.getID().hashCode());
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted");
                    }

                    //check if I can come back
                    firstWake = false;
                }

            }

            if (!firstWake) {
                removeMissedNotify(future);
            }

        }

        protected boolean wakeMissedNotifs() {
            List<Future> copy = new LinkedList<Future>();
            synchronized (missedNotifs) {
                for (Future f : missedNotifs.keySet()) {
                    copy.add(f);
                }
            }
            for (Future f : copy) {
                synchronized (f) {
                    f.notifyAll();
                }
            }
            
            return copy.size()>0;
        }

        protected void addMissedNotify(Future future) {
            synchronized (missedNotifs) {
                if (!missedNotifs.containsKey(future)) {
                    missedNotifs.put(future, 0);
                }
                missedNotifs.put(future, missedNotifs.get(future) + 1);
            }
        }

        protected void removeMissedNotify(Future future) {
            synchronized (missedNotifs) {
                Integer cnt = missedNotifs.get(future);
                if (cnt != null && cnt > 1) {
                    missedNotifs.put(future, cnt - 1);
                } else {
                    missedNotifs.remove(future);
                }
            }
        }

        public void registerThread(RunnableRequest runnableRequest) {
            threads.put(Thread.currentThread().getId(), runnableRequest);
        }
        
        protected RunnableRequest wrapRequest(Request request) {
            return new RunnableRequest(request);
        }

        /**
         * By wrapping the request, we can pass the 'method' to the outside world
         * without actually exposing internal information.
         * 
         * @author Zsolt István
         * 
         */
        protected class RunnableRequest implements Runnable {
            private Request r;

            public RunnableRequest(Request r) {
                this.r = r;
            }

            public Request getRequest() {
                return r;
            }

            @Override
            public void run() {
                ///* */System.out.println("serving "+r.getMethodName()+" --- a"+getNumberOfActive()+"/w"+getNumberOfWaiting()+"/r"+getNumberOfReady()+" in "+body.getID().hashCode());
                registerThread(this);
                body.serve(r);
                cleanupAfter(this);
                asynchronousServeFinished(r);
                //activateOtherInline();
                //System.out.println("finished "+r.getMethodName()+" --- a"+getNumberOfActive()+"/w"+getNumberOfWaiting()+"/r"+getNumberOfReady()+" in "+body.getID().hashCode());
            }

        }

    }
    
    public class NoLimitTM implements ThreadManager {
        private ExecutorService executorService = Executors.newCachedThreadPool();
        private AtomicInteger active = new AtomicInteger(0);

        @Override
        public void submit(Request r) {
            active.incrementAndGet();
            executorService.submit(new RunnableRequest(r));
        }

        @Override
        public int getNumberOfReady() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getNumberOfActive() {
            // TODO Auto-generated method stub
            return active.get();
        }

        @Override
        public int getNumberOfWaiting() {
            // TODO Auto-generated method stub
            return 0;
        }
        
        private class RunnableRequest implements Runnable {
            private Request r;

            public RunnableRequest(Request r) {
                this.r = r;
            }

            public Request getRequest() {
                return r;
            }

            @Override
            public void run() {
                body.serve(r);
                asynchronousServeFinished(r);
            }

        }

    }

}
