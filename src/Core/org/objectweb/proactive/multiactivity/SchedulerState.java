package org.objectweb.proactive.multiactivity;

import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.body.request.Request;

/**
 * Objects implementing this interface can be used to interrogate the scheduler's state.
 * @author Zsolt István
 *
 */
public interface SchedulerState {
	
	/**
	 * Returns the set of methods which are currently executing. Even if 
	 * a method is executing in multiple instances, it will appear only 
	 * once in this set.
	 * @return
	 */
	public Set<String> getExecutingMethodNameSet();
	
	/**
	 * Returns the list of methods which are currently executing. If a method
	 * is executing in multiple instances, it will appear multiple times in
	 * this list.
	 * @return
	 */
	public List<String> getExecutingMethodNames();
	
	/**
	 * Returns the list of requests which are currently executing.
	 * @return
	 */
	public List<Request> getExecutingRequests();
	
	/**
	 * Returns only the instances of a given method name which are
	 * executing
	 * @param name
	 * @return
	 */
	public List<Request> getExecutingRequestsFor(String method);
	
	/**
	 * Gives the content of the request queue of the scheduler.
	 * Elements are sorted in descending order of their age
	 * @return
	 */
	public List<Request> getQueueContents();
	
	/**
	 * Returns the first element of the queue, or null in case
	 * the queue is empty 
	 * @return
	 */
	public Request getOldestInTheQueue();

}
