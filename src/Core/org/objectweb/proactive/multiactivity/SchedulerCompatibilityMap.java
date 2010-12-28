package org.objectweb.proactive.multiactivity;

import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.body.request.Request;

public abstract class SchedulerCompatibilityMap extends CompatibilityMap {
	
	public SchedulerCompatibilityMap(AnnotationProcessor annotProc) {
		super(annotProc);
	}
	
	public SchedulerCompatibilityMap(Class<?> clazz) {
		super(clazz);
	}
	
	/**
	 * Returns the set of methods which are currently executing. Even if 
	 * a method is executing in multiple instances, it will appear only 
	 * once in this set.
	 * @return
	 */
	public abstract Set<String> getExecutingMethodNameSet();
	
	/**
	 * Returns the list of methods which are currently executing. If a method
	 * is executing in multiple instances, it will appear multiple times in
	 * this list.
	 * @return
	 */
	public abstract List<String> getExecutingMethodNames();
	
	/**
	 * Returns the list of requests which are currently executing.
	 * @return
	 */
	public abstract List<Request> getExecutingRequests();
	
	/**
	 * Returns the number of executing requests in the service.
	 * @return
	 */
	public abstract int getNumberOfExecutingRequests();
	
	/**
	 * Returns only the instances of a given method name which are
	 * executing
	 * @param name
	 * @return
	 */
	public abstract List<Request> getExecutingRequestsFor(String method);
	
	/**
	 * Gives the content of the request queue of the scheduler.
	 * Elements are sorted in descending order of their age
	 * @return
	 */
	public abstract List<Request> getQueueContents();
	
	/**
	 * Returns the first element of the queue, or null in case
	 * the queue is empty 
	 * @return
	 */
	public abstract Request getOldestInTheQueue();
	
	public abstract boolean isCompatibleWithExecuting(Request request);
	public abstract boolean isCompatibleWithExecuting(String method);

}
