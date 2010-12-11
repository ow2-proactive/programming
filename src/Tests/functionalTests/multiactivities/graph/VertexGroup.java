package functionalTests.multiactivities.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.multiactivity.MultiActiveService;

public class VertexGroup implements RunActive {
	
	private Map<Integer, List<Integer>> vertices;
	private Map<Integer, VertexGroup> location;
	private List<Integer> forwardMarked;
	private List<Integer> backwardMarked;
	private String name;
	private MultiActiveService mas;
	
	@CompatibleWith({"setupLocation"})
	public void setupVertices(Map<Integer, List<Integer>> data) {
		vertices = data;
	}
	
	@CompatibleWith({"setupVertices"})
	public void setupLocation(Map<Integer, VertexGroup> data) {
		location = data;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void runActivity(Body body) {
		mas = (new MultiActiveService(body));
		mas.multiActiveServing();
	}
	
	@CompatibleWith({"markForward"})
	public Boolean markForward(Integer node){
		System.out.println("Entered "+this);
		synchronized (location) {
			if (forwardMarked!=null && forwardMarked.contains(node)) {
				System.out.println(node+" already visited!");
				return false;
			} else if (forwardMarked==null) {
				forwardMarked = new ArrayList<Integer>();
			}
			
			//mark the node
			forwardMarked.add(node);
			if (vertices.get(node)!=null) {
				for (Integer v : vertices.get(node)){
					if (!location.keySet().contains(v)) {
						System.out.println("Marked "+v);
						markForward(v);
					}
				}
			}
		}
		
		if (vertices.get(node)!=null) {
			for (Integer v : vertices.get(node)){
				if (location.keySet().contains(v)) {
					System.out.println("Following "+v+" into "+location.get(v));
					location.get(v).markForward(v);
				}
			}
		}
		
		return true;
	}
	
	@CompatibleWith({"getAllForwardMarked"})
	public List<Integer> getAllForwardMarked() {
		List<Integer> allOther = new LinkedList<Integer>();
		
		synchronized (location) {
			if (forwardMarked==null) {
				return new LinkedList<Integer>();
			} else {
				allOther.addAll(forwardMarked);
				forwardMarked = null;
			}
			
		}
		
		List<Integer> other;
		
		for (VertexGroup vg : location.values()) {
			other = vg.getAllForwardMarked();
			if (other!=null && other.size()>0) {
				allOther.addAll(other);
			}
		}
		
		return allOther;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
