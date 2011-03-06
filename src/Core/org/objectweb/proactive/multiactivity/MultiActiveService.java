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
    RequestExecutor threadManager;

    /**
     * MultiActiveService that will be able to optionally use a policy, and will deploy each serving request on a 
     * separate physical thread.
     * @param body
     */
    public MultiActiveService(Body body) {
        super(body);

        compatibility = new CompatibilityTracker(new AnnotationProcessor(body.getReifiedObject().getClass()));
        threadManager = new RequestExecutorMock();

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
        
        threadManager = new RequestExecutorImpl(maxActiveThreads, false);

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
        
        threadManager = new RequestExecutorImpl(maxActiveThreads, hardLimit);

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
        synchronized (requestQueue) {
            compatibility.addRunning(r);
            activeServes++;
            serveTsts.add((int) (new Date().getTime()));
            serveHistory.add(activeServes);
            threadManager.submit(r);
        }
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
    protected void asynchronousServeFinished(Request r) {
        synchronized (requestQueue) {
            compatibility.removeRunning(r);
            activeServes--;
            serveTsts.add((int) (new Date().getTime()));
            serveHistory.add(activeServes);
            
            requestQueue.notifyAll();
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

    
    /**
     * Implementation of the {@link RequestExecutor} interface that is also a {@link FutureWaiter} so it can use the 
     * time spent on wait-by-necessity in one thread to execute something else.
     * @author Izso
     *
     */
    public class RequestExecutorImpl implements RequestExecutor, FutureWaiter, Runnable {
        
        protected int ACTIVE_LIMIT = 1;
        protected boolean HARD_LIMIT_ENABLED = true;
        
        protected ExecutorService executorService;
        
        /**
         * Requests submitted but not started yet
         */
        protected HashSet<RunnableRequest> ready;
        
        /**
         * Requests currently being executed.
         */
        protected HashSet<RunnableRequest> active;
        
        /**
         * Requests blocked on some event.
         */
        protected HashSet<RunnableRequest> waiting;
        
        /**
         * Set of futures whose values have already arrived
         */
        protected HashSet<Future> hasArrived;
        
        /**
         * Associates with each thread a list of requests which represents the
         * stack of execution inside the thread. Only the top level request can be
         * active.
         */
        protected HashMap<Long, List<RunnableRequest>> threadUsage;
        
        /**
         * List of requests waiting for the value of a future
         */
        protected HashMap<Future, List<RunnableRequest>> waitingList;
        
        /**
         * Pairs of requests meaning which is hosting which inside it. Hosting means
         * that when a wait by necessity occurs the first request will perform a serving of
         * the second request instead of waiting for the future. It will 'resume' the waiting when
         * the second request finishes execution
         */
        protected HashMap<RunnableRequest, RunnableRequest> hostMap;
        
        /**
         * 
         * @param activeLimit Limit of active serves
         * @param hardLimit If true, the limit will also apply to the number of threads
         */
        public RequestExecutorImpl(int activeLimit, boolean hardLimit) {
            ACTIVE_LIMIT = activeLimit;
            HARD_LIMIT_ENABLED = hardLimit;
            
            executorService = hardLimit ? Executors.newFixedThreadPool(activeLimit) : Executors.newCachedThreadPool();
            ready = new HashSet<RunnableRequest>();
            active = new HashSet<RunnableRequest>();
            waiting = new HashSet<RunnableRequest>();
            hasArrived = new HashSet<Future>();
            threadUsage = new HashMap<Long, List<RunnableRequest>>();
            waitingList = new HashMap<Future, List<RunnableRequest>>();
            hostMap = new HashMap<RunnableRequest, RunnableRequest>();
            
            (new Thread((Runnable) this, "ThreadManager of "+body)).start();
        }

        @Override
        public synchronized void submit(Request r) {
            ///*DEGUB*/System.out.println("submitted one"+ " in "+body.getID().hashCode()+ "("+body+")");
            ready.add(wrapRequest(r));
            this.notify();
        }
        
        /**
         * This is the heart of the executor. It is an internal scheduling thread that coordinates wake-ups, and waits and future value arrivals.
         */
        public synchronized void run() {
            //TODO replace for a better one
            while (body.isActive()) {
                ///*DEGUB*/System.out.println("r"+ready.size() + " a"+active.size() +" w"+waiting.size() + " f"+hasArrived.size() +" in "+body.getID().hashCode()+ "("+body+")");
                
                //WAKE any waiting thread that could resume execution and there are free resources for it
                Iterator<RunnableRequest> i = waiting.iterator();
                while (canResumeOne() && i.hasNext()) {
                    RunnableRequest cont = i.next();
                    // check if the future has arrived + the request is not already engaged in a hosted serving
                    if (hasArrived.contains(cont.getWaitingOn()) &&
                        (!hostMap.keySet().contains(cont) || (!active.contains(hostMap.get(cont)) && !waiting.contains(hostMap.get(cont))))) {
                        synchronized (cont) {
                            i.remove();
                            resumeServing(cont, cont.getWaitingOn());
                            cont.notify();
                        }
                        ///*DEGUB*/System.out.println("resumed one"+ " in "+body.getID().hashCode()+ "("+body+")");
                    }
                }

                //SERVE anyone who is ready and there are resources available
                i = ready.iterator();
                while (canServeOne() && i.hasNext()) {
                    RunnableRequest next = i.next();
                    i.remove();
                    active.add(next);
                    executorService.submit(next);
                    ///*DEGUB*/System.out.println("activate one"+ " in "+body.getID().hashCode()+ "("+body+")");
                }

                if (HARD_LIMIT_ENABLED) {
                    //HOST a request inside a blocked one's thread
                    i = waiting.iterator();
                    while (canServeOneHosted() && i.hasNext()) {
                        RunnableRequest host = i.next();
                        //look for requests whose future did not arrive and are not already hosting
                        if (!hasArrived.contains(host.getWaitingOn()) &&
                            (!hostMap.keySet().contains(host) || (!active.contains(hostMap.get(host)) && !waiting.contains(hostMap.get(host))))) {
                            //find a request suitable for serving inside the host
                            RunnableRequest parasite = findParasiteRequest(host);
                            
                            if (parasite != null) {
                                synchronized (host) {
                                    ready.remove(parasite);
                                    active.add(parasite);
                                    hostMap.put(host, parasite);
                                    host.notify();
                                    ///*DEGUB*/System.out.println("hosted one"+ " in "+body.getID().hashCode()+ "("+body+")");
                                }
                            }
                        }
                    }
                }

                //SLEEP if nothing else to do
                //      will wake up on 1) new submit, 2) finish of a request, 3) arrival of a future, 4) wait of a request
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Find a request that can be safely executed on the thread of the host request.
         * @param host The request who ran on the thread until it got blocked.
         * @return
         */
        private RunnableRequest findParasiteRequest(RunnableRequest host) {
            return ready.iterator().next();            
        }
        
        /**
         * Returns true if it may be possible to find a request to be hosted inside an other
         * @return
         */
        private boolean canServeOneHosted() {
            return ready.size()>0 && waiting.size()>0 && active.size()<ACTIVE_LIMIT;
        }

        /**
         * Returns true if it may be possible to resume a previously blocked request
         * @return
         */
        private boolean canResumeOne() {
           return HARD_LIMIT_ENABLED ? (waiting.size()>0 && hasArrived.size()>0) : (waiting.size()>0 && hasArrived.size()>0 && active.size()<ACTIVE_LIMIT);
        }
        
        /**
         * Returns true if there are ready requests and free resources hat permit the serving of at least one additional one.
         * @return
         */
        private boolean canServeOne() {
            return HARD_LIMIT_ENABLED ? (ready.size()>0 && threadUsage.keySet().size()<ACTIVE_LIMIT && active.size()<ACTIVE_LIMIT) : (ready.size()>0 && active.size()<ACTIVE_LIMIT); 
        }
        
        
        /**
         * Called from the {@link #waitForFuture(Future)} method to signal the blocking of a request.
         * @param r wrapper of the request that starts waiting
         * @param f the future for whose value the wait occured
         */
        private synchronized void pauseServing(RunnableRequest r, Future f) {
            ///**/System.out.println("blocked "+r.getRequest().getMethodName()+ " in "+body.getID().hashCode()+ "("+body+")");
            active.remove(r);
            waiting.add(r);
            if (!waitingList.containsKey(f)) {
                waitingList.put(f, new LinkedList<RunnableRequest>());
                //TOOD -- check if future has arrived -- useful in cases somebody comes after the first wave of waiters has all finished
            }
            waitingList.get(f).add(r);
            
            r.setCanRun(false);
            r.setWaitingOn(f);
            this.notify();
        }
        
        /**
         * Called from the executor's thread to signal a waiting request that it can resume execution.
         * @param r the request's wrapper
         * @param f the future it was waiting for
         */
        private synchronized void resumeServing(RunnableRequest r, Future f) {
            active.add(r);
            
            r.setCanRun(true);
            r.setWaitingOn(null);
            
            waitingList.get(f).remove(r);
            if (waitingList.get(f).size()==0) {
                waitingList.remove(f);
                //TODO -- should clean up the future from the arrived set
            }
        }
        
        /**
         * Tell the executor about the creation of a new thread, of the current usage of
         * an already existing thread.
         * @param r
         */
        private synchronized void registerServerThread(RunnableRequest r) {
            ///**/System.out.println("serving "+r+ " in "+body.getID().hashCode()+ "("+body+")");
            Long tId = Thread.currentThread().getId();
            if (!threadUsage.containsKey(tId)) {
                threadUsage.put(tId, new LinkedList<RunnableRequest>());
            }
            threadUsage.get(tId).add(0, r);
        }
        
        /**
         * Tell the executor about the termination, or updated usage stack of a thread.
         * @param r
         */
        private synchronized void unregisterServerThread(RunnableRequest r) {
            ///**/System.out.println("finished "+r+ " in "+body.getID().hashCode()+ "("+body+")");
            active.remove(r);
            
            Long tId = Thread.currentThread().getId();
            if (!r.equals(threadUsage.get(tId).remove(0))) {
                System.err.println("Thread inconsistency -- Request is not found in the stack.");
            }
            if (threadUsage.get(tId).size()==0) {
                threadUsage.remove(tId);
            }
            this.notify();
        }

        @Override
        public void waitForFuture(Future future) {
            RunnableRequest me = threadUsage.get(Thread.currentThread().getId()).get(0);
            synchronized (me) {
                if (((FutureProxy) future).isAvailable()) {
                    //value already arrived
                    return;
                }

                pauseServing(me, future);

                while (!me.canRun()) {

                    try {
                        me.wait();
                    } catch (InterruptedException e) {
                        //  TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (hostMap.containsKey(me) && hostMap.get(me) != null) {
                        hostMap.get(me).run();
                    }

                }
            }

        }

        @Override
        public synchronized void futureArrived(Future future) {
            ///*DEBUG*/ System.out.println("future arrived"+ " in "+body.getID().hashCode()+ "("+body+")");
            hasArrived.add(future);
            this.notify();
        }

        @Override
        public synchronized int getNumberOfReady() {
            return ready.size();
        }

        @Override
        public synchronized int getNumberOfActive() {
            return active.size();
        }

        @Override
        public synchronized int getNumberOfWaiting() {
            return waiting.size();
        }
        
        /**
         * Makes a request runnable on a separate thread, by wrapping it in an instance of {@link RunnableRequest}.
         * @param request
         * @return
         */
        protected RunnableRequest wrapRequest(Request request) {
            return new RunnableRequest(request);
        }
        
        /**
         * Wrapper class for a request. Apart from the actual serving it also performs calls
         * for thread registering and unregistering inside the executor and callback after termination
         * in the wrapping service. 
         * @author Izso
         *
         */
        protected class RunnableRequest implements Runnable {            
            private Request r;
            private boolean canRun = true;
            private Future waitingOn;

            public RunnableRequest(Request r) {
                this.r = r;
            }

            public Request getRequest() {
                return r;
            }

            @Override
            public void run() {
                registerServerThread(this);
                body.serve(r);
                asynchronousServeFinished(r);
                unregisterServerThread(this);
            }
            
            @Override
            public String toString() {
                return "Wrapper for "+r.toString();
            }

            /**
             * Tell this request that is can carry on executing, because the event it has been
             * waiting for has arrived.
             * @param canRun
             */
            public void setCanRun(boolean canRun) {
                this.canRun = canRun;
            }

            /**
             * Check whether the inner request can carry on the execution.
             * @return
             */
            public boolean canRun() {
                return canRun;
            }

            /**
             * Tell the outer world on what future's value is this request performing
             * a wait-by-necessity.
             * @param waitingOn
             */
            public void setWaitingOn(Future waitingOn) {
                this.waitingOn = waitingOn;
            }

            /**
             * Find out on what future's value is this request performing
             * a wait-by-necessity.
             * @return
             */
            public Future getWaitingOn() {
                return waitingOn;
            }

        }
    }
    
    /**
     * Basic {@link RequestExecutor} that will start every submitted request on a new 
     * thread. Upon completion a callback method is called inside the service.
     * @author Izso
     *
     */
    public class RequestExecutorMock implements RequestExecutor {
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
        
        /**
         * Wrapper that is used for the parallel serving of requests
         * @author Izso
         *
         */
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
