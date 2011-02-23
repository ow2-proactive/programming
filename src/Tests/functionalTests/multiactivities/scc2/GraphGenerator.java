package functionalTests.multiactivities.scc2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Generates a graph based on the parameters, writes it to stdout
 * @author Izso
 *
 */
public class GraphGenerator implements Runnable {
	
    int from;
    int to;
    int cnt;
    int groups;
    double groupD;
    double interD;
    
    
    
	public GraphGenerator(int from, int to, int cnt, int groups, double groupD, double interD) {
        super();
        this.from = from;
        this.to = to;
        this.cnt = cnt;
        this.groups = groups;
        this.groupD = groupD;
        this.interD = interD;
    }

    public Graph generateDirectedBA(int nodes) {
		Graph g = new Graph();
		
		int totalLinks = 0;
		
		// init the algorithm
		g.arcs.put(0, new HashSet<Integer>());
		g.arcs.get(0).add(1);
		printEdge(0,1);
		g.arcs.put(1, new HashSet<Integer>());
		g.arcs.get(1).add(0);
		printEdge(1,0);
		g.arcs.put(2, new HashSet<Integer>());
		//g.arcs.get(0).add(2);
		g.arcs.get(2).add(3);
		//printEdge(0,2);;
		printEdge(2,3);
		g.arcs.put(3, new HashSet<Integer>());
		g.arcs.get(3).add(2);
		printEdge(3,2);
		
		totalLinks = 4;
		
		for (int i=4; i<nodes; i++) {
			g.arcs.put(i, new HashSet<Integer>());
			for (int j=0; j<i; j++) {
				boolean connect = Math.random()<=(((float) g.arcs.get(j).size())/((float) totalLinks));
				boolean connectInv = Math.random()<=(((float) g.arcs.get(j).size())/((float) totalLinks));
				if (connect) {
					g.arcs.get(i).add(j);
					printEdge(i,j);
					totalLinks+=1;
				}
				
				if (connectInv) {
					g.arcs.get(j).add(i);
					printEdge(j,i);
					totalLinks+=1;
				}
			}
		}
			
		return g;
	}
	
	private static void printEdge(int i, int j) {
		System.out.println(i+">"+j);
	}
	
	public static void generateDensityBasedGraph(final int threads, final int piece, final int numPieces, final int cnt, final int groups, final double groupD, final double interD) throws InterruptedException {
		//System.out.println(cnt);		
		LinkedList<Thread> waitFor = new LinkedList();
		final int perPiece = cnt/numPieces;
		final int perThread = perPiece/threads;
		for (int t=0; t<threads; t++) { 
		    waitFor.add(new Thread(new GraphGenerator(piece*perPiece + (t*perThread), piece*perPiece + (t+1)*perThread, cnt, groups, groupD, interD)));
		}
		
		for (Thread t : waitFor) {
		    t.start();
		}
		
		for (Thread t : waitFor) {
		    t.join();
		}
		
	}
	
	public void run() {
	    for (int x=from; x<to; x++) {
            for (int y=0; y<cnt; y++) {
                if (x!=y) {
                    double random = Math.random();
                //  if same group
                    if (x/(cnt/groups) == y/(cnt/groups)) {
                        if (random<groupD) {
                            System.out.println(x+">"+y);
                        }
                    } else {
                        if (random<interD) {
                            System.out.println(x+">"+y);
                        }
                    }
                }
            }
        }
	}

	public static void main(String[] args) throws InterruptedException {
	    if (args.length!=7) {
	        System.out.println("Usage:\n<Number of nodes> <Number of groups> " +
	        		"<Edge probability inside a group> <Edge prob. between group> " +
	        		"<Number of threads> <worker id> <total workers>");
	        return;
	    }
	    
		Integer cnt = Integer.parseInt(args[0]);
		Integer groups = Integer.parseInt(args[1]);
		double gd = Double.parseDouble(args[2]);
		double id = Double.parseDouble(args[3]);
		int threads = Integer.parseInt(args[4]);
		int worker_id = Integer.parseInt(args[5]);
		int worker_count = Integer.parseInt(args[6]);
		generateDensityBasedGraph(threads, worker_id, worker_count, cnt, groups, gd, id);
	}
	
	public class Graph {
		
		public Graph() {
			arcs = new HashMap<Integer, Set<Integer>>();
		}
		
		public Map<Integer, Set<Integer>> arcs;
		
		@Override
		public String toString() {
			return "Graph arcs: "+arcs.toString();
		}
	}


}
