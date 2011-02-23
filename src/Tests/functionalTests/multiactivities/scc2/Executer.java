package functionalTests.multiactivities.scc2;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;

/**
 * Performs the scc algorithm
 * @author Izso
 *
 */
public class Executer {
    /**
     * Not USED!
     */
	//private ExecutorService execs = Executors.newCachedThreadPool();
	
	private GraphWorker[] workers;
	private static Integer nodeNum;
	private AtomicInteger txId = new AtomicInteger();

	public Executer(GraphWorker[] workers) {
		this.workers = workers;
		txId.set(0);
	}

	public Integer inheritTransaction(Integer tx, Integer pA, Integer pB) {
		IntWrapper[] res = new IntWrapper[workers.length];
		//make them work
		for (int i = 0; i < workers.length; i++) {
			res[i] = workers[i].addTransaction(tx, pA, pB);
		}
		//wait for all
		for (int i = 0; i < workers.length; i++) {
			res[i].getIntValue();
		}
		return tx;
	}

	public void deleteTransaction(Integer tx) {
		IntWrapper[] res = new IntWrapper[workers.length];
		for (int i = 0; i < workers.length; i++) {
			res[i] = workers[i].removeTransaction(tx);
		}
		for (int i = 0; i < workers.length; i++) {
			res[i].getIntValue();
		}
	}

	public void runAlgorithm(int maxBranches, Integer numNodes) {

		HashSet<Integer> alive = doInit(numNodes);
		
		//not USED
		//if (maxBranches>0) execs = Executors.newFixedThreadPool(maxBranches);

		Date tStart = new Date();
		// -------------------------------------

		new FBThread(alive, inheritTransaction(txId.getAndIncrement(), null, 0))
				.run();

		// -------------------------------------
		Date tFinal = new Date();
		System.err.println((tFinal.getTime() - tStart.getTime()));
	}

	public class FBThread implements Runnable {
		Set<Integer> alive;
		Set<Integer> toSend = new HashSet<Integer>();
		Integer tx;

		public FBThread(Set<Integer> alive, Integer tx) {
			this.alive = alive;
			this.tx = tx;
		}

		public void run() {
			LinkedList<Thread> toWait = new LinkedList<Thread>();
			LinkedList<Integer> toRemove = new LinkedList<Integer>();
			//do not start new thread for the third branch, use the loop
			while (alive.size() > 1) {
				// System.out.print("<");

				Integer pivot = alive.iterator().next();
				//find location of pivot
				int owner = pivot / (nodeNum / workers.length);
				toSend.clear(); 
				toSend.add(pivot);
				Set<Integer> fm = workers[owner].markForward(tx, toSend);
				Set<Integer> bm = workers[owner].markBackward(tx, toSend);

				//System.out.println(fm.size()+" "+bm.size()+" | "+alive.size()+" pivot "+pivot);

				// System.out.print(">");

				Set<Integer> common = new HashSet<Integer>();
				common.addAll(bm);
				common.retainAll(fm);
				if (common.size() > 1) {
					System.out.println("Found SCC of size " + common.size());// +" values are "+common);
					// System.out.print("*");
				}

				Set<Integer> b_f = new HashSet<Integer>();
				b_f.addAll(bm);
				b_f.removeAll(fm);
				Thread t = null;
				if (b_f.size() > 1) {
				    //new thread.
				    Integer newBackward = null;
					newBackward = inheritTransaction(txId.getAndIncrement(),
							tx, -1);
					t = new Thread(new FBThread(b_f, newBackward));
					t.start();
					toWait.add(t);
					////do not start a new thread
					//new FBThread(b_f, newBackward).run();
					toRemove.add(newBackward);
				}

				Set<Integer> f_b = new HashSet<Integer>();
				f_b.addAll(fm);
				f_b.removeAll(bm);
				Thread t2 = null;
				if (f_b.size() > 1) {
				    //new thread
				    Integer newForward = null;
					newForward = inheritTransaction(txId.getAndIncrement(), tx,
							1);
					t2 = new Thread(new FBThread(f_b, newForward));
					t2.start();
					toWait.add(t2);
					////do not use a new thread
					//new FBThread(f_b, newForward).run();
					toRemove.add(newForward);
				}

				/*
				Set<Integer> others = new HashSet<Integer>();
				others.addAll(alive);
				others.removeAll(bm);
				others.removeAll(fm);
				Thread t3 = null;
				if (others.size() > 1) {
					newOther = inheritTransaction(tx, tx, 0);
					t3 = new Thread(new FBThread(others, newOther));
					t3.start();
					toWait.add(t3);
				}
				*/
				
				Integer x = inheritTransaction(tx, tx, 0);
				alive.removeAll(bm);
				alive.removeAll(fm);
				tx = x + 0;
				/*
				alive.removeAll(bm);
				alive.removeAll(fm);
				inheritTransaction(tx, tx, 0);
				*/
				/*
				 * try { if (t!=null) t.join(); if (t2!=null) t2.join(); if
				 * (t3!=null) t3.join();
				 * 
				 * if (newOther!=null) deleteTransaction(newOther); if
				 * (newForward!=null) deleteTransaction(newForward); if
				 * (newBackward!=null) deleteTransaction(newBackward);
				 * 
				 * } catch (InterruptedException e) { // TODO Auto-generated
				 * catch block e.printStackTrace(); }
				 */
			}
			
			for (Thread t : toWait) {
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			deleteTransaction(tx);
			for (Integer rt : toRemove) {
				deleteTransaction(rt);
			}
		}

	}

	private HashSet<Integer> doInit(Integer numNodes) {
		nodeNum = numNodes;
		HashSet<Integer> alive = new HashSet<Integer>();
		for (Integer index = 0; index < numNodes; index++) {
			alive.add(index);
		}
		return alive;
	}

}
