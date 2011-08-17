package org.objectweb.proactive.multiactivity.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureID;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.body.tags.Tag;
import org.objectweb.proactive.core.body.tags.tag.DsiTag;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.multiactivity.ServingController;
import org.objectweb.proactive.multiactivity.ServingPolicy;
import org.objectweb.proactive.multiactivity.compatibility.CompatibilityTracker;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor.RunnableRequest;
/**
 * The request executor that constitutes the multi-active service.
 * It contains two management threads: one listens to the queue and applies the schedulign policy, while the other manages the execution of requests on threads.
 * @author  Zsolt Istvan
 */
public class RequestExecutor implements FutureWaiter, ServingController {
    
	/**
	 * Number of concurrent threads allowed
	 */
    protected int THREAD_LIMIT = Integer.MAX_VALUE;
    /**
     * If set to true, then the THREAD_LIMIT refers to the total number of serves. 
     * If false then it refers to actively executing serves, not the waiting by necessity ones.
     */
    protected boolean LIMIT_TOTAL_THREADS = false;
    /**
     * If true re-entrant calls will be hosted on the same thread as their source.
     * If false than all serves will be served on separate threads.
     */
    protected boolean SAME_THREAD_REENTRANT = false;
    
    protected CompatibilityTracker compatibility;
    protected Body body;
    protected RequestQueue requestQueue;
    
    /**
     * Threadpool
     */
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
    protected HashSet<FutureID> hasArrived;
    
    /**
     * Associates with each thread a list of requests which represents the
     * stack of execution inside the thread. Only the top level request can be
     * active.
     */
    protected HashMap<Long, List<RunnableRequest>> threadUsage;
    
    /**
     * Associates a session-tag with a set of requests -- the ones which are part of the same execution path.
     */
    protected HashMap<String, Set<RunnableRequest>> requestTags;
    
    /**
     * List of requests waiting for the value of a future
     */
    protected HashMap<FutureID, List<RunnableRequest>> waitingList;
    
    /**
     * Pairs of requests meaning which is hosting which inside it. Hosting means
     * that when a wait by necessity occurs the first request will perform a serving of
     * the second request instead of waiting for the future. It will 'resume' the waiting when
     * the second request finishes execution
     */
    protected ConcurrentHashMap<RunnableRequest, RunnableRequest> hostMap;
    
    
    protected HashSet<Request> invalid = new HashSet<Request>();
    
    protected HashMap<Request, Set<Request>> invalidates = new HashMap<Request, Set<Request>>();
    
    /**
     * Default constructor. 
     * @param body Body of the active object.
     * @param compatibility Compatibility information of the active object's class
     */
    public RequestExecutor(Body body, CompatibilityTracker compatibility) {
        this.compatibility = compatibility;
        this.body = body;
        this.requestQueue = body.getRequestQueue();
        
        executorService = Executors.newCachedThreadPool();
        ready = new HashSet<RunnableRequest>();
        active = new HashSet<RunnableRequest>();
        waiting = new HashSet<RunnableRequest>();
        hasArrived = new HashSet<FutureID>();
        threadUsage = new HashMap<Long, List<RunnableRequest>>();
        waitingList = new HashMap<FutureID, List<RunnableRequest>>();
        hostMap = new ConcurrentHashMap<RunnableRequest, RunnableRequest>();
        
        FutureWaiterRegistry.putForBody(body.getID(), (FutureWaiter) this);
        
    }
    
    /**
     * Constructor with all options.
     * @param body Body of the active object
     * @param compatibility Compatibility information of the active object's class
     * @param activeLimit thread limit
     * @param hardLimit hard or soft limit (limiting total nb of threads, or only those which are active) 
     * @param hostReentrant whether to serve re-entrant calls on the same thread as their source
     */
    public RequestExecutor(Body body, CompatibilityTracker compatibility, int activeLimit, boolean hardLimit, boolean hostReentrant) {
        this(body, compatibility);
        
        THREAD_LIMIT = activeLimit;
        LIMIT_TOTAL_THREADS = hardLimit;
        SAME_THREAD_REENTRANT = hostReentrant;
        
        if (SAME_THREAD_REENTRANT) {
            requestTags = new HashMap<String, Set<RunnableRequest>>();
        }

    }
    
