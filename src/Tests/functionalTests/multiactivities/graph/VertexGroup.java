package functionalTests.multiactivities.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

@DefineGroups({
	@Group(name = "", selfCompatible = false),
	@Group(name = "", selfCompatible = false)
})
@DefineRules({
	@Compatible({"a","a"})
})
public class VertexGroup implements RunActive, Serializable {
	
	private Map<Integer, Set<Integer>> vertices;
	
	private Map<Integer, Set<Integer>> invertedVertices;
	
	private Map<Integer, VertexGroup> externalVert;
	
	private Set<Integer> sccMarked = new HashSet<Integer>();
	
	private Map<Integer, List<Integer>> forwardMarked = new HashMap<Integer, List<Integer>>();
	
	private Map<Integer, Map<VertexGroup, Integer>> forwardWorking = new HashMap<Integer, Map<VertexGroup, Integer>>();
	
	private Map<Integer, List<Integer>> backwardMarked = new HashMap<Integer, List<Integer>>();
	
	private Map<Integer, Map<VertexGroup, Integer>> backwardWorking = new HashMap<Integer, Map<VertexGroup, Integer>>();

	private String name;
	
	public void setupVertices(Map<Integer, Set<Integer>> data) {
		vertices = data;
		invertedVertices = new HashMap<Integer, Set<Integer>>();
		for (Integer to : vertices.keySet()) {
			for (Integer from : vertices.get(to)) {
				if (invertedVertices.get(from)==null) {
					invertedVertices.put(from, new HashSet<Integer>());
				}
				invertedVertices.get(from).add(to);
			}
		}
	}
	
