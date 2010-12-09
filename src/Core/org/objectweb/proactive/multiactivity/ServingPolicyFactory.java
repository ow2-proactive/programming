package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;

public class ServingPolicyFactory {
	
	public static ServingPolicy getSingleActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<Request> runPolicy(SchedulerState state) {
				List<Request> ret = new LinkedList<Request>();
				
				if (state.getExecutingMethodNames().size()==0 && state.getQueueContents().size()>0) {
					ret.add(state.getOldestInTheQueue());
				}
				
				return ret;
			}
		};
	}
	
}
