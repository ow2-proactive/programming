package functionalTests.multiactivities.can.test;

import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;

import functionalTests.multiactivities.can.Key;
import functionalTests.multiactivities.can.Peer;
import functionalTests.multiactivities.can.Zone;

public class SymmetricTest {
	
	static AtomicInteger tm = new AtomicInteger(0);
	
	public static void main(String[] args)  {
		try {
		if (args.length<5) {
			System.out.println("USAGE: <MAO/SAO> <NUM_OF_PEERS> <NUM_OF_MSG_X> <NUM_OF_MSG_Y> <MSG_SIZE> <LIST OF ADDR>");
			System.exit(0);
		}
		
		boolean isMao = args[0].startsWith("MAO");
		final int totalPeers = Integer.parseInt(args[1]);
		int xMessages = Integer.parseInt(args[2]);
		int yMessages = Integer.parseInt(args[3]);
		final int msgSize = Integer.parseInt(args[4]);
		String[] nodeAddr = new String[0];
		if (args.length>5) {
			nodeAddr = args[5].split(";");
		}
		
		Peer.IS_MAO = isMao;
		
		int x=0;
		int TOTAL = totalPeers;
		
		Peer[] aos = new Peer[TOTAL];
		for (int i=0; i<TOTAL; i++) {
			aos[i] = PAActiveObject.newActive(Peer.class, new Object[] {(""+i), isMao}, args.length>5 ? "rmi://"+nodeAddr[i%nodeAddr.length]+":1099/worker" : null);
		}
		
		List<Peer> peers = new LinkedList<Peer>();
		peers.add(aos[0]);
		peers.get(0).createNetwork();
		
		List<Zone> msg = new LinkedList<Zone>();
		for (int z=0; z<msgSize; z++) {
			msg.add(new Zone((int)(z), (int)(z), (int)(z), (int)(z), null));
		}
		
		 ObjectOutput out = new ObjectOutputStream(new FileOutputStream("msgSize"+msgSize));
		 out.writeObject(msg);
		 out.close();
		
		Key key1 = new Key(Zone.MAX_X-10, Zone.MAX_Y-10);
		
		while (x<TOTAL-1) {
			List<Peer> newPeers = new LinkedList<Peer>();
			newPeers.addAll(peers);
			
			for (Peer p : peers) {
				x++;
				Peer newPeer = aos[x];
				PAFuture.waitFor(newPeer.join(p));
				newPeers.add(newPeer);
			}
			
			peers = newPeers;
			
		}
		
		Thread.sleep(1000);
		
		peers.get(0).add(key1, (Serializable) msg);
		
		long startLookup = System.currentTimeMillis();
		
		List<Object> results = new LinkedList<Object>();
		for (int i=0; i<xMessages; i++) {
			results.add(peers.get((int) 0).lookup(key1));
		}
		
		PAFuture.waitForAll(results);
		
		long endLookup = System.currentTimeMillis();
		
		System.out.println("Time is "+(endLookup-startLookup));
		System.exit(0);
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}

}
