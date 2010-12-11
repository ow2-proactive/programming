package functionalTests.multiactivities.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

public class ForwardMarkTest {
	
	@Test
	public void simpleForwardMarkTest() throws ActiveObjectCreationException, NodeException{
		VertexGroup vg1 = PAActiveObject.newActive(VertexGroup.class, null);
		VertexGroup vg2 = PAActiveObject.newActive(VertexGroup.class, null);
		VertexGroup vg3 = PAActiveObject.newActive(VertexGroup.class, null);
		VertexGroup vg4 = PAActiveObject.newActive(VertexGroup.class, null);
		
		//1
		HashMap<Integer, List<Integer>> conn1 = new HashMap<Integer, List<Integer>>();
		conn1.put(1, new LinkedList<Integer>());
		conn1.get(1).add(2);
		conn1.get(1).add(3);
		conn1.put(2, new LinkedList<Integer>());
		conn1.get(2).add(7);
		HashMap<Integer, VertexGroup> neigh1 = new HashMap<Integer, VertexGroup>();
		neigh1.put(3, vg2);
		neigh1.put(7, vg4);
		
		//2
		HashMap<Integer, List<Integer>> conn2 = new HashMap<Integer, List<Integer>>();
		conn2.put(4, new LinkedList<Integer>());
		conn2.get(4).add(5);
		conn2.get(4).add(3);
		HashMap<Integer, VertexGroup> neigh2 = new HashMap<Integer, VertexGroup>();
		neigh2.put(5, vg3);
		
		//3
		HashMap<Integer, List<Integer>> conn3 = new HashMap<Integer, List<Integer>>();
		conn3.put(5, new LinkedList<Integer>());
		conn3.get(5).add(6);
		conn3.put(6, new LinkedList<Integer>());
		conn3.get(6).add(4);
		HashMap<Integer, VertexGroup> neigh3 = new HashMap<Integer, VertexGroup>();
		neigh3.put(4, vg2);
		
		//4
		HashMap<Integer, List<Integer>> conn4 = new HashMap<Integer, List<Integer>>();
		conn4.put(7, new LinkedList<Integer>());
		conn4.get(7).add(8);
		conn4.get(7).add(6);
		HashMap<Integer, VertexGroup> neigh4 = new HashMap<Integer, VertexGroup>();
		neigh4.put(6, vg3);
		
		vg1.setupLocation(neigh1);
		vg1.setupVertices(conn1);
		vg1.setName("Group[1,2]");
		vg2.setupLocation(neigh2);
		vg2.setupVertices(conn2);
		vg2.setName("Group[3,4]");
		vg3.setupLocation(neigh3);
		vg3.setupVertices(conn3);
		vg3.setName("Group[5,6]");
		vg4.setupLocation(neigh4);
		vg4.setupVertices(conn4);
		vg4.setName("Group[7,8]");
		
		vg1.markForward(1);
		int value = vg1.getAllForwardMarked().size();
		System.out.println(value);
		Assert.assertTrue(value==8);

	}

}
