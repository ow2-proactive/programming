package org.objectweb.proactive.multiactivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;

/**
 * This class extends the {@link Service} class and adds the capability of serving
 * more methods in parallel. 
 * <br>The decision of which methods can run in parallel is made
 * based on annotations set by the user. These annotations are to be found in the <i>
 * org.objectweb.proactive.annotation.multiactivity</i> package.
 * @author Izso
 *
 */
public class MultiActiveService extends Service {
	Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * This maps associates to each method a list of active servings
	 */
	private Map<String, List<RequestWrapper>> runningServes = new HashMap<String, List<RequestWrapper>>();
	/**
	 * This is an undirected graph that expresses which methods are compatible.
	 * The information is calculated when the multi-active serving is requested, and to save memory, only
	 * methods which are compatible with at least one other appear here. 
	 */
	private Map<String, List<String>> compatibilityGraph = new HashMap<String, List<String>>();
	
	// stuff for the Policy API
	SchedulerStateImpl schedStateExposer = new SchedulerStateImpl();
	
	public MultiActiveService(Body body) {
		super(body);
		
		MultiActiveAnnotationProcesser maap = new MultiActiveAnnotationProcesser(body.getReifiedObject().getClass());
		compatibilityGraph = maap.getCompatibilityGraph();
	}
	
