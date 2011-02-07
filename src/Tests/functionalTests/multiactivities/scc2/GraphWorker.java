package functionalTests.multiactivities.scc2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;

@DefineGroups( { 
    @Group(name = "Forward", selfCompatible = true),
	@Group(name = "Backward", selfCompatible = true),
	@Group(name = "Info", selfCompatible = true),
	@Group(name = "Transaction", selfCompatible = true)
})
@DefineRules( {
    @Compatible( { "Backward", "Info", "Forward", "Transaction" })
})
public class GraphWorker implements RunActive {

	private String name;

	// set of vertexes of the graph that belong to this worker
	private HashSet<Integer> ownedNodes;
	// set of edges that belong to this worker
	private HashMap<Integer, HashSet<Integer>> localEdges;
	// same as the localEdges but with inverted direction
	private HashMap<Integer, HashSet<Integer>> invertedEdges;
	// edges coming from the "outside"
	private HashMap<Integer, HashSet<Integer>> incomingEdges;
	// edges leaving this worker
	private HashMap<Integer, HashSet<Integer>> outgoingEdges;

	// map of IDs to workers
	private HashMap<Integer, GraphWorker> neighbors;
	// map of referenced external nodes and their location ID
	private HashMap<Integer, Integer> nodeLocation;

	// hashmap of visited set for each transaction
	private HashMap<Integer, HashSet<Integer>> visitedF;
	private HashMap<Integer, HashSet<Integer>> visitedB;
	
	//the body is multiactive or not
	private boolean multiActive;

	public GraphWorker(String name, boolean multiActive) {
		this.name = name;
		this.multiActive = multiActive;
	}

	public GraphWorker() {
		this.name = "Noname";
	}

	// GROUP INIT
	// ##############################################################################
	public void init() {
		ownedNodes = new HashSet<Integer>();
		localEdges = new HashMap<Integer, HashSet<Integer>>();
		invertedEdges = new HashMap<Integer, HashSet<Integer>>();
		incomingEdges = new HashMap<Integer, HashSet<Integer>>();
		outgoingEdges = new HashMap<Integer, HashSet<Integer>>();
		nodeLocation = new HashMap<Integer, Integer>();
		neighbors = new HashMap<Integer, GraphWorker>();
		visitedF = new HashMap<Integer, HashSet<Integer>>();
		visitedB = new HashMap<Integer, HashSet<Integer>>();
	}
	
	/**
	 * 
	 * @param workers list of all workers
	 * @param path file containing the graph
	 * @param seqNum this worker's ID -- used for node location decision
	 */
	public void loadEdges(GraphWorker[] workers, String path, int seqNum) {
	    new DataManager(workers, seqNum).loadAndDistribute(path);
	}

	//these could be protected also-------------------------------------------
	
	public int getOwnedNodesCount() {
        return ownedNodes.size();
    }

    public Integer addNode(Integer id) {
		// System.out.println("Add node "+id+" to "+name);
		ownedNodes.add(id);
		return 0;
	}

	public Integer addNeighbor(GraphWorker worker, Integer name) {
		// System.out.println("Add neigh. "+name+" to "+this.name);
		neighbors.put(name, worker);
		return 0;
	}

	public Integer addNodeToNeighbor(Integer id, Integer name) {
		// System.out.println("Add node "+id+" to neigh "+name+" in "+this.name);
		nodeLocation.put(id, name);
		return 0;
	}
	
	public Integer addEdge(Integer from, Integer to) {
		// System.out.println("Add edge "+from+"->"+to+" to "+name);
		if (ownedNodes.contains(from) && ownedNodes.contains(to)) {
			if (!localEdges.containsKey(from)) {
				localEdges.put(from, new HashSet<Integer>());
			}
			localEdges.get(from).add(to);

			if (!invertedEdges.containsKey(to)) {
				invertedEdges.put(to, new HashSet<Integer>());
			}
			invertedEdges.get(to).add(from);
		} else if (ownedNodes.contains(from)) {
			if (!outgoingEdges.containsKey(from)) {
				outgoingEdges.put(from, new HashSet<Integer>());
			}
			outgoingEdges.get(from).add(to);
		} else if (ownedNodes.contains(to)) {
			if (!incomingEdges.containsKey(to)) {
				incomingEdges.put(to, new HashSet<Integer>());
			}
			incomingEdges.get(to).add(from);
		}
		return 0;
	}

	public Integer numberOfEdges(Integer id) {
		return (localEdges.containsKey(id) ? localEdges.get(id).size() : 0)
				+ (outgoingEdges.containsKey(id) ? outgoingEdges.get(id).size()
						: 0);
	}

