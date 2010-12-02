package org.objectweb.proactive.multiactivity;

import java.util.LinkedList;
import java.util.List;

public class ServingPolicyFactory {
	
	public static ServingPolicy getSingleActivityPolicy(){
		return new ServingPolicy() {
			
			@Override
			public List<MethodFacade> selectToRun(SchedulerState state) {
				List<MethodFacade> ret = new LinkedList<MethodFacade>();
				
				if (state.getExecutingMethods().size()==0 && state.getQueueContents().size()>0) {
					ret.add(state.getOldestInTheQueue());
				}
				
				return ret; 
			}
		};
	}
	
}
