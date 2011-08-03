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
 * TODO
 * @author  Izso
 */
public class RequestExecutor implements FutureWaiter, ServingController {
    
    protected int ACTIVE_THREAD_LIMIT = Integer.MAX_VALUE;
    protected boolean LIMIT_TOTAL_THREADS = false;
    protected boolean SAME_THREAD_REENTRANT = false;
    
    protected CompatibilityTracker compatibility;
    protected Body body;
    protected RequestQueue requestQueue;
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
     * 
     * @param activeLimit Limit of active serves
     * @param hardLimit If true, the limit will also apply to the number of threads
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
    
    public RequestExecutor(Body body, CompatibilityTracker compatibility, int activeLimit, boolean hardLimit, boolean hostReentrant) {
        this(body, compatibility);
        
        ACTIVE_THREAD_LIMIT = activeLimit;
        LIMIT_TOTAL_THREADS = hardLimit;
        SAME_THREAD_REENTRANT = hostReentrant;
        
        if (SAME_THREAD_REENTRANT) {
            requestTags = new HashMap<String, Set<RunnableRequest>>();
        }

    }
    
    public void configure(int activeLimit, boolean hardLimit, boolean hostReentrant) {
        synchronized (this) {
            
            ACTIVE_THREAD_LIMIT = activeLimit;
            LIMIT_TOTAL_THREADS = hardLimit;
            
            if (SAME_THREAD_REENTRANT!=hostReentrant) {
            	//must check if the tagging mechanism is activated in PA. if it has not been started, we are enable to do same thread re-entrance
                if (hostReentrant==true) {
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

    public void execute(final ServingPolicy policy) {
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                requestQueueHandler(policy);
                
            }
        },"Request listener for "+body).start();
        
        internalExecute();
        
    }

    
    private void requestQueueHandler() {
        synchronized (requestQueue) {
            while (body.isActive()) {
                List<Request> rc;
                rc = getMaxFromQueue();

                if (rc.size() >= 0) {
                    synchronized (this) {
                        for (int i = 0; i < rc.size(); i++) {
                            ready.add(wrapRequest(rc.get(i)));
                        }
                        if (active.size()<ACTIVE_THREAD_LIMIT) {
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
    
    private void requestQueueHandler(ServingPolicy policy) {
        synchronized (requestQueue) {
            while (body.isActive()) {

                List<Request> rc;
                rc = policy.runPolicy(compatibility);

                if (rc.size() >= 0) {
                    synchronized (this) {
                        for (int i = 0; i < rc.size(); i++) {

                            ready.add(wrapRequest(rc.get(i)));
                        }
                        if (active.size()<ACTIVE_THREAD_LIMIT) {
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

    private List<Request> getMaxFromQueue() {
    
        List<Request> reqs = requestQueue.getInternalQueue();
        List<Request> ret = new ArrayList<Request>();
        
        int i, lastIndex;
        for (i = 0; i < reqs.size(); i++) {
            lastIndex = -2;
            if (!invalid.contains(reqs.get(i)) && compatibility.isCompatibleWithExecuting(reqs.get(i)) &&
                (lastIndex = compatibility.getIndexOfLastCompatibleWith(reqs.get(i), reqs.subList(0, i)))==i-1) {
                Request r = reqs.get(i);
                ret.add(r);
                
/*                synchronized (this) {
                    ready.add(wrapRequest(r));
                    this.notify();
                }*/
                
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

    private void internalExecute() {

        synchronized (this) {
            
            while (body.isActive()) {
                
                Iterator<RunnableRequest> i;

                if (SAME_THREAD_REENTRANT) {

                    i = ready.iterator();
                    
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
/*    
    private RunnableRequest findParasiteRequest(RunnableRequest host) {
        for (RunnableRequest r : ready) {
            if (r.getSessionTag()==null || host.getSessionTag()==null) continue;
            
                
            if (r.getSessionTag().equals(host.getSessionTag())) {
                 return r;
            }
        }
        return null;
    }

    private RunnableRequest findHostRequest() {
        
        for (RunnableRequest candidate : waiting) {
            if (isNotAHost(candidate)) {
                return candidate; 
            }
        }

        return null;
    }*/
    
    private boolean isNotAHost(RunnableRequest r) {
        return !hostMap.keySet().contains(r) || (!active.contains(hostMap.get(r)) && !waiting.contains(hostMap.get(r)));
    }
    
    /**
     * Returns true if it may be possible to find a request to be hosted inside an other
     * @return
     */
    private boolean canServeOneHosted() {
        return ready.size()>0 && requestTags.size()>0 && active.size()<ACTIVE_THREAD_LIMIT;
    }

    /**
     * Returns true if it may be possible to resume a previously blocked request
     * @return
     */
    private boolean canResumeOne() {
       return LIMIT_TOTAL_THREADS ? (waiting.size()>0 && hasArrived.size()>0) : (waiting.size()>0 && hasArrived.size()>0 && active.size()<ACTIVE_THREAD_LIMIT);
    }
    
    /**
     * Returns true if there are ready requests and free resources that permit the serving of at least one additional one.
     * @return
     */
    private boolean canServeOne() {
        return LIMIT_TOTAL_THREADS ? (ready.size()>0 && threadUsage.keySet().size()<ACTIVE_THREAD_LIMIT && active.size()<ACTIVE_THREAD_LIMIT) : (ready.size()>0 && active.size()<ACTIVE_THREAD_LIMIT); 
    }
    
/*    private int getEmptySlotCount() {
        return (ACTIVE_THREAD_LIMIT-active.size());
    }*/

    /**
     * Called from the {@link #waitForFuture(Future)} method to signal the blocking of a request.
     * @param r wrapper of the request that starts waiting
     * @param f the future for whose value the wait occured
     */
    private void signalWaitFor(RunnableRequest r, Future f) {
        synchronized (this) {
            ///**/System.out.println("blocked "+r.getRequest().getMethodName()+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
            active.remove(r);
            waiting.add(r);
            if (!waitingList.containsKey(f)) {
                waitingList.put(f.getFutureID(), new LinkedList<RunnableRequest>());
            }
            waitingList.get(f).add(r);

            if (SAME_THREAD_REENTRANT) {
                if (!requestTags.containsKey(r.getSessionTag())) {
                    requestTags.put(r.getSessionTag(), new HashSet<RequestExecutor.RunnableRequest>());
                }
                requestTags.get(r.getSessionTag()).add(r);
            }
            
            
 //           System.out.println("wait" +r.getSessionTag()+ " @ "+Thread.currentThread().getId());
            
            r.setCanRun(false);
            r.setWaitingOn(f);
            this.notify();
        }
    }

    /**
     * Called from the executor's thread to signal a waiting request that it can resume execution.
     * @param r the request's wrapper
     * @param f the future it was waiting for
     */
    private void resumeServing(RunnableRequest r, Future f) {
        synchronized (this) {
            active.add(r);
    
            hostMap.remove(r);
    
            r.setCanRun(true);
            r.setWaitingOn(null);
    
            waitingList.get(f).remove(r);
            if (waitingList.get(f).size() == 0) {
                waitingList.remove(f);
                //TODO -- should clean up the future from the arrived set
                hasArrived.remove(f);
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
            ///**/System.out.println("serving "+r+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
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
            ///**/System.out.println("finished "+r+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
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
                
                signalWaitFor(thisRequest, future);
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
            ///*DEBUG*/ System.out.println("future arrived"+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
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
	 * @author  Izso
	 */
    protected class RunnableRequest implements Runnable {            
        private Request r;
        private boolean canRun = true;
        private Future waitingOn;
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
            return ACTIVE_THREAD_LIMIT;
        }
    }

    @Override
    public int decrementNumberOfConcurrent(int cnt) {
        synchronized (this) {
            if (cnt > 0) {

                ACTIVE_THREAD_LIMIT = (ACTIVE_THREAD_LIMIT > cnt) ? ACTIVE_THREAD_LIMIT - cnt : ACTIVE_THREAD_LIMIT;
                return ACTIVE_THREAD_LIMIT;
            } else {
                return ACTIVE_THREAD_LIMIT;
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
                ACTIVE_THREAD_LIMIT = ACTIVE_THREAD_LIMIT + cnt;
                this.notify();
                return ACTIVE_THREAD_LIMIT;
            } else {
                return ACTIVE_THREAD_LIMIT;
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
                ACTIVE_THREAD_LIMIT = numActive;
                this.notify();
            }
        }
    }
}
