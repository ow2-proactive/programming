package functionalTests.multiactivities.writersreaders;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.Modifies;
import org.objectweb.proactive.annotation.multiactivity.Reads;
import org.objectweb.proactive.multiactivity.MultiActiveService;

public class IntegerStore implements RunActive {
	
	private Integer value = new Integer(0);
	private Boolean greedy;
	
	public IntegerStore(){
		
	}
	
	public IntegerStore(Boolean greedy) {
		this.greedy = greedy;
	}

	@Override
	public void runActivity(Body body) {
		if (greedy) {
			(new MultiActiveService(body)).greedyMultiActiveServing();
		} else {
			(new MultiActiveService(body)).multiActiveServing();
		}
		
	}
	
	@Modifies({"value"})
	public Integer write(Integer newInt) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		value = newInt;
		return newInt;
	}
	
	@Reads({"value"})
	public Integer read() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

}
