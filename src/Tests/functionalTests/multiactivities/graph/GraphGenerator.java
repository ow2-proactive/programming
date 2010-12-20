package functionalTests.multiactivities.graph;

import java.util.LinkedList;
import java.util.List;

public class GraphGenerator {
	
	public static void getUniformDirectedGraph(int vertices, float density) {
		
		for (int i=0; i<vertices; i++) {
			for (int j=0; j<vertices; j++) {
				
				double chance = Math.random();
				if (chance>1-density) {
					System.out.println(i+"-"+j);
				}
				
			}
		}
	}
	
	public static void main(String[] args) {
		getUniformDirectedGraph(Integer.parseInt(args[0]), Float.parseFloat(args[1]));
	}

}
