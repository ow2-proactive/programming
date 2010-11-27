package org.objectweb.proactive;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
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
	}
	
	/**
     * Invoke the default parallel policy to pick up the requests from the request queue.
     * This does not return until the body terminate, as the active thread enters in
     * an infinite loop for processing the request in the FIFO order, and parallelizing where 
     * possible.
     */
	public void multiActiveServing(){
		fillMethodInfo();
		initCompatibilityGraph();
		
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
						//if there is no conflict, prepare launch
						asserve = new ParallelServe(r);
						
						List<ParallelServe> aslist = runningServes.get(r.getMethodName());
						if (aslist==null) {
							runningServes.put(r.getMethodName(), new LinkedList<ParallelServe>());
							aslist = runningServes.get(r.getMethodName());
						}
						aslist.add(asserve);
					} else {
						//otherwise put it back
						requestQueue.addToFront(r);
					}
				}
			}
		}
		if (asserve!=null) {
			(new Thread(asserve)).start();
		}
		return asserve != null;
	}
	
	/**
	 * This method will iterate through all methods from the underlying class, and
	 * create a descriptor object containing all annotations extracted. This object
	 * is put into the {@link #methodInfo} structure.
	 */
	protected void fillMethodInfo() {
		try {
			for (Method d : (Class.forName(body.getReifiedClassName())).getMethods()) {
				CompatibleWith cw = d.getAnnotation(CompatibleWith.class);
				if (cw!=null) {
					if (methodInfo.get(d.getName())==null) {
						methodInfo.put(d.getName(), new MultiActiveAnnotations());
					}
					
					methodInfo.get(d.getName()).setCompatibleWith(cw);
					//TODO add other types
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate the compatibility graphs based on various annotations, than generate
	 * the final graph from these.
	 */
	protected void initCompatibilityGraph() {
		Map<String, List<String>> compWith = getGraphForCompatibleWith();
		//take other annotations also
		compatibilityGraph = compWith;
	}
	
	/**
	 * Creates the compatibility graph based on the {@link CompatibleWith} annotations.
	 * @return
	 */
	private Map<String, List<String>> getGraphForCompatibleWith() {
		Map<String, List<String>> compWith  = new HashMap<String, List<String>>();
		//for all methods
		for (String method : methodInfo.keySet()) {
			compWith.put(method, new ArrayList<String>());
			String[] compList = methodInfo.get(method).getCompatibleWith();
			//if the user set some methods, put them into the neighbour list
			if (compList!=null) {
				for (String cm : compList) {
					if (!cm.equals(MultiActiveAnnotations.ALL)) {
						compWith.get(method).add(cm);
					} else {
						compWith.get(method).addAll(compWith.keySet());
					}
				}
			}
		}
		//check for bidirectionality of relations. if a compatibleWith is only
		// expressed in one direction, it is deleted
		for (String method : compWith.keySet()) {
			Iterator<String> sit = compWith.get(method).iterator();
			while (sit.hasNext()) {
				String other = sit.next();
				if (!compWith.get(other).contains(method)) {
					sit.remove();
				}
			}
		}
		
		//to save space, all no-neighbour methods are removed from the graph
		Iterator<String> sit = compWith.keySet().iterator();
		while (sit.hasNext()) {
			if (compWith.get(sit.next()).size()==0) {
				sit.remove();
			}
		}
		
		return compWith;
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
		parallelServeOldest();
	}
	
	/**
	 * Runnable class that will serve a request on the body, than call
	 * back to the multi-active-service.
	 * @author Izso
	 *
	 */
	protected class ParallelServe implements Runnable {
		private Request r;
		
		public ParallelServe(Request r){
			this.r = r;
		}
		
		@Override
		public void run() {
			body.serve(r);
			asynchronousServeFinished(r, this);
		}
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
		//TODO add other types
		
		public MultiActiveAnnotations(){
			
		}
		
		public void setCompatibleWith(CompatibleWith annotation) {
			compatibleWith = annotation;
		}
		
		public String[] getCompatibleWith(){
			return compatibleWith!=null ? compatibleWith.value() : null;
		}
	}

}
