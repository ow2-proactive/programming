package functionalTests.multiactivities.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.multiactivity.CompatibleWith;
import org.objectweb.proactive.annotation.multiactivity.Reads;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;

public class VertexGroup implements RunActive {
	
	private Map<Integer, Set<Integer>> vertices;
	private Map<Integer, VertexGroup> externalNode;
	private Set<Integer> forwardMarked;
	private Set<Integer> backwardMarked;
	private String name;
	
	private Boolean forwardLock = new Boolean(true);
	private Boolean backwardLock = new Boolean(true);
	
	@CompatibleWith({"setupExternal"})
	public void setupVertices(Map<Integer, Set<Integer>> data) {
		vertices = data;
	}
	
	@CompatibleWith({"setupVertices"})
	public void setupExternal(Map<Integer, VertexGroup> data) {
		externalNode = data;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Reads({"vertices"})
	public Set<Integer> getVertices(){
		HashSet<Integer> ret = new HashSet<Integer>();
		for (Set<Integer> setI : vertices.values()) {
			for (Integer v : setI) {
				if (!externalNode.keySet().contains(v)) {
					ret.add(v);
				}
			}
		}
		for (Integer v : vertices.keySet()) {
			if (!externalNode.keySet().contains(v)) {
				ret.add(v);
			}
		}
		return ret;
	}

	@Override
	public void runActivity(Body body) {
		MultiActiveService mas = (new MultiActiveService(body));
		mas.multiActiveServing();
		//(new Service(body)).fifoServing();
	}
	
	@CompatibleWith({"markForward", "markBackward"})
	@Reads({"vertices"})
	public BooleanWrapper markForward(Integer node){
		//System.out.println("[F] "+"Entered "+this);
		synchronized (forwardLock) {
			if (forwardMarked!=null && forwardMarked.contains(node)) {
				//System.out.println(node+" already visited!");
				return new BooleanWrapper(false);
			} else if (forwardMarked==null) {
				forwardMarked = new HashSet<Integer>();
			}
			
			//mark the node
			forwardMarked.add(node);
			if (vertices.get(node)!=null) {
				for (Integer v : vertices.get(node)){
					if (!externalNode.keySet().contains(v)) {
						//System.out.println("[F] "+"Marked "+v);
						markForward(v);
					}
				}
			}
		}
		
		if (vertices.get(node)!=null) {
			for (Integer v : vertices.get(node)){
				if (externalNode.keySet().contains(v)) {
					//System.out.println("[F] "+"Following "+v+" into "+externalNode.get(v));
					externalNode.get(v).markForward(v);
				}
			}
		}
		
		return new BooleanWrapper(true);
	}
	
	@CompatibleWith({"getAllBackwardMarked", "getAllForwardMarked"})
	@Reads({"vertices"})
	public Set<Integer> getAllForwardMarked() {
		Set<Integer> allOther = new HashSet<Integer>();
		
		synchronized (forwardLock) {
			if (forwardMarked==null) {
				return new HashSet<Integer>();
			} else {
				allOther.addAll(forwardMarked);
				forwardMarked = null;
			}
			
		}
		
		Set<Integer> other;
		
		for (VertexGroup vg : externalNode.values()) {
			other = vg.getAllForwardMarked();
			if (other!=null && other.size()>0) {
				allOther.addAll(other);
			}
		}
		
		return allOther;
	}
	
	@CompatibleWith({"markBackward", "markForward"})
	@Reads({"vertices"})
	public BooleanWrapper markBackward(Integer node){
		//System.out.println("[B] Entered "+this);
		synchronized (backwardLock) {
			if (backwardMarked!=null && backwardMarked.contains(node)) {
				//System.out.println("[B] "+node+" already visited!");
				return new BooleanWrapper(false);
			} else if (backwardMarked==null) {
				backwardMarked = new HashSet<Integer>();
			}
			
			//mark the node
			backwardMarked.add(node);
			for (Integer v : vertices.keySet()){
				if (vertices.get(v).contains(node)) {
					if (!externalNode.keySet().contains(v)) {
						//System.out.println("[B] "+"Marked "+v);
						markBackward(v);
					}
				}	
			}
		}
		
		for (Integer v : vertices.keySet()){
			if (vertices.get(v).contains(node)) {
				if (externalNode.keySet().contains(v)) {
					//System.out.println("[B] "+"Following "+v+" into "+externalNode.get(v));
					externalNode.get(v).markBackward(v);
				}
			}	
		}
		
		return new BooleanWrapper(true);
	}
	
	@CompatibleWith({"getAllBackwardMarked", "getAllForwardMarked"})
	@Reads({"vertices"})
	public Set<Integer> getAllBackwardMarked() {
		Set<Integer> allOther = new HashSet<Integer>();
		synchronized (backwardLock) {
			if (backwardMarked==null) {
				return new HashSet<Integer>();
			} else {
				allOther.addAll(backwardMarked);
				backwardMarked = null;
			}
			
		}
		
		Set<Integer> other;
		
		for (Integer incoming : vertices.keySet()) {
			if (externalNode.get(incoming)!=null) {
				other = externalNode.get(incoming).getAllBackwardMarked();
				if (other!=null && other.size()>0) {
					allOther.addAll(other);
				}
			}
			
		}
		
		return allOther;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
