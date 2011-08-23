package org.objectweb.proactive.extra.multiactivecan;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;

/**
 * This will be the peer in the network.
 * It should be able to do join, add and lookup.
 * @author zistvan
 *
 */
@DefineGroups({
	@Group(name="join", selfCompatible=false),
	@Group(name="update", selfCompatible=false),
	@Group(name="add", selfCompatible=true),
	@Group(name="lookup", selfCompatible=true)
})
@DefineRules({
	@Compatible({"add", "lookup"})
})
public class Peer implements Serializable, RunActive {
	
	public static boolean IS_MAO = false;
	
	ConcurrentHashMap<Key, Serializable> data = new ConcurrentHashMap<Key, Serializable>();
	Router router;
	String name = "noname";
	
	public Peer() {
	}
	
	public Peer(String name) {
		this.name = name;
	}
	
	public Peer(String name, boolean mao) {
		this();
		this.name = name;
		IS_MAO = mao;
	}
	
	public boolean barrier() {
		return true;
	}
	
	
	public BooleanWrapper createNetwork() {
		router = new Router(new Zone(0, 0, Zone.MAX_X, Zone.MAX_Y, (Peer) PAActiveObject.getStubOnThis())); 
		
		return new BooleanWrapper(true);
	}
	
	@MemberOf("join")
	public BooleanWrapper join(Peer master) {
		
		JoinResponse resp = master.joinFrom((Peer) PAActiveObject.getStubOnThis());
		
		try{
			setData(resp.getData());
			setRouter(resp.getRouter());
		} catch (NullPointerException npe) {
			return new BooleanWrapper(false);
		}
		return new BooleanWrapper(true);
	}
	
	/**
	 * Joins an other peer and returns this peer's zone
	 * @param other
	 * @return
	 */
	@MemberOf("join")
	public JoinResponse joinFrom(Peer other) {
		
		Router otherRouter = router.splitWith(other);
		
		if (otherRouter==null) {
			return null;
		}
		
		ConcurrentHashMap<Key, Serializable> otherData = new ConcurrentHashMap<Key, Serializable>();
		Set<Key> toRemove = new HashSet<Key>();
		
		for (Key k : data.keySet()) {
			if (!router.isLocal(k)) {
				toRemove.add(k);
			}
		}
		
		for (Key k : toRemove) {
			otherData.put(k, data.get(k));
			data.remove(k);
		}
		
		JoinResponse resp = new JoinResponse(otherRouter, otherData);
		
		router.joinFinished();

		return resp;
	}
	
	@MemberOf("update")
	public BooleanWrapper updateNeighbours(Zone oldZone, Collection<Zone> newZones) {
		//System.out.println("Neighbour update in "+this);
		router.updateNeighbours(oldZone, newZones);
		return new BooleanWrapper(true);
	}
/*	
	@MemberOf("update")
	public void removeNeighbour(Zone oldZone) {
		//System.out.println("Neighbour update in "+this);
		router.removeNeighbour(oldZone);
	}
	
	@MemberOf("update")
	public void addNeighbour(Zone newZone) {
		//System.out.println("Neighbour update in "+this);
		router.addNeighbour(newZone);
	}*/
	
	/**
	 * Adds a key/value pair to the network.
	 * @param k
	 * @param value
	 * @return
	 */
	@MemberOf("add")
	public void add(Key k, Serializable value) {
		
		if (router.isLocal(k)) {
			data.put(k, value);
			//System.out.println("---");
			//System.out.println("Stored "+k+" in peer "+this+" in thread "+Thread.currentThread().getId());
					
			//return new BooleanWrapper(true);
		} else {
			//System.out.println("routing to  "+router.getNeighbourClosestTo(k));
			router.getNeighbourClosestTo(k).add(k, value);
		}
	}
	
	/**
	 * finds the value associated with a given key. May return null if the value is not stored anywhere.
	 * @param k
	 * @return
	 */
	@MemberOf("lookup")
	public Serializable lookup(Key k) {
		
		if (router.isLocal(k)) {
			/*if (data.get(k)!=null) {
				System.out.println("Found "+k+" in peer "+this+" in thread "+Thread.currentThread().getId());
			} else {
				System.err.println("[ERROR] Empty "+k+" in peer "+this);
			}*/
			return data.get(k);
		} else {
			return router.getNeighbourClosestTo(k).lookup(k);
		}
	}
	
	private void setData(ConcurrentHashMap<Key, Serializable> data) {
		this.data = data;
	}

	private void setRouter(Router router) {
		System.out.println("router set!");
		this.router = router;
		
	}

	@Override
	public String toString() {
		return ""+name + " [Keys:"+data.keySet().size()+", "+this.router+"]";
	}
	
	@Override
	public void runActivity(Body body) {
		if (IS_MAO) {
			System.out.println("MAO");
			new MultiActiveService(body).multiActiveServing();
			
/*			ObservableRequestExecutor rex = ObservableRequestExecutor.getForBody(body);
			rex.setObserver(new MinimalLiveWindow(body.getName()));
			rex.configure(1000, true, false);
			rex.execute();
*/			
		} else {
			System.out.println("SAO");
			new Service(body).fifoServing();
		}
	}
	
}
