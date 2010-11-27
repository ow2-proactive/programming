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

public class MultiActiveService extends Service {
	public Map<String, List<AsynchronousServe>> runningServes = new HashMap<String, List<AsynchronousServe>>();
	public Map<String, List<String>> compatibilityGraph = new HashMap<String, List<String>>();
	public Map<String, ArrayList<Annotation>> methodInfo = new HashMap<String, ArrayList<Annotation>>();
	
	public MultiActiveService(Body body) {
		super(body);
	}
		
	public void multiActiveServing(){
		fillMethodInfo();
		initCompatibilityGraph();
		
		boolean success;
		while (body.isAlive()) {
			success = parallelServeOldest();
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
	
	public boolean parallelServeOldest(){
		AsynchronousServe asserve = null;
		synchronized (requestQueue) {
			synchronized (runningServes) {
				
				Request r = requestQueue.removeOldest();
				if (r!=null) {
					if (canRun(r)){
						asserve = new AsynchronousServe(r);
						List<AsynchronousServe> aslist = runningServes.get(r.getMethodName());
						if (aslist==null) {
							runningServes.put(r.getMethodName(), new LinkedList<AsynchronousServe>());
							aslist = runningServes.get(r.getMethodName());
						}
						aslist.add(asserve);
					} else {
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
	
	protected void fillMethodInfo() {
		try {
			for (Method d : (Class.forName(body.getReifiedClassName())).getMethods()) {
				CompatibleWith cw = d.getAnnotation(CompatibleWith.class);
				if (cw!=null) {
					if (methodInfo.get(d.getName())==null) {
						methodInfo.put(d.getName(), new ArrayList<Annotation>());
					}
					
					methodInfo.get(d.getName()).add(0, cw);
					//TODO add other types
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void initCompatibilityGraph() {
		Map<String, List<String>> compWith = getGraphForCompatibleWith();
		//take other annotations also
		compatibilityGraph = compWith;
	}
	
	private Map<String, List<String>> getGraphForCompatibleWith() {
		Map<String, List<String>> compWith  = new HashMap<String, List<String>>();
		for (String method : methodInfo.keySet()) {
			compWith.put(method, new ArrayList<String>());
			CompatibleWith cwAnn = ((CompatibleWith)methodInfo.get(method).get(0));
			String[] compList = cwAnn!=null ? cwAnn.value() : null;
			if (compList!=null) {
				for (String cm : compList) {
					compWith.get(method).add(cm);
				}
			}
		}
		for (String method : compWith.keySet()) {
			Iterator<String> sit = compWith.get(method).iterator();
			while (sit.hasNext()) {
				String other = sit.next();
				if (!compWith.get(other).contains(method)) {
					sit.remove();
				}
			}
		}
		
		Iterator<String> sit = compWith.keySet().iterator();
		while (sit.hasNext()) {
			if (compWith.get(sit.next()).size()==0) {
				sit.remove();
			}
		}
		
		return compWith;
	}
	
	protected boolean canRun(Request r) {
		if (runningServes.keySet().size()==0) return true;
		
		String request = r.getMethodName();
		for (String s : runningServes.keySet()) {
			if (compatibilityGraph.get(s)==null || !compatibilityGraph.get(s).contains(request)) return false;
		}
		return true;
	}
	
	protected void asynchronousServeFinished(Request r, AsynchronousServe asserve) {
		synchronized (runningServes) {
			runningServes.get(r.getMethodName()).remove(asserve);
			if (runningServes.get(r.getMethodName()).size()==0) {
				runningServes.remove(r.getMethodName());
			}
		}
		parallelServeOldest();
	}
	
	protected class AsynchronousServe implements Runnable {
		private Request r;
		
		public AsynchronousServe(Request r){
			this.r = r;
		}
		
		@Override
		public void run() {
			body.serve(r);
			asynchronousServeFinished(r, this);
		}
	}

}
