package functionalTests.multiactivities.can.test;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.multiactivities.can.Key;
import functionalTests.multiactivities.can.Peer;

public class LookupPeer extends Peer implements InitActive {
	
	public LookupPeer() {
	}
	
	public LookupPeer(String name, boolean isMao) {
		super(name, isMao);
	}
	
	@MemberOf("lookup")
	public BooleanWrapper lookupAllKeys(int canSize, int totalMessages) {
		List<Key> keys = LookupTest.generateKeys(canSize);
		
		List<Object> results = new LinkedList<Object>();
		
		for (int i=0; i<totalMessages/keys.size(); i++) {
			for (Key k : keys) {
				results.add(lookup(k));
			}
		}
		
		PAFuture.waitForAll(results);
		
		return new BooleanWrapper(true);
	}

	@Override
	public void initActivity(Body body) {
		if (!IS_MAO) {
			body.setImmediateService("lookupAllKeys", false);
		}
	}

}
