package org.objectweb.proactive.multiactivity.execution;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.tags.Tag;
import org.objectweb.proactive.core.body.tags.tag.DsiTag;

/**
 * Implementation of the {@link RequestExecutor} interface that is also a {@link FutureWaiter} so it can use the 
 * time spent on wait-by-necessity in one thread to execute something else.
 * @author Izso
 *
 */
public class ControlledRequestExecutor implements RequestExecutor, FutureWaiter, Runnable {
    
    protected int ACTIVE_LIMIT = 1;
    protected boolean HARD_LIMIT_ENABLED = true;
    protected boolean HOST_REENTRANT = true;
    
    protected RequestSupplier listener; 
    
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
    protected ConcurrentHashMap<RunnableRequest, RunnableRequest> hostMap;
    
    /**
     * 
     * @param activeLimit Limit of active serves
     * @param hardLimit If true, the limit will also apply to the number of threads
     */
    public ControlledRequestExecutor(RequestSupplier listener, int activeLimit, boolean hardLimit, boolean hostReentrant) {
        ACTIVE_LIMIT = activeLimit;
        HARD_LIMIT_ENABLED = hardLimit;
        HOST_REENTRANT = hostReentrant;
        
        executorService = Executors.newCachedThreadPool();
        ready = new HashSet<RunnableRequest>();
        active = new HashSet<RunnableRequest>();
        waiting = new HashSet<RunnableRequest>();
        hasArrived = new HashSet<Future>();
        threadUsage = new HashMap<Long, List<RunnableRequest>>();
        waitingList = new HashMap<Future, List<RunnableRequest>>();
        hostMap = new ConcurrentHashMap<RunnableRequest, RunnableRequest>();
        
        this.listener = listener;
        
        (new Thread((Runnable) this, "ThreadManager of "+listener.getServingBody())).start();
    }

    @Override
    public synchronized void submit(Request r) {
        ///*DEGUB*/System.out.println("submitted one"+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
        ready.add(wrapRequest(r));
        this.notify();
    }
    
    @Override
    public  void submit(Collection<Request> reqs) {
        List<RunnableRequest> wraps = new LinkedList<ControlledRequestExecutor.RunnableRequest>();
        for (Request r : reqs) {
            wraps.add(wrapRequest(r));
        }
        
        synchronized (this) {
            ready.addAll(wraps);
            this.notify();
        }
    }
    /**
     * This is the heart of the executor. It is an internal scheduling thread that coordinates wake-ups, and waits and future value arrivals.
     */
    public synchronized void run() {
        Body body = listener.getServingBody();
        //TODO replace for a better one
        while (body.isActive()) {

            ///*DEGUB*/System.out.println("r"+ready.size() + " a"+active.size() +" w"+waiting.size() + " f"+hasArrived.size() +" in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");

            //WAKE any waiting thread that could resume execution and there are free resources for it

            Iterator<RunnableRequest> i = waiting.iterator();
            while (canResumeOne() && i.hasNext()) {

                RunnableRequest cont = i.next();
                // check if the future has arrived + the request is not already engaged in a hosted serving
                if (hasArrived.contains(cont.getWaitingOn()) && isNotAHost(cont)) {

                    synchronized (cont) {
                        i.remove();
                        resumeServing(cont, cont.getWaitingOn());
                        cont.notify();
                    }
                    ///*DEGUB*/System.out.println("resumed one"+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
                }
            }

            //SERVE anyone who is ready and there are resources available
            i = ready.iterator();

            while (canServeOne() && i.hasNext()) {

                RunnableRequest next = i.next();
                i.remove();
                active.add(next);

                executorService.execute(next);
                //(new Thread(next, "Serving thread for " + listener.getServingBody())).start();
                ///*DEGUB*/System.out.println("activate one"+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
            }

            if (HOST_REENTRANT) {

                //HOST a request inside a blocked one's thread
                RunnableRequest parasite;
                RunnableRequest host;
                boolean couldStart = true;

                //find a request suitable for serving (preferably a recursion)
                while (canServeOneHosted() && couldStart) {
                    host = findHostRequest();

                    if (host != null) {
                        parasite = findParasiteRequest(host);

                        if (parasite != null) {

                            synchronized (host) {
                                //i.remove();
                                ready.remove(parasite);
                                active.add(parasite);
                                hostMap.put(host, parasite);
                                host.notify();
                                // /*DEGUB*/System.out.println("hosted one"+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
                            }
                        } else {
                            couldStart = false;
                        }
                    } else {
                        couldStart = false;
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
    
    private RunnableRequest findParasiteRequest(RunnableRequest host) {
        Tag tag;
        
        for (RunnableRequest r : ready) {
            if (r.getRequest().getTags().getTag(DsiTag.IDENTIFIER)==null) continue;
            
            tag = r.getRequest().getTags().getTag(DsiTag.IDENTIFIER);
            
            
            if (host.getRequest().getTags().getTag(DsiTag.IDENTIFIER)==null) continue;
                
            String[] hostData = ((String) (host.getRequest().getTags().getTag(DsiTag.IDENTIFIER).getData())).split("::"); 
            String[] parasiteData = ((String) (tag.getData())).split("::");
                
            if (hostData[0].equals(parasiteData[0]) && hostData[1].equals(parasiteData[1])) {
                 return r;
            }
        }
        
        return null;
    }

    private boolean isNotAHost(RunnableRequest r) {
        return !hostMap.keySet().contains(r) || (!active.contains(hostMap.get(r)) && !waiting.contains(hostMap.get(r)));
    }

    private RunnableRequest findHostRequest() {
        
        for (RunnableRequest candidate : waiting) {
            if (isNotAHost(candidate)) {
                return candidate; 
            }
        }

        return null;
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
    private synchronized void signalWaitFor(RunnableRequest r, Future f) {
        ///**/System.out.println("blocked "+r.getRequest().getMethodName()+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
        active.remove(r);
        waiting.add(r);
        if (!waitingList.containsKey(f)) {
            waitingList.put(f, new LinkedList<RunnableRequest>());
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
        
        hostMap.remove(r);
        
        r.setCanRun(true);
        r.setWaitingOn(null);
        
        waitingList.get(f).remove(r);
        if (waitingList.get(f).size()==0) {
            waitingList.remove(f);
            //TODO -- should clean up the future from the arrived set
            hasArrived.remove(f);
        }
    }
    
    /**
     * Tell the executor about the creation of a new thread, of the current usage of
     * an already existing thread.
     * @param r
     */
    private synchronized void registerServerThread(RunnableRequest r) {
        ///**/System.out.println("serving "+r+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
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
        ///**/System.out.println("finished "+r+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
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
    public synchronized void futureArrived(Future future) {
        ///*DEBUG*/ System.out.println("future arrived"+ " in "+listener.getServingBody().getID().hashCode()+ "("+listener.getServingBody()+")");
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
            listener.getServingBody().serve(r);
            listener.finished(r);
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
