package org.objectweb.proactive.multiactivity;

import java.util.List;

/**
 * Objects implementing this interface can be used to interrogate the scheduler's state.
 * @author Izso
 *
 */
public interface SchedulerState {
	
	public List<MethodFacade> getExecutingMethods();
	public List<MethodFacade> getExecutingMethods(String name);
	
	public List<MethodFacade> getQueueContents();
	public MethodFacade getOldestInTheQueue();
	
	public Integer getRejectionCount(MethodFacade m);
	

}
