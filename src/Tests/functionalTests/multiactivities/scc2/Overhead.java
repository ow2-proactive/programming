package functionalTests.multiactivities.scc2;

import java.util.Date;
import java.util.LinkedList;
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.Self;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicy;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

@DefineGroups(
	@Group(name="group", selfCompatible=false)
)
public class Overhead implements RunActive {
	public static final int MULTI_ACTIVE = 1;
	public static final int SINGLE_ACTIVE = 2;
	public static final int POLICY_MULTI_ACTIVE = 3;
	public static final int POLICY_SINGLE_ACTIVE = 4;
	
	private int mode;
	
	private int tOne = 100;
	
	public Overhead() {
		// for PA
	}
	
	public Overhead(int mode, int time) {
		this.mode = mode;
		tOne = time;
	}
	
	@MemberOf("group")
	public BooleanWrapper doOne(){
		try {
            Thread.sleep(tOne);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		return new BooleanWrapper(true);
	}

	@Override
	public void runActivity(Body body) {
		if (mode==MULTI_ACTIVE) {
			new MultiActiveService(body).multiActiveServing();
		} else if (mode==SINGLE_ACTIVE) {
			new Service(body).fifoServing();
		} else if (mode==POLICY_MULTI_ACTIVE) {
			new MultiActiveService(body).policyServing(ServingPolicyFactory.getMultiActivityPolicy());
		} else if (mode==POLICY_SINGLE_ACTIVE) {
			new MultiActiveService(body).policyServing(ServingPolicyFactory.getSingleActivityPolicy());
		}
	}
	
	public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
		Object[] consPar = new Object[2];
		
		consPar[0]=(int) Integer.parseInt(args[0]);
		consPar[1]=(int) Integer.parseInt(args[1]);
		
		Overhead oh = PAActiveObject.newActive(Overhead.class, consPar);
		LinkedList<BooleanWrapper> res = new LinkedList<BooleanWrapper>();
		Date t = new Date();
		for (int x=0; x<Integer.parseInt(args[2]); x++) {
		    res.add(oh.doOne());
		}
		for (BooleanWrapper bw : res) {
		    bw.equals(true);
		}
		System.err.println(new Date().getTime() - t.getTime());
		PAActiveObject.terminateActiveObject(oh, true);
		System.exit(0);
	}
}
