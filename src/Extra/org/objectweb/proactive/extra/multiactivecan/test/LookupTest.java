package org.objectweb.proactive.extra.multiactivecan.test;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extra.multiactivecan.Key;
import org.objectweb.proactive.extra.multiactivecan.Peer;
import org.objectweb.proactive.extra.multiactivecan.Zone;


public class LookupTest {
	
	static AtomicInteger tm = new AtomicInteger(0);
	
	public static final int CORNER_TO_CORNER = 1;
	public static final int CORNER_TO_THREE_CORNERS = 3;
	public static final int TWO_CORNERS_TO_ALL = 0;
	
	public static void main(String[] args) {
		
		try {
			if (args.length<4) {
				System.out.println("USAGE: <OBJECT_YPE> <NUM_OF_PEERS> <NUM_OF_LOOKUPS> <LOOKUP_TYPE> <MSG_SIZE> [<PA_NODE_ADDR>]");
				System.out.println("	OBJECT_TYPE = MAO/SAO");
				System.out.println("	NUM_OF_PEERS = number of nodes in the network - power of 2!");
				System.out.println("	NUM_OF_LOOKUPS = total number of lookups to do");
				System.out.println("	LOOKUP_TYPE = 1/3/0 ('1' is key located in one corner, lookup from the opposite corner; " +
									" '3' is keys in three corners, lookups from the fourth; " +
									" '0' is keys in all peers, lookup from two opposite corners");
				System.out.println("	MSG_SIZE = the size of the value stored, as number of units (a unit is 23Bytes)");
				System.out.println("	PA_NODE_ADDR = address of machines which have the PA noded (called worker); just put machine_name, the rest is appended");
				
				System.exit(0);
			}
			
			boolean isMao = args[0].startsWith("MAO");
			final int totalPeers = Integer.parseInt(args[1]);
			int numMessages = Integer.parseInt(args[2]);
			int behaviour = Integer.parseInt(args[3]);
			final int msgSize = Integer.parseInt(args[4]);
			String[] nodeAddr = new String[0];
			if (args.length>5) {
				nodeAddr = args[5].split(";");
			}
			
			Peer.IS_MAO = isMao;
			
			int x=0;

			Peer[] aos = new Peer[totalPeers];
			
			if (behaviour!=TWO_CORNERS_TO_ALL) {
				for (int i=0; i<totalPeers; i++) {
					aos[i] = PAActiveObject.newActive(Peer.class, new Object[] {(""+i), isMao}, args.length>5 ? "rmi://"+nodeAddr[i%nodeAddr.length]+":1099/worker" : null);
				}
			} else {
				for (int i=0; i<totalPeers; i++) {
					aos[i] = PAActiveObject.newActive(LookupPeer.class, new Object[] {(""+i), isMao}, args.length>5 ? "rmi://"+nodeAddr[i%nodeAddr.length]+":1099/worker" : null);
				}
			}
			
			List<Peer> peers = new LinkedList<Peer>();
			peers.add(aos[0]);
			peers.get(0).createNetwork();
			
			List<Zone> msg = new LinkedList<Zone>();
			for (int z=0; z<msgSize; z++) {
				msg.add(new Zone((int)(z), (int)(z), (int)(z), (int)(z), null));
			}
			
			List<Key> keys = generateKeys(behaviour, totalPeers);
			
			while (x<totalPeers-1) {
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
			
			for (Key k : keys) {
				peers.get(0).add(k, (Serializable) msg);
			}
			
			Thread.sleep(1000);
			
			long startLookup = System.currentTimeMillis();
			
			List<Object> results = new LinkedList<Object>();
			
			if (behaviour!=TWO_CORNERS_TO_ALL) {
				for (int i=0; i<numMessages/keys.size(); i++) {
					for (Key k : keys) {
						results.add(peers.get((int) 0).lookup(k));;
					}
				}
			} else {
				results.add(((LookupPeer) peers.get(0)).lookupAllKeys(totalPeers, numMessages/2));
				results.add(((LookupPeer) peers.get(peers.size()-1)).lookupAllKeys(totalPeers, numMessages/2));
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

	private static List<Key> generateKeys(int behaviour, int peers) {
		List<Key> keys = new LinkedList<Key>();
		
		if (behaviour==CORNER_TO_CORNER) {
			keys.add(new Key(Zone.MAX_X - 10, Zone.MAX_Y - 10));
		} else if (behaviour==CORNER_TO_THREE_CORNERS) {
			keys.add(new Key(0, Zone.MAX_Y - 10));
			keys.add(new Key(Zone.MAX_X - 10, 0));
			keys.add(new Key(Zone.MAX_X - 10, Zone.MAX_Y - 10));
		} else {
			int cols = (int) Math.sqrt(peers);
			for (int i=0; i<cols; i++) {
				for (int j=0; j<cols; j++) {
					keys.add(new Key((Zone.MAX_X/cols)*i+10, (Zone.MAX_Y/cols)*i+10)); 
				}
			}
		} 
		
		return keys;
		
	}
	
	public static List<Key> generateKeys(int peers) {
		return generateKeys(TWO_CORNERS_TO_ALL, peers);
	}

}
