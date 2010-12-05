package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.multiactivity.MultiActiveService.RequestWrapper;

public class ServingPolicyFactory {
	
	public static ServingPolicy getSingleActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public void runPolicy(SchedulerState state) {
				List<RequestWrapper> ret = new LinkedList<RequestWrapper>();
				
				if (state.getExecutingMethods().size()==0 && state.getQueueContents().size()>0) {
					ret.add(state.getOldestInTheQueue());
				}
				
				state.selectForExecution(ret);
			}
		};
	}
	
}
