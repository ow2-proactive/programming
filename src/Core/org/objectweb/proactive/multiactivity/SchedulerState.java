package org.objectweb.proactive.multiactivity;

import java.util.List;

import org.objectweb.proactive.core.body.request.Request;

/**
 * Objects implementing this interface can be used to interrogate the scheduler's state.
 * @author Izso
 *
 */
public interface SchedulerState {
	
	/**
	 * Returns the list of methods which are currently executing
	 * @return
	 */
	public List<String> getExecutingMethodNames();
	
	public List<Request> getExecutingRequests();
	
	/**
	 * Returns only the instances of a given method which are
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