    /**
     * Method for changing the limits inside the executor during runtime.
     * @param activeLimit thread limit
     * @param hardLimit hard or soft limit (limiting total nb of threads, or only those which are active) 
     * @param hostReentrant whether to serve re-entrant calls on the same thread as their source
     */
    public void configure(int activeLimit, boolean hardLimit, boolean hostReentrant) {
        synchronized (this) {
            
            THREAD_LIMIT = activeLimit;
            LIMIT_TOTAL_THREADS = hardLimit;
            
            if (SAME_THREAD_REENTRANT!=hostReentrant) {
                if (hostReentrant==true) {
                	//must check if the tagging mechanism is activated in PA. if it has not been started, we are enable to do same thread re-entrance
                	if (CentralPAPropertyRepository.PA_TAG_DSF.isTrue()) {
                		SAME_THREAD_REENTRANT = hostReentrant;
                        //'create the map and populate it with tags
                        requestTags = new HashMap<String, Set<RunnableRequest>>();
                        for (RunnableRequest r : waiting) {
                            if (isNotAHost(r)) {
                                if (!requestTags.containsKey(r.getSessionTag())) {
                                    requestTags.put(r.getSessionTag(), new HashSet<RequestExecutor.RunnableRequest>());
                                }
                                requestTags.get(r.getSessionTag()).add(r);
                            }
                        }
                	} else {
                		requestTags = null;
                		Logger.getLogger(Loggers.MAO).error("Same thread re-entrance was requested, but property 'PA_TAG_DSF' is set to false");
                	}
                } else {
                    //clean up
                    requestTags = null;
                }
            }
            
            this.notify();
        }
    }

