package org.objectweb.proactive.multiactivity.compatibility;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.body.request.Request;

/**
 * This is an extension to the {@link CompatibilityMap}, and it incorporates information
 * about the state of the scheduler. It facilitates checking compatibility of methods with
 * the ones in the waiting queue, or the ones that are currently executing.
 * @author Zsolt Istvan
 *
 */
public abstract class StatefulCompatibilityMap extends CompatibilityMap {
	
	public StatefulCompatibilityMap(AnnotationProcessor annotProc) {
		super(annotProc);
	}
	
	public StatefulCompatibilityMap(Class<?> clazz) {
		super(clazz);
	}
	
	/**
	 * Returns the set of methods which are currently executing. Even if 
	 * a method is executing in multiple instances, it will appear only 
	 * once in this set.
	 * @return
	 */
//	public abstract Set<String> getExecutingMethodNameSet();
	
	/**
	 * Returns the list of methods which are currently executing. If a method
	 * is executing in multiple instances, it will appear multiple times in
	 * this list.
	 * @return
	 */
//	public abstract List<String> getExecutingMethodNames();
	
	/**
	 * Returns the list of requests which are currently executing.
	 * @return
	 */
	public abstract Collection<Request> getExecutingRequests();
	
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
//	public abstract List<Request> getExecutingRequestsFor(String method);
	
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
	
	/**
	 * Returns true if the given request can be run in parallel with all 
	 * methods that are currently executing. 
	 * @param request
	 * @return
	 */
	public abstract boolean isCompatibleWithExecuting(Request request);
	
	/**
	 * Returns true if the given method can be run in parallel with all 
	 * methods that are currently executing. 
	 * @param method
	 * @return
	 */
//	public abstract boolean isCompatibleWithExecuting(String method);

}
