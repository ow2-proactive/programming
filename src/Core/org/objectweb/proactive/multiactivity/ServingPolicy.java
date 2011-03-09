package org.objectweb.proactive.multiactivity;

import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;

/**
 * Interface for describing the inner policy of the MultiActiveService
 * @author Zsolt István
 *
 */
public interface ServingPolicy {	
	
	/**
	 * This method will decide which methods get to run given the current state of the scheduler
	 * and the relation between methods.
	 * <br/>
	 * IMPORTANT: This policy is run in three cases:
	 * <ul>
	 * 	<li>A request has finished execution in the MultiActiveObject</li>
	 *  <li>A new request arrived in the queue</li>
	 *  <li>This policy has returned at least one request to be served in parallel</li>
	 * </ul>
	 * @param state
	 * @param compatibilityMap
	 * @return a sublist of the requests that can be started in parallel
	 */
	public List<Request> runPolicy(StatefulCompatibilityMap compatibility);
	
}