	// GROUP
	// FORWARD##############################################################################

	@MemberOf("Forward")
	public Set<Integer> markForward(Integer transaction, Integer id) {
		return mark(transaction, id, localEdges, outgoingEdges, true);
	}

	// GROUP
	// BACKWARD##############################################################################

	@MemberOf("Backward")
	public Set<Integer> markBackward(Integer transaction, Integer id) {
		return mark(transaction, id, invertedEdges, incomingEdges, false);
	}

	// GROUP
	// MANAGEMENT##############################################################################
	@MemberOf("Transaction")
	public IntWrapper removeTransaction(Integer transaction) {
		// System.out.println("Tx - "+transaction+" of "+name);
		return new IntWrapper(checkRemoveTransaction(transaction));
	}

	@MemberOf("Transaction")
	public IntWrapper addTransaction(Integer transaction, Integer parent, Integer mode) {
		// System.out.println("Tx + "+transaction+" based on "+parent+" mode "+mode+" in "+name);
		return new IntWrapper(inheritTransaction(transaction, parent, mode));
	}

	// GROUP
	// INFO##############################################################################

	@MemberOf("Info")
	@Override
	public String toString() {
		return name;
	}

	@MemberOf("Info")
	public String getDetails() {
		return "Forward edges: " + localEdges + "\n" + "Outgoing edges: "
				+ outgoingEdges + "\n" + "Incoming (inverted) edges: "
				+ incomingEdges + "\n";
	}

	// ##############################################################################

	private Set<Integer> mark(Integer transaction, Integer id,
			HashMap<Integer, HashSet<Integer>> normal,
			HashMap<Integer, HashSet<Integer>> border, boolean forward) {
		// synchronize access on visited!
		Set<Integer> visited = checkGetTransaction(transaction, forward);
		LinkedList<Set<Integer>> results = new LinkedList<Set<Integer>>();
		Set<Integer> visitedNow = new HashSet<Integer>();

		synchronized (visited) {
			if (visited.contains(id)) {
				return new LinkedHashSet<Integer>();
			}
		

		LinkedList<Integer> q = new LinkedList<Integer>();
		q.add(id);
		Integer current;
		while (q.size() > 0) {
			current = q.removeFirst();
			// no need for synchro because even if more instances of the same mark exist 
			// on the same transaction, only one will be active...
//			synchronized (visited) {
				if (!visited.contains(current)) {
					visited.add(current);
					visitedNow.add(current);
				} else {
					continue;
				}
			//}
			// add locals
			if (normal.get(current) != null) {
				q.addAll(normal.get(current));
			}

			// follow external links, save ref for further collection;
			if (border.get(current) != null) {
				for (Integer out : border.get(current)) {
					results.add(propagateMark(transaction, out, forward));
				}
			}

		}
		}
		// collect result
		for (Set<Integer> res : results) {
			Set<Integer> copy = res;
			try {
				visitedNow.addAll(copy);
			} catch (NullPointerException npe) {
				// oops
			}
		}

		return visitedNow;
	}

	private Set<Integer> propagateMark(Integer transaction, Integer id,
			boolean forward) {
		GraphWorker other = (neighbors.get(nodeLocation.get(id)));
		if (other==null) {
		    System.out.println("WTF?");
		}
		return (forward) ? other.markForward(transaction, id) : other.markBackward(transaction, id);
	}

	/**
	 * Lookup the visited set of the given transaction (create it if it does not exist)
	 * @param transaction
	 * @param forward
	 * @return
	 */
	private Set<Integer> checkGetTransaction(Integer transaction,  
			boolean forward) {
		HashMap<Integer, HashSet<Integer>> visitedGlobal = forward ? visitedF : visitedB;
		HashSet<Integer> visited;
		synchronized (visitedGlobal) {
			if (!visitedGlobal.containsKey(transaction)) {
				visitedGlobal.put(transaction, new HashSet<Integer>());
			}
			visited = visitedGlobal.get(transaction);
		}
		return visited;
	}

