package functionalTests.multiactivities.pingpong;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.annotation.multiactivity.Reads;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicy;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

/**
 * Class to test ping-pong-like interactions
 * @author Izso
 *
 */
public class Pinger implements RunActive {
	private Integer count = 5;
	private Pinger other;
	private Boolean multiActive;
	
	public Pinger(){
		// for PA
	}
	
	public Pinger(Boolean multiActive) {
		this.other = other;
		this.multiActive = multiActive;
	}
	
	public void setOther(Pinger other) {
		this.other = other;
	}
	
	/**
	 * Set the pair of this object
	 * @return
	 */
	public Pinger getOther() {
		return this.other;
	}
	
	@Override
	public void runActivity(Body body) {
		if (this.multiActive) {
			ServingPolicy greedy = ServingPolicyFactory.getGreedyMultiActivityPolicy();
			(new MultiActiveService(body)).policyServing(greedy);
		} else {
			(new Service(body)).fifoServing();
		}
		
	}
	
	/**
	 * Call the pair's ping method
	 * @return
	 */
	@CompatibleWith({"pong", "startWithPing"})
	public Integer pong(){
		count--;
		System.out.print("Pong"+count+"!");
		if (count>0) other.ping();
		return count;
	}
	
	/**
	 * Call the pair's pong method
	 * @return
	 */
	@CompatibleWith({"ping", "startWithPing"})
	public Integer ping(){
		count--;
		System.out.print("Ping"+count+"!");
		if (count>0) other.pong();
		return count;
	}
	
	/**
	 * Method to start off -- it will call the pair's ping method 
	 * @return
	 */
	@CompatibleWith({"ping", "pong"})
	public Integer startWithPing(){
		return other.ping();
	}
	
	/**
	 * Method to start off -- that will deadblock
	 * @return
	 */
	public Integer startWithPong(){
		return other.pong();
	}
}
	