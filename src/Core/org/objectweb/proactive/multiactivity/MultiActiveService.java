package org.objectweb.proactive.multiactivity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;
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
/*
 * TODO: Most probably will have to remove the two interfaces from here, so ppl will not be able
 * to mess around with internals (syncros) 
 */
public class MultiActiveService extends Service implements SchedulerState, CompatibilityGraph {
	Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * This maps associates to each method a list of active servings
	 */
	public Map<String, List<ParallelServe>> runningServes = new HashMap<String, List<ParallelServe>>();
	/**
	 * This is an undirected graph that expresses which methods are compatible.
	 * The information is calculated when the multi-active serving is requested, and to save memory, only
	 * methods which are compatible with at least one other appear here. 
	 */
	public Map<String, List<String>> compatibilityGraph = new HashMap<String, List<String>>();
	/**
	 * Information contained in the annotations is read and stored in this structure.
	 */
	public Map<String, MultiActiveAnnotations> methodInfo = new HashMap<String, MultiActiveAnnotations>();
	
	public MultiActiveService(Body body) {
		super(body);
		
		populateMethodInfo();
		initCompatibilityGraph();
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
			success = parallelServeOldest();
			
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
		List<MethodFacade> chosen;
		RequestWrapper rw;
		int launched;;
		
		while (body.isAlive()) {
			synchronized (requestQueue) {
				synchronized (runningServes) {
					launched = 0;
					//get the list of requests to run -- as calculated by the policy
					chosen = policy.selectToRun(this, this);
					
					for (MethodFacade mf : chosen) {
						try {
							//for the outside world these are method facades, 
							//but we know they have a request inside
							rw = (RequestWrapper) mf;
							internalParallelServe(rw.r);
							requestQueue.getInternalQueue().remove(rw.r);
							launched++;
						}
						catch (ClassCastException e) {
							// somebody implemented the MethodFacade and passed an instance to us
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
	 * This method will try to start the oldest waiting method in parallel
	 * with the currently running ones. The decision is made based on the 
	 * information extracted from annotations.
	 * @return whether the oldest request could be started or not
	 */
	public boolean parallelServeOldest(){
		ParallelServe asserve = null;
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

	private ParallelServe internalParallelServe(Request r) {
		ParallelServe asserve;
		//if there is no conflict, prepare launch
		asserve = new ParallelServe(r);
		
		List<ParallelServe> aslist = runningServes.get(r.getMethodName());
		if (aslist==null) {
			runningServes.put(r.getMethodName(), new LinkedList<ParallelServe>());
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
	 * Method called from the {@link ParallelServe} to signal the end of a serving.
	 * State is updated here, and also a new request will be attempted to be started.
	 * @param r
	 * @param asserve
	 */
	protected void asynchronousServeFinished(Request r, ParallelServe asserve) {
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
	 * This method will iterate through all methods from the underlying class, and
	 * create a descriptor object containing all annotations extracted. This object
	 * is put into the {@link #methodInfo} structure.
	 */
	protected void populateMethodInfo() {
		try {
			for (Method d : (Class.forName(body.getReifiedClassName())).getMethods()) {
				if (methodInfo.get(d.getName())==null) {
					methodInfo.put(d.getName(), new MultiActiveAnnotations());
				}
				
				CompatibleWith cw = d.getAnnotation(CompatibleWith.class);
				if (cw!=null) {
					methodInfo.get(d.getName()).setCompatibleWith(cw);
				}
				
				Modifies mo = d.getAnnotation(Modifies.class);
				if (mo!=null) {
					methodInfo.get(d.getName()).setModifies(mo);
				}
				
				Reads re = d.getAnnotation(Reads.class);
				if (re!=null) {
					methodInfo.get(d.getName()).setReads(re);
				}
				//System.out.println(d.getName()+" "+cw+" "+mo+" "+re);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the compatibility graph based on the annotations.
	 * @return
	 */
	private void initCompatibilityGraph() {
		Map<String, List<String>> graph  = new HashMap<String, List<String>>();
		//for all methods
		for (String method : methodInfo.keySet()) {
			graph.put(method, new ArrayList<String>());
			
			String[] compList = methodInfo.get(method).getCompatibleWith();
			//if the user set some methods as compatible, put them into the neighbor list
			if (compList!=null) {
				for (String cm : compList) {
					if (!cm.equals(MultiActiveAnnotations.ALL)) {
						graph.get(method).add(cm);
					} else {
						graph.get(method).addAll(graph.keySet());
					}
				}
			}
		}
		
		//check for bidirectionality of relations. if a compatibleWith is only
		// expressed in one direction, it is deleted
		for (String method : graph.keySet()) {
			Iterator<String> sit = graph.get(method).iterator();
			while (sit.hasNext()) {
				String other = sit.next();
				if (!graph.get(other).contains(method)) {
					sit.remove();
				}
			}
		}
		
		//process the read-write stuff
		for (String method : methodInfo.keySet()){
			for (String other : methodInfo.keySet()) {				
				Boolean areOk = areDisjoint(method, other);
				if (Boolean.TRUE.equals(areOk)) {
					graph.get(method).add(other);
					graph.get(other).add(method);
				} else if (Boolean.FALSE.equals(areOk)) {
					graph.get(method).remove(other);
					graph.get(other).remove(method);
				} // else - if null - leave like it is
				//System.out.println("Testing-["+method+"]-["+other+"]->"+areOk);
			}
		}
		
		//to save space, all no-neighbour methods are removed from the graph
		Iterator<String> sit = graph.keySet().iterator();
		while (sit.hasNext()) {
			if (graph.get(sit.next()).size()==0) {
				sit.remove();
			}
		}
		
		compatibilityGraph = graph;
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
	
	/**
	 * Checks whether the modified/read resources are disjoint between two methods.
	 * @param m1 first method
	 * @param m2 second method
	 * @return 
	 * <ul>
	 *  <li>true - the two methods are compatible</li>
	 *  <li>false - the two methods concurrently access resources</li>
	 *  <li>null - impossible to decide (one of the methods lacks annotations about resources)</li>
	 * </ul>
	 */
	protected Boolean areDisjoint(String m1, String m2) {
		MultiActiveAnnotations maa1 = methodInfo.get(m1);
		MultiActiveAnnotations maa2 = methodInfo.get(m2);
		
		if ((maa1==null || maa2==null) || 
				(maa1.getModifies()==null && maa1.getReads()==null) || 
				(maa2.getModifies()==null && maa2.getReads()==null)) {
			return null;
		}
		
		if (maa1.getModifies()!=null) {
			for (String w1 : maa1.getModifies()) {
				if (maa2.getModifies()!=null) {
					for (String w2 : maa2.getModifies()) {
						if (w1.equals(w2) || w2.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
				if (maa2.getReads()!=null) {
					for (String w2 : maa2.getReads()) {
						if (w1.equals(w2) || w2.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
			}
		}
		
		if (maa2.getModifies()!=null) {
			for (String w2 : maa2.getModifies()) {
				if (maa1.getModifies()!=null) {
					for (String w1 : maa1.getModifies()) {
						if (w1.equals(w2) || w1.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
				if (maa1.getReads()!=null) {
					for (String w1 : maa1.getReads()) {
						if (w1.equals(w2)|| w1.equals(MultiActiveAnnotations.ALL)) return false;
					}
				}
			}
		}
		
		return true;
		
	}
	
	/**
	 * Container for annotations of methods.
	 * The getter methods are simplified to return arrays of
	 * strings, thus we don't have to couple the methods in the 
	 * multi-active-service to the actual annotation classes.
	 * @author Izso
	 *
	 */
	protected class MultiActiveAnnotations {
		public static final String ALL = "*";
		private CompatibleWith compatibleWith;
		private Reads readVars;
		private Modifies modifiesVars;
		
		public MultiActiveAnnotations(){
			
		}
		
		public void setCompatibleWith(CompatibleWith annotation) {
			compatibleWith = annotation;
		}
		
		public String[] getCompatibleWith(){
			return compatibleWith!=null ? compatibleWith.value() : null;
		}
		
		public void setReads(Reads reads) {
			readVars = reads;
		}
		
		public String[] getReads(){
			return readVars!=null ? readVars.value() : null; 
		}
		
		public void setModifies(Modifies modifs) {
			modifiesVars = modifs;
		}
		
		public String[] getModifies(){
			return modifiesVars!=null ? modifiesVars.value() : null; 
		}
	}

	/**
	 * Runnable class that will serve a request on the body, than call
	 * back to the multi-active-service.
	 * @author Izso
	 *
	 */
	protected class ParallelServe extends RequestWrapper implements Runnable {
		
		public ParallelServe(Request r) {
			super(r);
		}

		@Override
		public void run() {
			body.serve(r);
			asynchronousServeFinished(r, this);
		}
		
	}
	
	/**
	 * By wrapping the request, we can pass the 'method' to the 
	 * outside world without actually exposing internal information. 
	 * @author Izso
	 *
	 */
	protected class RequestWrapper implements MethodFacade {
		protected final Request r;
		
		public RequestWrapper(Request r){
			this.r = r;
		}
		
		@Override
		public String getName() {
			return r.getMethodName();
		}
		
	}

	@Override
	public List<MethodFacade> getExecutingMethods() {
		List<MethodFacade> mlist = new LinkedList<MethodFacade>();
		for (List<ParallelServe> lps : runningServes.values()) {
			mlist.addAll(lps);
		}
		return mlist;
	}
	
	@Override
	public List<MethodFacade> getExecutingMethods(String name) {
		List<MethodFacade> mlist = new LinkedList<MethodFacade>();
		if (runningServes.get(name)==null) return mlist;
		
		for (ParallelServe lps : runningServes.get(name)) {
			mlist.add(lps);
		}
		return mlist;
	}

	@Override
	public MethodFacade getOldestInTheQueue() {
		Request r = requestQueue.getOldest();
		return (r==null) ? null : new RequestWrapper(r);
	}

	@Override
	public List<MethodFacade> getQueueContents() {
		List<MethodFacade> mlist = new LinkedList<MethodFacade>();
		List<Request> rlist = requestQueue.getInternalQueue();
		for (int i=0; i<rlist.size(); i++){
			mlist.add(new RequestWrapper(rlist.get(i)));
		}
		return mlist;
	}

	@Override
	public Integer getRejectionCount(MethodFacade m) {
		//TODO ?
		return 0;
	}

	@Override
	public boolean areConnex(List<MethodFacade> methodList) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<MethodFacade> getMaximumConnex(List<MethodFacade> methodList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MethodFacade> getNeighbours(MethodFacade method) {
		// TODO Auto-generated method stub
		return null;
	}

}