	/**
     * Invoke the default parallel policy to pick up the requests from the request queue.
     * This does not return until the body terminate, as the active thread enters in
     * an infinite loop for processing the request in the FIFO order, and parallelizing where 
     * possible.
     */
	public void multiActiveServing(){
		boolean success;
		while (body.isAlive()) {
			// try to launch next request -- synchrnoized inside
			success = parallelServeOldestOptimal();
			
			//if we were not successful, let's wait until a new request arrives
			synchronized (requestQueue) {
				if (!success) {
					try {
						requestQueue.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * In this policy the scheduler will always schedule the oldest request in case the object is 
	 * free, and in case methods are already running, it will schedule compatible methods from the
	 * queue.
	 * <br>
	 * <b>ATTENTION:</b> Starvation may be possible.
	 */
	//TODO: maybe add a rejection counter to methods inside, so in case a method was postponed for
	//more than X scheduling cycles, it would need to be chosen, or something like this
	public void greedyMultiActiveServing(){
		boolean success;
		while (body.isAlive()) {
			// try to launch nextc ompatible request -- synchrnoized inside
			success = parallelServeFirstCompatible();
			
			//if we were not successful, let's wait until a new request arrives
			synchronized (requestQueue) {
				if (!success) {
					try {
						requestQueue.wait();
						//logger.info(this.body.getID()+" Greedy scheduler - wake up");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Scheduling based on a user-defined policy
	 * @param policy
	 */
	public void policyServing(ServingPolicy policy) {
		List<RequestWrapper> chosen;
		int launched;;
		
		while (body.isAlive()) {
			synchronized (requestQueue) {
				synchronized (runningServes) {
					launched = 0;
					//get the list of requests to run -- as calculated by the policy
					schedStateExposer.clearSelectedToStart();
					policy.runPolicy(schedStateExposer);
					chosen = schedStateExposer.getSelectedToStart();
					
					for (RequestWrapper mf : chosen) {
						try {
							internalParallelServe(mf.r);
							requestQueue.getInternalQueue().remove(mf.r);
							launched++;
						}
						catch (ClassCastException e) {
							// somebody implemented the RequestWrapper and passed an instance to us
							// this is not good...
						}
						
					}
					
					//if we could not launch anything, that we wait until
					// either a running serve finishes or a new req. arrives
					if (launched==0) {
						try {
							requestQueue.wait();
							//logger.info(this.body.getID()+" Greedy scheduler - wake up");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * This method will find the first method in the request queue that is compatible
	 * with the currently running methods. In case nothing is executing, it will take the 
	 * first, thus acting like the default strategy.
	 * @return
	 */
	public boolean parallelServeFirstCompatible(){
		synchronized (requestQueue) {
			synchronized (runningServes) {
				List<Request> reqs = requestQueue.getInternalQueue();
				for (int i=0; i<reqs.size(); i++) {
					if (canRun(reqs.get(i))){
						internalParallelServe(reqs.remove(i));
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * This method will find the first method in the request queue that is compatible
	 * with the currently running methods. In case nothing is executing, it will take the 
	 * first, thus acting like the default strategy.
	 * @return
	 */
	public boolean parallelServeOldestOptimal(){
		synchronized (requestQueue) {
			synchronized (runningServes) {
				List<Request> reqs = requestQueue.getInternalQueue();
				if (reqs.size()==0) return false;
				
				if (canRun(reqs.get(0))){
					internalParallelServe(reqs.remove(0));
					return true;
				} else if (reqs.size()>1 && areCompatible(reqs.get(0), reqs.get(1)) && canRun(reqs.get(1))){
					internalParallelServe(reqs.remove(1));
					return true;
				} 
			}
		}
		return false;
	}
	
	/**
	 * This method will try to start the oldest waiting method in parallel
	 * with the currently running ones. The decision is made based on the 
	 * information extracted from annotations.
	 * @return whether the oldest request could be started or not
	 */
	public boolean parallelServeOldest(){
		RequestWrapper asserve = null;
		//synchronize both the queue and the running status to be safe from any angle
		synchronized (requestQueue) {
			synchronized (runningServes) {
				
				Request r = requestQueue.removeOldest();
				if (r!=null) {
					if (canRun(r)){
						asserve = internalParallelServe(r);
					} else {
						//otherwise put it back
						requestQueue.addToFront(r);
					}
				}
			}
		}
		return asserve != null;
	}

	private RequestWrapper internalParallelServe(Request r) {
		RequestWrapper asserve;
		//if there is no conflict, prepare launch
		asserve = new RequestWrapper(r);
		
		List<RequestWrapper> aslist = runningServes.get(r.getMethodName());
		if (aslist==null) {
			runningServes.put(r.getMethodName(), new LinkedList<RequestWrapper>());
			aslist = runningServes.get(r.getMethodName());
		}
		aslist.add(asserve);
		
		if (asserve!=null) {
			//logger.info(this.body.getID()+" Parallel serving '"+asserve.r.getMethodName()+"'");
			(new Thread(asserve)).start();
		}
		
		return asserve;
	}
	
	/**
	 * Method called from the {@link RequestWrapper} to signal the end of a serving.
	 * State is updated here, and also a new request will be attempted to be started.
	 * @param r
	 * @param asserve
	 */
	protected void asynchronousServeFinished(Request r, RequestWrapper asserve) {
		synchronized (runningServes) {
			runningServes.get(r.getMethodName()).remove(asserve);
			if (runningServes.get(r.getMethodName()).size()==0) {
				runningServes.remove(r.getMethodName());
			}
		}
		synchronized (requestQueue) {
			//logger.info("Asynchrnous serve finished");
			requestQueue.notifyAll();	
		}
	}

	
	/**
	 * Check if the request conflicts with any of the running requests.
	 * @param r the request
	 * @return true if there are no conflicts
	 */
	protected boolean canRun(Request r) {
		
		if (runningServes.keySet().size()==0) return true;
		
		String request = r.getMethodName();
		for (String s : runningServes.keySet()) {
			if (compatibilityGraph.get(s)==null || !compatibilityGraph.get(s).contains(request)) return false;
		}
		return true;
	}
	
	protected boolean areCompatible(String m1, String m2) {
		List<String> ret = compatibilityGraph.get(m1);
		if (ret==null) return false;
		
		return ret.contains(m2);
	}
	
	protected boolean areCompatible(Request r1, Request r2) {
		return areCompatible(r1.getMethodName(), r2.getMethodName());
	}
	
	/**
	 * By wrapping the request, we can pass the 'method' to the 
	 * outside world without actually exposing internal information. 
	 * @author Izso
	 *
	 */
	protected class RequestWrapper implements Runnable {
		private final Request r;
		
		private RequestWrapper(){
			r = null;
		}
		
		private RequestWrapper(Request r){
			this.r = r;
		}
		
		private Request getRequest(){
			return r;
		}
		
		public String getName() {
			return r.getMethodName();
		}

		public List<String> getCompatibleNames() {
			List<String> ret = new LinkedList<String>();
			ret.addAll(compatibilityGraph.get(this.getName()));
			return ret;
		}

		public boolean isCompatibleWith(RequestWrapper method) {
			return isCompatibleWithName(method.getName());
		}

		public boolean isCompatibleWithName(String methodName) {
			return areCompatible(this.getName(), methodName);
		}

		public boolean isCompatibleWith(List<RequestWrapper> methodList) {
			for (RequestWrapper mf : methodList) {
				if (!isCompatibleWith(mf)) return false;
			}
			return true;
		}

		public boolean isCompatibleWithName(List<String> methodList) {
			for (String mname : methodList) {
				if (!isCompatibleWithName(mname)) return false;
			}
			return true;
		}
		
		@Override
		public void run() {
			body.serve(getRequest());
			asynchronousServeFinished(getRequest(), this);
		}
		
	}
	
	private class SchedulerStateImpl implements SchedulerState {
		private List<RequestWrapper> queueCache;
		private List<RequestWrapper> selectedToStart;
		private boolean userRemovedFromQueue = false; 
		
		@Override
		public List<RequestWrapper> getExecutingMethods() {
			List<RequestWrapper> mlist = new LinkedList<RequestWrapper>();
			for (List<RequestWrapper> lps : runningServes.values()) {
				mlist.addAll(lps);
			}
			return mlist;
		}
		
		@Override
		public List<RequestWrapper> getExecutingMethods(String name) {
			List<RequestWrapper> mlist = new LinkedList<RequestWrapper>();
			if (runningServes.get(name)==null) return mlist;
			
			for (RequestWrapper lps : runningServes.get(name)) {
				mlist.add(lps);
			}
			return mlist;
		}

		@Override
		public RequestWrapper getOldestInTheQueue() {
			Request r = requestQueue.getOldest();
			return (r==null) ? null : new RequestWrapper(r);
		}

		/*
		 * We're doing this magic here because the user *under no circumstances* can change
		 * directly the contents of the queue
		 */
		@Override
		public List<RequestWrapper> getQueueContents() {
			List<Request> rlist = requestQueue.getInternalQueue();
			if (queueCache!=null) {
				int i= userRemovedFromQueue ? 0 : queueCache.size();
				while (i<rlist.size()) {
					if (queueCache.size()>0 && i<queueCache.size()) {
						if ((queueCache.get(i)).getRequest()==rlist.get(i)) {
							i++;
						} else {
							queueCache.remove(i);
						}
					} else {
						queueCache.add(i, new RequestWrapper(rlist.get(i)));
						i++;
					}
				}
				return queueCache;
			} else {
				List<RequestWrapper> mlist = new LinkedList<RequestWrapper>();
				for (int i=0; i<rlist.size(); i++){
					mlist.add(new RequestWrapper(rlist.get(i)));
				}
				queueCache = mlist;
				return queueCache;
			}
		}

		public void clearSelectedToStart() {
			selectedToStart = new LinkedList<RequestWrapper>();
			userRemovedFromQueue = false;
		}

		public List<RequestWrapper> getSelectedToStart() {
			return selectedToStart;
		}

		@Override
		public boolean selectForExecution(List<RequestWrapper> requests) {
			selectedToStart = (requests != null) ? requests : new LinkedList<RequestWrapper>();
			userRemovedFromQueue = selectedToStart.size()>0;
			return true;
		}
		
	}

}
