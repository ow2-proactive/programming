package org.objectweb.proactive.multiactivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Map<String, List<Request>> runningServes = new HashMap<String, List<Request>>();

	//TODO: comment
	private MultiActiveCompatibilityMap compatibilityMap;
	
	// stuff for the Policy API
	SchedulerStateImpl schedStateExposer = new SchedulerStateImpl();
	
	public MultiActiveService(Body body) {
		super(body);
		
		compatibilityMap = MultiActiveCompatibilityMap.getFor(body.getReifiedObject().getClass());
	}
	
	/**
     * Invoke the default parallel policy to pick up the requests from the request queue.
     * This does not return until the body terminate, as the active thread enters in
     * an infinite loop for processing the request in the FIFO order, and parallelizing where 
     * possible.
     */
	public void multiActiveServing(){
		boolean success;
		while (body.isActive()) {
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
	 * Scheduling based on a user-defined policy
	 * @param policy
	 */
	public void policyServing(ServingPolicy policy) {
		List<Request> chosen;
		int launched;;
		
		while (body.isActive()) {
			synchronized (requestQueue) {
				launched = 0;
				// get the list of requests to run -- as calculated by the
				// policy
				chosen = policy.runPolicy(schedStateExposer, compatibilityMap);
				if (chosen!=null) {
					for (Request mf : chosen) {
						try {
							internalParallelServe(mf);
							requestQueue.getInternalQueue().remove(mf);
							launched++;
						} catch (ClassCastException e) {
							// somebody implemented the RequestWrapper and passed an
							// instance to us
							// this is not good...
						}
	
					}
				}
				// if we could not launch anything, that we wait until
				// either a running serve finishes or a new req. arrives
				if (launched == 0) {
					try {
						requestQueue.wait();
						// logger.info(this.body.getID()+" Greedy scheduler - wake up");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
			List<Request> reqs = requestQueue.getInternalQueue();
			for (int i = 0; i < reqs.size(); i++) {
				if (compatibilityMap.isCompatibleWithAllNames(reqs.get(i).getMethodName(), runningServes.keySet())) {
					internalParallelServe(reqs.remove(i));
					return true;
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
			List<Request> reqs = requestQueue.getInternalQueue();
			if (reqs.size() == 0)
				return false;

			if (compatibilityMap.isCompatibleWithAllNames(reqs.get(0).getMethodName(), runningServes.keySet())) {
				internalParallelServe(reqs.remove(0));
				return true;
			} else if (reqs.size() > 1
					&& compatibilityMap.areCompatible(reqs.get(0), reqs.get(1))
					&& compatibilityMap.isCompatibleWithAllNames(reqs.get(1).getMethodName(), runningServes.keySet())) {
				internalParallelServe(reqs.remove(1));
				return true;
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
		RunnableRequest asserve = null;
		//synchronize both the queue and the running status to be safe from any angle
		synchronized (requestQueue) {

			Request r = requestQueue.removeOldest();
			if (r != null) {
				if (compatibilityMap.isCompatibleWithAllNames(r.getMethodName(), runningServes.keySet())) {
					asserve = internalParallelServe(r);
				} else {
					// otherwise put it back
					requestQueue.addToFront(r);
				}
			}
		}
		return asserve != null;
	}
	
	public void startServe(Request r) {
		internalParallelServe(r);
	}

	protected RunnableRequest internalParallelServe(Request r) {
		RunnableRequest asserve;
		//if there is no conflict, prepare launch
		asserve = new RunnableRequest(r);
		
		List<Request> aslist = runningServes.get(r.getMethodName());
		if (aslist==null) {
			runningServes.put(r.getMethodName(), new LinkedList<Request>());
			aslist = runningServes.get(r.getMethodName());
		}
		aslist.add(r);
		
		if (asserve!=null) {
			//logger.info(this.body.getID()+" Parallel serving '"+asserve.r.getMethodName()+"'");
			(new Thread(asserve)).start();
		}
		
		return asserve;
	}
	
	/**
	 * Method called from the {@link RunnableRequest} to signal the end of a serving.
	 * State is updated here, and also a new request will be attempted to be started.
	 * @param r
	 * @param asserve
	 */
	protected void asynchronousServeFinished(Request r) {
		synchronized (requestQueue) {
			runningServes.get(r.getMethodName()).remove(r);
			if (runningServes.get(r.getMethodName()).size()==0) {
				runningServes.remove(r.getMethodName());
			}
			requestQueue.notifyAll();	
		}
	}

	/**
	 * By wrapping the request, we can pass the 'method' to the 
	 * outside world without actually exposing internal information. 
	 * @author Izso
	 *
	 */
	private class RunnableRequest implements Runnable {
		private final Request r;
		
		public RunnableRequest(Request r){
			this.r = r;
		}
		
		public Request getRequest(){
			return r;
		}
		
		@Override
		public void run() {
			body.serve(getRequest());
			asynchronousServeFinished(getRequest());
		}
		
	}
	
	private class SchedulerStateImpl implements SchedulerState {
		
		public Set<String> getExecutingMethodNameSet(){
			return runningServes.keySet();
		}

		@Override
		public List<String> getExecutingMethodNames() {
			List<String> names = new LinkedList<String>();
			for (String m : runningServes.keySet()) {
				for (int i=0; i<runningServes.get(m).size(); i++) {
					names.add(m);
				}
			}
			
			return names;
		}

		@Override
		public List<Request> getExecutingRequests() {
			List<Request> reqs = new LinkedList<Request>();
			for (List<Request> lrr : runningServes.values()) {
				reqs.addAll(lrr);
			}
			
			return reqs;
		}

		@Override
		public List<Request> getExecutingRequestsFor(String method) {
			return (runningServes.containsKey(method)) ? runningServes.get(method) : new LinkedList<Request>();
		}

		@Override
		public Request getOldestInTheQueue() {
			return requestQueue.getOldest();
		}

		@Override
		public List<Request> getQueueContents() {
			return requestQueue.getInternalQueue();
		}
		
	}
	
}
