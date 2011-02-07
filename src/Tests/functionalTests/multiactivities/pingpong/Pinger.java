package functionalTests.multiactivities.pingpong;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.multiactivity.MultiActiveService;

/**
 * Class to test ping-pong-like interactions
 * @author Zsolt István
 *
 */
@DefineGroups(
		{
			@Group(name = "gPing", selfCompatible = true),
			@Group(name = "gPong", selfCompatible = true),
			@Group(name = "gStarter", selfCompatible = false)
		}
)
@DefineRules(
		{
			@Compatible({ "gStarter", "gPing" }),
			@Compatible({ "gStarter", "gPong" })
		}
)
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
			(new MultiActiveService(body)).multiActiveServing();
		} else {
			(new Service(body)).fifoServing();
		}
		
	}
	
	/**
	 * Call the pair's ping method
	 * @return
	 */
	@MemberOf("gPong")
	/*@Reads("pong")*/
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
	@MemberOf("gPing")
	/*@Reads("ping")*/
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
	@MemberOf("gStarter")
	/*@Modifies("count")
	@Reads({"ping","pong"})*/
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
	