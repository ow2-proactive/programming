package org.objectweb.proactive.multiactivity;

import java.util.List;

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
	public List<MethodFacade> getExecutingMethods();
	
	/**
	 * Returns only the instances of a given method which are
	 * executing
	 * @param name
	 * @return
	 */
	public List<MethodFacade> getExecutingMethods(String name);
	
	/**
	 * Gives the content of the request queue of the scheduler.
	 * Elements are sorted in descending order of their age
	 * @return
	 */
	public List<MethodFacade> getQueueContents();
	
	/**
	 * Returns the first element of the queue, or null in case
	 * the queue is empty 
	 * @return
	 */
	public MethodFacade getOldestInTheQueue();
	
	/**
	 * NOT FINAL: returns the number of scheduling cycles have 
	 * passed since this method entered the queue
	 * @param m
	 * @return
	 */
	// TODO: do we need this?
	public Integer getRejectionCount(MethodFacade m);
	

}
