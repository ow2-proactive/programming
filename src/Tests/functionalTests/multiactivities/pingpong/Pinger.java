package functionalTests.multiactivities.pingpong;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.MultiActiveService;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;

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
	
	@CompatibleWith({"pong", "startWithPing"})
	public Integer pong(){
		count--;
		System.out.print("Pong!");
		if (count>0) other.ping();
		return count;
	}
	
	@CompatibleWith({"ping", "startWithPing"})
	public Integer ping(){
		count--;
		System.out.print("Ping!");
		if (count>0) other.pong();
		return count;
	}
	
	@CompatibleWith({"ping", "pong"})
	public Integer startWithPing(){
		return other.ping();
	}
	
	public Integer startWithPong(){
		return other.pong();
	}
}
	