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
	
	public static void getPartitionedDirectedGraph(int vertices, float density, int partition) {
		
		for (int i=0; i<vertices; i++) {
			for (int j=0; j<vertices; j++) {
				
				double chance = Math.random();
				if (chance>1-density && i%partition==j%partition) {
					System.out.println(i+"-"+j);
				}
				
			}
		}
	}

	
	public static void main(String[] args) {
		if (args.length==2) {
			getUniformDirectedGraph(Integer.parseInt(args[0]), Float.parseFloat(args[1]));
		} else if (args.length==3) {
			getPartitionedDirectedGraph(Integer.parseInt(args[0]), Float.parseFloat(args[1]), Integer.parseInt(args[2]));
		}
	}

}