	/**
	 * Create a new transaction using the visited sets of an other transaction
	 * @param transaction new ID
	 * @param parent old ID
	 * @param mode values: 0=merge the B and F marks; +1=keep only B; -1=keep only F;
	 * @return
	 */
	private Integer inheritTransaction(Integer transaction, Integer parent,
			Integer mode) {

		if (parent == null) return transaction;
		
		Set<Integer> visited;
		HashSet<Integer> setNewF;
		HashSet<Integer> setNewB;

		synchronized (visitedF) {
			synchronized (visitedB) {
				Set<Integer> setOldF = visitedF.get(parent);
				Set<Integer> setOldB = visitedB.get(parent);
				if (parent!=transaction) {
					setNewF = new HashSet<Integer>();
					setNewB = new HashSet<Integer>();
					if (mode.equals(0)) {
						if (setOldF!=null) setNewF.addAll(setOldF);
						if (setOldB!=null) setNewF.addAll(setOldB);
						
						if (setOldF!=null) setNewB.addAll(setOldF);
						if (setOldB!=null) setNewB.addAll(setOldB);
					} else if (mode > 0) {
						if (setOldB!=null) setNewF.addAll(setOldB);
	
						if (setOldB!=null) setNewB.addAll(setOldB);
					} else if (mode < 0) {
						if (setOldF!=null) setNewF.addAll(setOldF);
						
						if (setOldF!=null) setNewB.addAll(setOldF);
					}
					
					visitedF.put(transaction, setNewF);
					visitedB.put(transaction, setNewB);
				} else {
					setOldF.addAll(setOldB);
					setOldB.addAll(setOldF);
				}

			//System.out.println("TX created " + transaction + "@" + name + " p=" + parent + " m=" + mode + " v=" + setNew);
			}
		}
		return transaction;
	}

	/**
	 * Clean up after a transaction
	 * @param transaction
	 * @return
	 */
	private Integer checkRemoveTransaction(Integer transaction) {
		synchronized (visitedF) {
			synchronized (visitedB) {
				if (visitedF.containsKey(transaction)) {
					visitedF.remove(transaction);
				}
				if (visitedB.containsKey(transaction)) {
					visitedB.remove(transaction);
				}
			}
		}
		return transaction;
	}
	
	/**
	 * Loads a graph's relevant edges for this worker from a file.
	 * @author Izso
	 *
	 */
	public class DataManager {
	    private GraphWorker[] workers;
	    private int total;
	    private int thisNum;
	    private Integer nodeNumber;
	    
	    private HashMap<Integer, HashSet<Integer>> knows;
	    
	    public DataManager(GraphWorker[] workers, int seqNum) {
	        this.workers = workers;
	        total = workers.length;
	        knows = new HashMap<Integer, HashSet<Integer>>();
	        thisNum = seqNum;
	    }
	    
	    public boolean loadAndDistribute(String path) {
	        File file = new File(path);
	        FileInputStream fis = null;
	        InputStreamReader isr = null;
	        BufferedReader br = null;

	        try {
	            fis = new FileInputStream(file);
	            isr = new InputStreamReader(fis);
	            br = new BufferedReader(isr);
	            
	            Integer numNodes = Integer.parseInt(br.readLine());
	            nodeNumber = numNodes;
	            int upperBound = (thisNum+1)*(nodeNumber/total);
	            int lowerBound = thisNum*(nodeNumber/total);
	            for (Integer i=lowerBound; i<upperBound; i++) {
	                addNode(i);
	            }
	            System.out.print("Loading links");
	            String[] edge;
	            int cnt = 0;
	            while (br.ready()){
	                edge = br.readLine().split(">");
	                createEdge(Integer.parseInt(edge[0]), Integer.parseInt(edge[1]));
	            }
	            System.out.println(" Done.");

	            fis.close();
	            isr.close();
	            br.close();

	        } catch (FileNotFoundException e) {
	            return false;
	        } catch (IOException e) {
	            return false;
	        }
	        return true;
	    }
	    
	    public Integer getNumberOfNodes() {
	        return nodeNumber;
	    }
	    
	    private int hashLocation(Integer id) {
	        return id/(nodeNumber/total);
	    }
	    
	    private void createEdge(Integer from, Integer to) {
	        int fromWorker = hashLocation(from);
	        int toWorker = hashLocation(to);
	        
	        if (fromWorker!=thisNum && toWorker!=thisNum) return;
	        
	        addEdge(from, to);
	        if (fromWorker!=toWorker) {
	            if (fromWorker!=thisNum) {
	                addNeighbor(workers[fromWorker], fromWorker);
	                addNodeToNeighbor(from, fromWorker);
	            } else {
	                addNeighbor(workers[toWorker], toWorker);
                    addNodeToNeighbor(to, toWorker);
	            }
	        }
	    }
	}

	@Override
	public void runActivity(Body body) {
		if (multiActive) {
			(new MultiActiveService(body)).multiActiveServing();
		} else {
			(new MultiActiveService(body)).fifoServing();
		}
	}

}
