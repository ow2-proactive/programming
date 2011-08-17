package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.multiactivity.compatibility.StatefulCompatibilityMap;

/**
 * A factory with examples of serving policies.
 * @author Zsolt Istvan
 *
 */
@Deprecated
public class ServingPolicyFactory {
	
	public static ServingPolicy getSingleActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(StatefulCompatibilityMap compatibility) {
				List<Request> ret = new LinkedList<Request>();
				
				if (compatibility.getNumberOfExecutingRequests()==0 && compatibility.getQueueContents().size()>0) {
					ret.add(compatibility.getOldestInTheQueue());
				}
				
				return ret;
			}
		};
	}
	
	public static ServingPolicy getMultiActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(StatefulCompatibilityMap compatibility) {
				List<Request> ret = new LinkedList<Request>();
				Request oldest = compatibility.getOldestInTheQueue();
				
				if (oldest==null) return ret;
				
				if (compatibility.getExecutingRequests().size()==0) {
					ret.add(oldest);
				} else {
					if (compatibility.isCompatibleWithRequests(oldest, compatibility.getExecutingRequests())){
						ret.add(oldest);
					} else if (compatibility.getQueueContents().size()>1) {
						Request secondOldest = compatibility.getQueueContents().get(1);
						if (secondOldest!=null && compatibility.areCompatible(oldest, secondOldest)
								&& compatibility.isCompatibleWithRequests(secondOldest, compatibility.getExecutingRequests())) {
							ret.add(secondOldest);
						}
					}
				}
				
				return ret;
			}
		};
	}
	
	public static ServingPolicy getMaxThreadMultiActivityPolicy(final int maxThreads){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(StatefulCompatibilityMap compatibility) {
				
				if (compatibility.getExecutingRequests().size()<maxThreads) {
					ServingPolicy maPolicy = getMultiActivityPolicy();
					return maPolicy.runPolicy(compatibility);
				}
				
				return null;
			}
		};
	}

	public static ServingPolicy getGreedyMultiActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(StatefulCompatibilityMap compatibility) {
				
				List<Request> ret = new LinkedList<Request>();
				List<Request> queue = compatibility.getQueueContents();
				
				for (int i=0; i<queue.size(); i++) {
					if (compatibility.isCompatibleWithRequests(queue.get(i),compatibility.getExecutingRequests())){
						ret.add(queue.get(i));
						return ret;
					}
				}
				
				return ret;
			}
		};
	}
}