	public void setupExternal(Map<Integer, VertexGroup> data) {
		externalVert = data;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public String getName() {
		return name;
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public void cleanupAfter(Integer pivot) {
		synchronized (forwardMarked) {
			forwardMarked.remove(pivot);
		}
		synchronized (backwardMarked) {
			backwardMarked.remove(pivot);
		}
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public void addToScc(Set<Integer> verts){
		synchronized (sccMarked) {
			sccMarked.addAll(verts);
		}
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public void markForward(VertexGroup master, Integer pivot, Set<Integer> from) {
		//System.out.println("I'm "+name+" doing "+from);
		Map<VertexGroup, Set<Integer>> buffer = new HashMap<VertexGroup, Set<Integer>>();
		List<Integer> myForwardMarked = new LinkedList<Integer>();
		

		synchronized (forwardMarked) {
			if (forwardMarked.get(pivot)==null) {
				forwardMarked.put(pivot, new LinkedList<Integer>());
			}
			
			myForwardMarked.addAll(forwardMarked.get(pivot));
		}
		
		int added = 0;
		
		synchronized (sccMarked) {
			if (from!=null) {
				for (Integer fromVert : from) {
					if (!myForwardMarked.contains(fromVert) && !sccMarked.contains(fromVert)) {
						myForwardMarked.add(0, fromVert);
						added++;
					}
				}
			} else {
				myForwardMarked.add(0, pivot);
				added++;
			}
		}
		
		if (added==0) {
			master.finishedWork(pivot, this, 0);
			return;
		}
		
		synchronized (sccMarked) {
			while (added > 0) {
				added--;
				Integer cur = myForwardMarked.get(added);
				if (vertices.get(cur) != null && !externalVert.containsKey(cur)) {
					for (Integer to : vertices.get(cur)) {
						if (!myForwardMarked.contains(to)
								&& !sccMarked.contains(to)) {
							myForwardMarked.add(0, to);
							added++;
						}
					}
				} else {

					if (!sccMarked.contains(cur)) {
						VertexGroup location = externalVert.get(cur);
						if (location != null) {
							if (buffer.get(location) == null) {
								buffer.put(location, new HashSet<Integer>());
							}
							buffer.get(location).add(cur);
						}
					}
				}
			}
		}
		
		synchronized (forwardMarked) {
			forwardMarked.get(pivot).addAll(myForwardMarked);
		}
		
		for (VertexGroup vg : buffer.keySet()) {
			master.addedWorker(pivot, vg, 0);
			vg.markForward(master, pivot, buffer.get(vg));
		}
		
		synchronized (forwardMarked) {
			try {
				master.finishedWork(pivot, this, 0);
			} catch (ConcurrentModificationException ccme) {
				master.finishedWork(pivot, this, 0);
			}
		}		
		
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public Set<Integer> markBackward(Integer pivot, Set<Integer> from) {
		//System.out.println("I'm "+name+" doing bacward "+from);
		Map<VertexGroup, Set<Integer>> buffer = new HashMap<VertexGroup, Set<Integer>>();
		Map<VertexGroup, Set<Integer>> result = new HashMap<VertexGroup, Set<Integer>>();
		Set<Integer> resultSet = new HashSet<Integer>();
		List<Integer> myBackwardMarked = new LinkedList<Integer>();
		
		synchronized (backwardMarked) {
			if (backwardMarked.get(pivot)==null) {
				backwardMarked.put(pivot, new LinkedList<Integer>());
			}

			myBackwardMarked.addAll(backwardMarked.get(pivot));
		}
		
		int added = 0;
		
		synchronized (sccMarked) {
			if (from!=null) {
				for (Integer fromVert : from) {
					if (!myBackwardMarked.contains(fromVert) && !sccMarked.contains(fromVert)) {
						myBackwardMarked.add(0, fromVert);
						added++;
					}
				}
			} else {
				myBackwardMarked.add(0, pivot);
				added++;
			}
		}
		
		if (added==0) {
			return new HashSet<Integer>();
		}
		synchronized (sccMarked) {
			while (added > 0) {
				added--;
				Integer cur = myBackwardMarked.get(added);
				if (invertedVertices.get(cur) != null
						&& !externalVert.containsKey(cur)) {
					for (Integer to : invertedVertices.get(cur)) {
						if (!myBackwardMarked.contains(to)
								&& !sccMarked.contains(to)) {
							myBackwardMarked.add(0, to);
							added++;
						}
					}
				} else {
					if (!sccMarked.contains(cur)) {
						VertexGroup location = externalVert.get(cur);
						if (location != null) {
							if (buffer.get(location) == null) {
								buffer.put(location, new HashSet<Integer>());
							}
							buffer.get(location).add(cur);
						}
					}
				}
			}
		}
		
		synchronized (backwardMarked) {
			backwardMarked.get(pivot).addAll(myBackwardMarked);
		}
		
		resultSet.addAll(myBackwardMarked);
		
		for (VertexGroup vg : buffer.keySet()) {
			result.put(vg, vg.markBackward(pivot, buffer.get(vg)));
			resultSet.addAll(result.get(vg));
		}
		
		return resultSet;
		
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public Set<Integer> getVertices(){
		HashSet<Integer> ret = new HashSet<Integer>();
		for (Set<Integer> setI : vertices.values()) {
			for (Integer v : setI) {
				if (!externalVert.keySet().contains(v)) {
					ret.add(v);
				}
			}
		}
		for (Integer v : vertices.keySet()) {
			if (!externalVert.keySet().contains(v)) {
				ret.add(v);
			}
		}
		return ret;
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public Boolean addedWorker(int pivot, VertexGroup who, int seqNumber){
		synchronized (forwardWorking) {
			//System.out.println("adding "+who.getName()+" -- "+pivot);
			if (forwardWorking.get(pivot)==null) {
				forwardWorking.put(pivot, new HashMap<VertexGroup, Integer>());
			}
			
			for (VertexGroup vg : forwardWorking.get(pivot).keySet()) {
				if (vg.getName().equals(who.getName())) {
					Integer old = forwardWorking.get(pivot).get(who);
					forwardWorking.get(pivot).put(vg, old==null ? 1 : old+1);
					
					forwardWorking.notify();
					return true;
				}
			}
			
			forwardWorking.get(pivot).put(who, 1);
			
		}
		return true;
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public Boolean finishedWork(int pivot, VertexGroup who, int seqNumber){
		synchronized (forwardWorking) {
			//System.out.println("finished "+who.getName()+" -- "+pivot);
			if (forwardWorking.get(pivot)==null) {
				forwardWorking.put(pivot, new HashMap<VertexGroup, Integer>());
			}
			
			for (VertexGroup vg : forwardWorking.get(pivot).keySet()) {
				if (vg.getName().equals(who.getName())) {
					Integer old = forwardWorking.get(pivot).get(vg);
					
					forwardWorking.get(pivot).put(vg, old==null ? -1 : old-1);
					forwardWorking.notify();
					
					return true;
				}
			}
			
			forwardWorking.get(pivot).put(who, -1);
			forwardWorking.notify();
		}
		return true;
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public Set<Integer> makeForwardMaster(final Integer pivot) {
		addedWorker(pivot, this, 0);
		boolean ok = false;
		
		synchronized (forwardWorking) {
			while (!ok){
				Map<VertexGroup, Integer> fw = forwardWorking.get(pivot);
				ok = true;
				
				for (VertexGroup dvg : fw.keySet()){
					if (fw.get(dvg)!=0){
						ok = false;
						break;
					}
				}
				
				if (!ok) {
					try {
						//System.out.println("Sleeping...");
						forwardWorking.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		//System.out.println("Finished pivot "+pivot);
		Set<Integer> result = new HashSet<Integer>();
		
		Map<VertexGroup, Integer> fw = forwardWorking.get(pivot);
		for (VertexGroup dvg : fw.keySet()){
			result.addAll(dvg.getForwardMarked(pivot));
		}
		
		return result;
	}
	
	@Compatible({"addToScc", "getVertices", "markForward", "markBackward", "cleanupAfter", "finishedWork", "addedWorker", "makeForwardMaster", "getForwardMarked", "getName"})
	public Collection<Integer> getForwardMarked(Integer pivot) {
		synchronized (forwardMarked) {
			return forwardMarked.get(pivot)==null ? new HashSet<Integer>() : forwardMarked.get(pivot); 
		}
	}

	@Override
	public void runActivity(Body body) {
		MultiActiveService mas = (new MultiActiveService(body));
		//mas.multiActiveServing();
		
		mas.policyServing(ServingPolicyFactory.getMultiActivityPolicy());
		
		//(new Service(body)).fifoServing();
		
	}
		

}
