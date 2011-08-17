package org.objectweb.proactive.multiactivity;

import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;

/**
 * Interface for describing the scheduling policy to be used in a multi-active service.
 * @author Zsolt Istvan
 *
 */
public interface ServingPolicy {	
	
	/**
	 * This method will decide which methods get to run given the current state of the scheduler
	 * and the relation between methods. 
	 * <br>
	 * <i>IMPORTANT:</i> While executing a policy the state of the queue and the running set is guaranteed not to change.
	 * @param state
	 * @param compatibilityMap
	 * @return a sublist of the requests that can be started in parallel
	 */
	public List<Request> runPolicy(StatefulCompatibilityMap compatibility);
	
}
