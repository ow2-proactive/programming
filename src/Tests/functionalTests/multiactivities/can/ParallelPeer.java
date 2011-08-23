package functionalTests.multiactivities.can;

import java.io.Serializable;

import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;

@DefineGroups({
	@Group(name="add_param", selfCompatible=true, parameter="proactive.multiactivity.can.Key"),
	@Group(name="join_from", selfCompatible=false)
})
@DefineRules({
	@Compatible(value={"add_param", "join_from"}, condition="!this.dueToChange"),
	@Compatible({"add_param", "lookup"})
})
public class ParallelPeer extends Peer {
	
	public ParallelPeer() {
		super();
	}
	
	public ParallelPeer(String name, boolean mao) {
		super(name, mao);
	}
	
	public ParallelPeer(String name) {
		super(name);
	}
	
	private boolean dueToChange(Key k){
		return router.isRecentLocal(k);
	}
	
	@Override
	@MemberOf("join_from")
	public JoinResponse joinFrom(Peer other) {
		// TODO Auto-generated method stub
		return super.joinFrom(other);
	}
	
	@Override
	@MemberOf("add_param")
	public void add(Key k, Serializable value) {
		// TODO Auto-generated method stub
		super.add(k, value);
	}
	
	@Override
	@MemberOf("add_param")
	public Serializable lookup(Key k) {
		// TODO Auto-generated method stub
		return super.lookup(k);
	}

}
