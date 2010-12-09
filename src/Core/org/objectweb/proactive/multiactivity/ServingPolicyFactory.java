package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;

public class ServingPolicyFactory {
	
	public static ServingPolicy getSingleActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(SchedulerState state, MultiActiveCompatibilityMap compatibilityMap) {
				List<Request> ret = new LinkedList<Request>();
				
				if (state.getExecutingMethodNames().size()==0 && state.getQueueContents().size()>0) {
					ret.add(state.getOldestInTheQueue());
				}
				
				return ret;
			}
		};
	}
	
	public static ServingPolicy getMultiActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(SchedulerState state, MultiActiveCompatibilityMap compatibilityMap) {
				List<Request> ret = new LinkedList<Request>();
				Request oldest = state.getOldestInTheQueue();
				
				if (oldest==null) return ret;
				
				if (state.getExecutingMethodNames().size()==0) {
					ret.add(oldest);
				} else {
					if (compatibilityMap.isCompatibleWithAllRequests(oldest, state.getExecutingRequests())){
						ret.add(oldest);
					} else if (state.getQueueContents().size()>1) {
						Request secondOldest = state.getQueueContents().get(1);
						if (secondOldest!=null && compatibilityMap.areCompatible(oldest, secondOldest)
								&& compatibilityMap.isCompatibleWithAllRequests(secondOldest, state.getExecutingRequests())) {
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
			public List<Request> runPolicy(SchedulerState state,
					MultiActiveCompatibilityMap compatibilityMap) {
				
				if (state.getExecutingMethodNames().size()<maxThreads) {
					ServingPolicy maPolicy = getMultiActivityPolicy();
					return maPolicy.runPolicy(state, compatibilityMap);
				}
				
				return null;
			}
		};
	}

	public static ServingPolicy getGreedyMultiActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(SchedulerState state,
					MultiActiveCompatibilityMap compatibilityMap) {
				
				List<Request> ret = new LinkedList<Request>();
				List<Request> queue = state.getQueueContents();
				
				for (int i=0; i<queue.size(); i++) {
					if (compatibilityMap.isCompatibleWithAllRequests(queue.get(i),state.getExecutingRequests())){
						ret.add(queue.get(i));
						return ret;
					}
				}
				
				return ret;
			}
		};
	}
}
