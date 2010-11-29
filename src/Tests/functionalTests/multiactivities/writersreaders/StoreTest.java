package functionalTests.multiactivities.writersreaders;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.multiactivities.pingpong.Pinger;

public class StoreTest {
	
	@Test
	public void simpleMultiActiveTest() throws ActiveObjectCreationException, NodeException{
		Object[] constrPrm = { false };
		IntegerStore ist = PAActiveObject.newActive(IntegerStore.class, constrPrm);
		List<Runnable> tasks = new LinkedList<Runnable>();
		//init
		tasks.add(new Writer(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Writer(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Writer(ist));
		System.out.println("NORMAL TEST:");
		for (int i=0; i<tasks.size()-1; i++) {
			(new Thread(tasks.get(i))).start();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		(tasks.get(tasks.size()-1)).run();
	}
	
	@Test
	public void greedyMultiActiveTest() throws ActiveObjectCreationException, NodeException{
		Object[] constrPrm = { true };
		IntegerStore ist = PAActiveObject.newActive(IntegerStore.class, constrPrm);
		List<Runnable> tasks = new LinkedList<Runnable>();
		//init
		tasks.add(new Writer(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Writer(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Reader(ist));
		tasks.add(new Writer(ist));
		System.out.println("GREEDY TEST: ");
		for (int i=0; i<tasks.size()-1; i++) {
			(new Thread(tasks.get(i))).start();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		(tasks.get(tasks.size()-1)).run();
	}
	
	public class Writer implements Runnable {
		public IntegerStore is;
		
		public Writer(IntegerStore is){
			this.is = is;
		}
		
		@Override
		public void run() {
			
			System.out.println("Written "+is.write(new Integer((int) (Math.random()*100))));
		}
		
	}
	
	public class Reader implements Runnable {
		public IntegerStore is;
		
		public Reader(IntegerStore is){
			this.is = is;
		}
		
		@Override
		public void run() {
			System.out.println("Read "+is.read());
		}
		
	}

}