    /**
     * This is the heart of the executor. It is an internal scheduling thread that coordinates wake-ups, and waits and future value arrivals.
     * Before doing that it also starts a thread for the queue handler.
     */
    public void execute() {
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                requestQueueHandler();
                
            }
        },"Request listener for "+body).start();
        
        internalExecute();
    }

    /**
     * This is the heart of the executor. It is an internal scheduling thread that coordinates wake-ups, and waits and future value arrivals.
     * Before doing that it also starts a thread for the queue handler that uses a custom policy for scheduling.
     * @param policy
     */
    public void execute(final ServingPolicy policy) {
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                requestQueueHandler(policy);
                
            }
        },"Request listener for "+body).start();
        
        internalExecute();
        
    }

    
    /**
     * Method which schedules requests with the default policy.
     */
    private void requestQueueHandler() {
    	
    	//register thread, so we can look up the Body if needed
    	LocalBodyStore.getInstance().pushContext(new Context(body, null));
    	
        synchronized (requestQueue) {
            while (body.isActive()) {
            	
            	//get compatible ones from the queue
                List<Request> rc;
                rc = runDefaultPolicy();

                if (rc.size() >= 0) {
                    synchronized (this) {
                    	//add them to the ready set
                        for (int i = 0; i < rc.size(); i++) {
                            ready.add(wrapRequest(rc.get(i)));
                        }
                        
                        //if anything can be done, let the other thread know
                        if (active.size()<THREAD_LIMIT) {
                            this.notify();
                        }
                    }
                }
                
                try {
                    requestQueue.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
            }
        }
    }
    
    /**
     * Method that retrieves the compatible requests from the queue 
     * based on a custom policy.
     * @param policy
     */
    private void requestQueueHandler(ServingPolicy policy) {
    	
    	//register thread, so we can look up the Body if needed
    	LocalBodyStore.getInstance().pushContext(new Context(body, null));
    	
        synchronized (requestQueue) {
            while (body.isActive()) {

            	//get compatible ones from the queue using the policy
                List<Request> rc;
                rc = policy.runPolicy(compatibility);

                if (rc.size() >= 0) {
                    synchronized (this) {
                    	//add them to the ready set
                        for (int i = 0; i < rc.size(); i++) {
                            ready.add(wrapRequest(rc.get(i)));
                        }
                        
                        //if anything can be done, let the other thread know
                        if (active.size()<THREAD_LIMIT) {
                            this.notify();
                        }
                    }
                }

                try {
                    requestQueue.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Default scheduling policy. <br>
     * It will take a request from the queue if it is compatible with all executing ones and also with everyone before it in the queue.
     * If a request can not be taken out from the queue, the requests it is invalid with are marked accordingly so that they are not retried until this one is finally served.
     * @return
     */
    private List<Request> runDefaultPolicy() {
    
        List<Request> reqs = requestQueue.getInternalQueue();
        List<Request> ret = new ArrayList<Request>();
        
        int i, lastIndex;
        for (i = 0; i < reqs.size(); i++) {
            lastIndex = -2;
            if (!invalid.contains(reqs.get(i)) && compatibility.isCompatibleWithExecuting(reqs.get(i)) &&
                (lastIndex = compatibility.getIndexOfLastCompatibleWith(reqs.get(i), reqs.subList(0, i)))==i-1) {
                Request r = reqs.get(i);
                ret.add(r);
                
                compatibility.addRunning(r);
                
                if (invalidates.containsKey(reqs.get(i))){
                    for (Request ok: invalidates.get(reqs.get(i))) {
                        invalid.remove(ok);
                    }
                    invalidates.remove(reqs.get(i));
                }
                
                reqs.remove(i);
                i--;
                
            } else if (lastIndex>-2 && lastIndex<i){
                lastIndex++;
                if (!invalidates.containsKey(reqs.get(lastIndex))){
                    invalidates.put(reqs.get(lastIndex), new HashSet<Request>());
                }
                
                invalidates.get(reqs.get(lastIndex)).add(reqs.get(i));
                invalid.add(reqs.get(i));
            }
        }
        
        return ret;
    }

    /**
     * Serving and Thread management.
     */
    private void internalExecute() {

        synchronized (this) {
            
            while (body.isActive()) {
                
                Iterator<RunnableRequest> i;

                if (SAME_THREAD_REENTRANT) {

                    i = ready.iterator();
                    //see if we can serve a request on the thread of an other one
                    while (canServeOneHosted() && i.hasNext()) {
                        RunnableRequest parasite = i.next();
                        String tag = parasite.getSessionTag();
                    	if (tag!=null) {
                            if (requestTags.containsKey(tag)) {
                                for (RunnableRequest host : requestTags.get(tag)) {
                                    if (host != null && isNotAHost(host)) {
                                        synchronized (host) {
                                            i.remove();
                                            active.add(parasite);
                                            hostMap.put(host, parasite);
                                            requestTags.get(tag).remove(host);
                                            parasite.setHostedOn(host);
                                            host.notify();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                //WAKE any waiting thread that could resume execution and there are free resources for it
                //i = waiting.iterator();
                Iterator<List<RunnableRequest>> it = threadUsage.values().iterator();
                
                while (canResumeOne() && it.hasNext()) {

                    List<RunnableRequest> list = it.next();
                    RunnableRequest cont = list.get(0);
                    // check if the future has arrived + the request is not already engaged in a hosted serving
                    if (hasArrived.contains(cont.getWaitingOn()) && isNotAHost(cont)) {

                        synchronized (cont) {
                            //i.remove();
                            waiting.remove(cont);
                            resumeServing(cont, cont.getWaitingOn());
                            cont.notify();
                        }
                    }
                }

                //SERVE anyone who is ready and there are resources available
                i = ready.iterator();

                while (canServeOne() && i.hasNext()) {

                    RunnableRequest next = i.next();
                    i.remove();
                    active.add(next);
                    executorService.execute(next);
                }
                
                
                //System.out.println("r" + ready.size());
                
                //SLEEP if nothing else to do
                //      will wake up on 1) new submit, 2) finish of a request, 3) arrival of a future, 4) wait of a request
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    /**
     * Returns true if the request is not hosting any other serve in its thread.
     * @param r
     * @return
     */
    private boolean isNotAHost(RunnableRequest r) {
        return !hostMap.keySet().contains(r) || (!active.contains(hostMap.get(r)) && !waiting.contains(hostMap.get(r)));
    }
    
    /**
     * Returns true if it may be possible to find a request to be hosted inside an other
     * @return
     */
    private boolean canServeOneHosted() {
        return ready.size()>0 && requestTags.size()>0 && active.size()<THREAD_LIMIT;
    }

    /**
     * Returns true if it may be possible to resume a previously blocked request
     * @return
     */
    private boolean canResumeOne() {
       return LIMIT_TOTAL_THREADS ? (waiting.size()>0 && hasArrived.size()>0) : (waiting.size()>0 && hasArrived.size()>0 && active.size()<THREAD_LIMIT);
    }
    
    /**
     * Returns true if there are ready requests and free resources that permit the serving of at least one additional one.
     * @return
     */
    private boolean canServeOne() {
        return LIMIT_TOTAL_THREADS ? (ready.size()>0 && threadUsage.keySet().size()<THREAD_LIMIT && active.size()<THREAD_LIMIT) : (ready.size()>0 && active.size()<THREAD_LIMIT); 
    }

    /**
     * Called from the {@link #waitForFuture(Future)} method to signal the blocking of a request.
     * @param r wrapper of the request that starts waiting
     * @param f the future for whose value the wait occured
     */
    private void signalWaitFor(RunnableRequest r, FutureID fId) {
        synchronized (this) {
            active.remove(r);
            waiting.add(r);
            if (!waitingList.containsKey(fId)) {
                waitingList.put(fId, new LinkedList<RunnableRequest>());
            }
            waitingList.get(fId).add(r);

            if (SAME_THREAD_REENTRANT) {
                if (!requestTags.containsKey(r.getSessionTag())) {
                    requestTags.put(r.getSessionTag(), new HashSet<RequestExecutor.RunnableRequest>());
                }
                requestTags.get(r.getSessionTag()).add(r);
            }
            
            r.setCanRun(false);
            r.setWaitingOn(fId);
            this.notify();
        }
    }

    /**
     * Called from the executor's thread to signal a waiting request that it can resume execution.
     * @param r the request's wrapper
     * @param fId the future it was waiting for
     */
    private void resumeServing(RunnableRequest r, FutureID fId) {
        synchronized (this) {
            active.add(r);
    
            hostMap.remove(r);
    
            r.setCanRun(true);
            r.setWaitingOn(null);
    
            waitingList.get(fId).remove(r);
            if (waitingList.get(fId).size() == 0) {
                waitingList.remove(fId);
                hasArrived.remove(fId);
            }
    
            if (SAME_THREAD_REENTRANT) {
                String sessionTag = r.getSessionTag();
                if (sessionTag != null) {
                    requestTags.get(sessionTag).remove(r);
                    if (requestTags.get(sessionTag).size()==0) {
                        requestTags.remove(sessionTag);
                    }
                }
            }
        }
    }
    
    /**
     * Tell the executor about the creation of a new thread, of the current usage of
     * an already existing thread.
     * @param r
     */
    private void serveStarted(RunnableRequest r) {
        synchronized (this) {
            Long tId = Thread.currentThread().getId();
            if (!threadUsage.containsKey(tId)) {
                threadUsage.put(tId, new LinkedList<RunnableRequest>());
            }
            threadUsage.get(tId).add(0, r);
        }
    }
    
    /**
     * Tell the executor about the termination, or updated usage stack of a thread.
     * @param r
     */
    private void serveStopped(RunnableRequest r) {
        synchronized (this) {
            active.remove(r);

            Long tId = Thread.currentThread().getId();
            if (!r.equals(threadUsage.get(tId).remove(0))) {
                System.err.println("Thread inconsistency -- Request is not found in the stack.");
            }

            if (threadUsage.get(tId).size() == 0) {
                threadUsage.remove(tId);
            }
            
            if (SAME_THREAD_REENTRANT) {
                if (r.getHostedOn()!=null) {
                    if (!requestTags.containsKey(r.getHostedOn().getSessionTag())) {
                        requestTags.put(r.getHostedOn().getSessionTag(), new HashSet<RequestExecutor.RunnableRequest>());
                    }
                    requestTags.get(r.getSessionTag()).add(r.getHostedOn());
                }
            }
            this.notify();
        }
    }

    @Override
    public void waitForFuture(Future future) {
        RunnableRequest thisRequest = threadUsage.get(Thread.currentThread().getId()).get(0);
        synchronized (thisRequest) {
            synchronized (future) {
                if (((FutureProxy) future).isAvailable()) {
                    return;
                }
                
                signalWaitFor(thisRequest, future.getFutureID());
            }

            while (!thisRequest.canRun()) {

                try {
                    thisRequest.wait();
                } catch (InterruptedException e) {
                    //  TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (hostMap.containsKey(thisRequest) && hostMap.get(thisRequest) != null) {
                    hostMap.get(thisRequest).run();
                }

            }
        }

    }

    @Override
    public void futureArrived(Future future) {
        synchronized (this) {
            hasArrived.add(future.getFutureID());
            this.notify();
        }
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
	 * Wrapper class for a request. Apart from the actual serving it also performs calls for thread registering and unregistering inside the executor and callback after termination in the wrapping service. 
	 * @author  Zsolt Istvan
	 */
    protected class RunnableRequest implements Runnable {            
        private Request r;
        private boolean canRun = true;
        private FutureID waitingOn;
        private RunnableRequest hostedOn;
        private String sessionTag;

        public RunnableRequest(Request r) {
            this.r = r;
        }

        public Request getRequest() {
            return r;
        }

        @Override
        public void run() {
            serveStarted(this);
            body.serve(r);
            synchronized (requestQueue) {
                serveStopped(this);
                compatibility.removeRunning(r);
                requestQueue.notify();
            }
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
        public void setWaitingOn(FutureID waitingOn) {
            this.waitingOn = waitingOn;
        }

        /**
         * Find out on what future's value is this request performing
         * a wait-by-necessity.
         * @return
         */
        public FutureID getWaitingOn() {
            return waitingOn;
        }

        private void setSessionTag(String sessionTag) {
            this.sessionTag = sessionTag;
        }

        public String getSessionTag() {
            if (sessionTag!=null) {
                return sessionTag;
            } else {
            	Tag tag = getRequest().getTags().getTag(DsiTag.IDENTIFIER);
            	if (tag!=null) {
            		Object tagObject = tag.getData();
            		if (tagObject != null) {
            			String[] tagData = ((String) (tagObject)).split("::");
            			setSessionTag(tagData[0] + "::" + tagData[1]);
            		}
            	}
                return sessionTag;
            }
        }

        public void setHostedOn(RunnableRequest hostedOn) {
            this.hostedOn = hostedOn;
        }

        public RunnableRequest getHostedOn() {
            return hostedOn;
        }

    }

    @Override
    public int getNumberOfConcurrent() {
        synchronized (this) {
            return THREAD_LIMIT;
        }
    }

    @Override
    public int decrementNumberOfConcurrent(int cnt) {
        synchronized (this) {
            if (cnt > 0) {

                THREAD_LIMIT = (THREAD_LIMIT > cnt) ? THREAD_LIMIT - cnt : THREAD_LIMIT;
                return THREAD_LIMIT;
            } else {
                return THREAD_LIMIT;
            }
        }
    }
    
    public int decrementNumberOfConcurrent() {
        return decrementNumberOfConcurrent(1);
    }

    @Override
    public int incrementNumberOfConcurrent(int cnt) {
        synchronized (this) {
            if (cnt > 0) {
                THREAD_LIMIT = THREAD_LIMIT + cnt;
                this.notify();
                return THREAD_LIMIT;
            } else {
                return THREAD_LIMIT;
            }
        }
    }

    @Override
    public int incrementNumberOfConcurrent() {
        return incrementNumberOfConcurrent(1);
    }
    
    @Override
    public void setNumberOfConcurrent(int numActive) {
        synchronized (this) {
            if (numActive>0) {
                THREAD_LIMIT = numActive;
                this.notify();
            }
        }
    }
}
