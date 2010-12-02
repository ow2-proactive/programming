package functionalTests.multiactivities.poller;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

public class InfiniteCounter implements RunActive {
	private Long value = new Long(0);
	private Boolean multiActive;
	
	public InfiniteCounter(){
		
	}
	
	public InfiniteCounter(Boolean multiActive) {
		this.multiActive = multiActive;
	}

	@Override
	public void runActivity(Body body) {
		if (multiActive) {
			(new MultiActiveService(body)).multiActiveServing();
		} else {
			(new MultiActiveService(body)).policyServing(ServingPolicyFactory.getSingleActivityPolicy());
		}
		
	}
	
	@CompatibleWith({"pollValue"})
	public void countToInfinity(){
		System.out.println("Counting to infinity!");
		while (value!=null) {
			synchronized (value) {
				value++;
			}
		}
	}
	
	@CompatibleWith({"*"})
	public Long pollValue(){
		synchronized (value) {
			System.out.println("Polling value...");
			return value.longValue();
		}
	}
	
}
