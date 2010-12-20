package functionalTests.multiactivities.graph;

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
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.ServingPolicyFactory;

@DefineGroups(
		{
		@Group(name = "gProperties", selfCompatible = true),
		@Group(name = "gMarkForward", selfCompatible = true),
		@Group(name = "gMarkBackward", selfCompatible = true),
		@Group(name = "gMaintenance", selfCompatible = false)
		}
)
@DefineRules(
		{
		@Compatible({"gProperties", "gMarkforward", "gMarkBackward", "gMaintenance"})
		}
)
public class VertexGroup implements RunActive {
	
	private Map<Integer, Set<Integer>> vertices;
	
	private Map<Integer, Set<Integer>> invertedVertices;
	
	private Map<Integer, VertexGroup> externalVert;
	
	private Set<Integer> sccMarked = new HashSet<Integer>();
	
	private Map<Integer, List<Integer>> forwardMarked = new HashMap<Integer, List<Integer>>();
	
	private Map<Integer, List<Integer>> backwardMarked = new HashMap<Integer, List<Integer>>();

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
	
	@MemberOf("gProperties")
	public String getName() {
		return name;
	}
	
	@MemberOf("gMaintenance")
	public void cleanupAfter(Integer pivot) {
		synchronized (forwardMarked) {
			forwardMarked.remove(pivot);
		}
		synchronized (backwardMarked) {
			backwardMarked.remove(pivot);
		}
	}
	
	@MemberOf("gMaintenance")
	public void addToScc(Set<Integer> verts){
		synchronized (sccMarked) {
			sccMarked.addAll(verts);
		}
	}
	
	@MemberOf("gMarkForward")
	public Set<Integer> markForward(Integer pivot, Set<Integer> from) {
		//System.out.println("I'm "+name+" doing "+from);
		Map<VertexGroup, Set<Integer>> buffer = new HashMap<VertexGroup, Set<Integer>>();
		Map<VertexGroup, Set<Integer>> result = new HashMap<VertexGroup, Set<Integer>>();
		Set<Integer> resultSet = new HashSet<Integer>();
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
			return new HashSet<Integer>();
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
		
		resultSet.addAll(myForwardMarked);
		
		for (VertexGroup vg : buffer.keySet()) {
			result.put(vg, vg.markForward(pivot, buffer.get(vg)));
			resultSet.addAll(result.get(vg));
		}
		
		return resultSet;
		
	}
	
	@MemberOf("gMarkBackward")
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
	
	@MemberOf("gProperties")
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
	
	
	@Override
	public void runActivity(Body body) {
		MultiActiveService mas = (new MultiActiveService(body));
		//mas.multiActiveServing();
		
		mas.policyServing(ServingPolicyFactory.getMultiActivityPolicy());
		
		//(new Service(body)).fifoServing();
		
	}
}
