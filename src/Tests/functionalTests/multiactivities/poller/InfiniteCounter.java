package functionalTests.multiactivities.poller;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

@DefineGroups(
		{
			@Group(name = "gCounter", selfCompatible = false),
			@Group(name = "gPoller", selfCompatible = true),
			@Group(name = "gPoller_Bogus", selfCompatible = true)
		}
)
@DefineRules(
		{
			@Compatible(value = { "gCounter", "gPoller" }),
			@Compatible(value = { "gCounter", "gPoller_Bogus" })
		}
)
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
			(new MultiActiveService(body)).policyServing(ServingPolicyFactory.getMaxThreadMultiActivityPolicy(2));
		} else {
			(new MultiActiveService(body)).policyServing(ServingPolicyFactory.getSingleActivityPolicy());
		}
		
	}
	
	@MemberOf({"gCounter"})
	public void countToInfinity(){
		System.out.println("Counting to infinity!");
		while (value!=null) {
			synchronized (value) {
				value++;
			}
		}
	}
	
	@MemberOf({"gPoller"})
	public Long noReturnPollValue(){
		while (value!=-1) {
			// ...
		}
		return value;
	}
	
	@MemberOf({"gPoller"})
	public Long pollValue(){
		synchronized (value) {
			System.out.println("Polling value...");
			return value.longValue();
		}
	}
	
}
