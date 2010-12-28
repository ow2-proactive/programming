package functionalTests.multiactivities.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.StartNode;

public class VertexGroupFactory {
	
	/**
	 * 
	 * @param edges {"from-to", "1-2", "5-7" ...}
	 * @param numGroups number of vertex groups to create
	 * @return
	 */
	static VertexGroup[] getVertexGroupsFor(String[] edges, int numhosts, boolean fromNodes, String[] nodes) {
		int numGroups = numhosts;
		Map<Integer, Set<Integer>> graph = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> location = new HashMap<Integer, Integer>();
		//add edges
		for (String edge : edges) {
			String[] vertices = edge.split("-");
			Integer from = new Integer(vertices[0]);
			Integer to = new Integer(vertices[1]);
			
			if (graph.get(from)==null) {
				graph.put(from, new HashSet<Integer>());
			}
			if (!from.equals(to)) {
				graph.get(from).add(to);
			}
		}
		VertexGroup[] vertexGroups = new VertexGroup[numGroups];
		for (int i=0 ; i<numGroups; i++) {
			VertexGroup toAdd;
			try {
				toAdd = (PAActiveObject.newActive(VertexGroup.class, null, fromNodes ? nodes[i] : null));
			} catch (ActiveObjectCreationException e) {
				e.printStackTrace();
				return null;
			} catch (NodeException e) {
				e.printStackTrace();
				return null;
			}
			vertexGroups[i]=toAdd;
			
		}
		System.out.println("* Created nodes");
		List<Map<Integer, Set<Integer>>> connections = new LinkedList<Map<Integer,Set<Integer>>>();
		
		int count = 0;
		int total = graph.keySet().size();
		int addTo = 0;
		for (Integer k : graph.keySet()) {
			addTo = Math.min(count/(total/numGroups), numGroups-1);
			if (connections.size()<=addTo) {
				connections.add(new HashMap<Integer, Set<Integer>>());
			}
			connections.get(addTo).put(k, graph.get(k));
			location.put(k, addTo);
			count++;
		}
		System.out.println("* distributed vertexes");
		List<Map<Integer, VertexGroup>> external = new LinkedList<Map<Integer,VertexGroup>>();
		for (int i=0; i<numGroups; i++) {
			external.add(new HashMap<Integer, VertexGroup>());
		}
		
		for (int i=0; i<numGroups; i++) {
			for (Integer key : connections.get(i).keySet()) {
				for (Integer node : connections.get(i).get(key)) {
					if (location.get(node)!=null && !location.get(node).equals(i)) {
						int other = location.get(node);
						external.get(i).put(node, vertexGroups[other]);
						external.get(other).put(key, vertexGroups[i]);
						if (connections.get(other).get(key)==null){
							connections.get(other).put(key, new HashSet<Integer>());
						}
						connections.get(other).get(key).add(node);
					}
				}
			}
		}
		System.out.println("* copy data to AOs");
		for (int i=0; i<numGroups; i++) {
			vertexGroups[i].setName("Group "+i);
			vertexGroups[i].setupVertices(connections.get(i));
			vertexGroups[i].setupExternal(external.get(i));
		}
		System.out.println("* DONE copy data to AOs");
		return vertexGroups;
		
	}

}
