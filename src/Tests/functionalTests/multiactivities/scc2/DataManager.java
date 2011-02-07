package functionalTests.multiactivities.scc2;

import ibis.io.DataInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Not used for the moment. See the worker's inner class instead ;)
 * @author Izso
 *
 */
public class DataManager {
	private GraphWorker[] workers;
	private int total;
	private Integer nodeNumber;
	
	private HashMap<Integer, HashSet<Integer>> knows;
	
	public DataManager(GraphWorker[] workers) {
		this.workers = workers;
		total = workers.length;
		knows = new HashMap<Integer, HashSet<Integer>>();
	}
	
	public void loadAndDistribute(String path) {
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
			for (Integer i=0; i<numNodes; i++) {
				createNode(i);
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void generateAndDistribute(int nodeCount){
		createNode(0);
		createNode(1);
		createNode(2);
		createEdge(0, 1);
		createEdge(0, 2);
		createEdge(1, 2);
		
		int totalLinks = 3;
		
		for (Integer i=3; i<nodeCount; i++) {
			createNode(i);
			for (Integer j=0; j<i; j++) {
				boolean connect = Math.random()<=(((float) getEdgeCount(j))/((float) totalLinks));
				if (connect) {
					createEdge(i, j);
					totalLinks+=1;
				}
				
				connect = Math.random()<=(((float) getEdgeCount(i))/((float) totalLinks));
				if (connect) {
					createEdge(j, i);
					totalLinks+=1;
				}
			}
		}

	}
	
	public Integer getNumberOfNodes() {
		return nodeNumber;
	}
	
	private int hashLocation(Integer id) {
		return id/(nodeNumber/total);
	}
	
	private void createNode(Integer id) {
		//System.out.println("create "+node);
		workers[hashLocation(id)].addNode(id);
	}
	
	private void createEdge(Integer from, Integer to) {
		int fromWorker = hashLocation(from);
		int toWorker = hashLocation(to);
		
		workers[fromWorker].addEdge(from, to);
		if (fromWorker!=toWorker) {
			workers[toWorker].addEdge(from, to);
			
			introduceToEachOther(fromWorker, toWorker);
			
			workers[fromWorker].addNodeToNeighbor(to, toWorker);
			workers[toWorker].addNodeToNeighbor(from, fromWorker);
		}
	}
	
	private void introduceToEachOther(int workerOne, int workerTwo) {
		if (knows.containsKey(workerOne) && knows.get(workerOne).contains(workerTwo)) {
			return;
		}
		
		workers[workerOne].addNeighbor(workers[workerTwo], workerTwo);
		workers[workerTwo].addNeighbor(workers[workerOne], workerOne);
		
		if (!knows.containsKey(workerOne)) {
			knows.put(workerOne, new HashSet<Integer>());
		}
		if (!knows.containsKey(workerTwo)) {
			knows.put(workerTwo, new HashSet<Integer>());
		}
		
		knows.get(workerOne).add(workerTwo);
		knows.get(workerTwo).add(workerOne);
	}

	private Integer getEdgeCount(Integer id) {
		return workers[hashLocation(id)].numberOfEdges(id);
	}

}
