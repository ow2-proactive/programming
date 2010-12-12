package functionalTests.multiactivities.graph;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.wsdl.TMessage;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveAnnotationProcessor;

public class GraphTest {
	private static VertexGroup[] vg;
	private static Map<Integer, Integer> vertices;

	// FB
	private static void fB(final Set<Integer> remaining) {
		if (remaining.size() == 0)
			return;

		// pick pivot v
		Integer[] all = remaining.toArray(new Integer[0]);
		Integer pivot = all[0];
		VertexGroup pivotOwner = vg[vertices.get(pivot)];
		
		BooleanWrapper mf = pivotOwner.markForward(pivot);
		BooleanWrapper mb = pivotOwner.markBackward(pivot);
		
		// f := fwd(v)
		Set<Integer> f = new HashSet<Integer>();
		if (mf.getBooleanValue()) {
			f = pivotOwner.getAllForwardMarked();
			f.retainAll(remaining);
		}

		// b := bwd(v)
		Set<Integer> b = new HashSet<Integer>();
		if (mb.getBooleanValue()) {
			b = pivotOwner.getAllBackwardMarked();
			b.retainAll(remaining);
		}

		// report F/\B
		Set<Integer> common = new HashSet<Integer>();
		common.addAll(b);
		common.retainAll(f);
		if (common.size() > 1) {
			System.out.println("Reporting " + common + " (from " + remaining
					+ ") @ " + new Date().getTime());
		}
		
		final Set<Integer> ff = f;
		final Set<Integer> fb = b;
		final Set<Integer> fminb;
		fminb = new HashSet<Integer>();
		fminb.addAll(ff);
		fminb.removeAll(fb);
		
		Thread tBranchA = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// fb(f\b)	
				fB(fminb);				
			}
		});
		tBranchA.start();
		
		final Set<Integer> bminf;
		bminf = new HashSet<Integer>();
		bminf.addAll(fb);
		bminf.removeAll(ff);
		
		Thread tBranchB = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// fb(f\b)	
				fB(bminf);				
			}
		});
		tBranchB.start();

		// fb(v\(f u b))
		final Set<Integer> aux = new HashSet<Integer>();
		aux.addAll(remaining);
		aux.removeAll(fb);
		aux.removeAll(ff);
		fB(aux);
		
		try {
			tBranchB.join();
			tBranchA.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;

	}

	public static void main(String[] args) {
		if (MultiActiveAnnotationProcessor
				.areAnnotationsCorrect(VertexGroup.class)) {
			String[] edges = { 
					"1-2",
					"2-3",
					"3-4",
					"4-1",
					"2-5",
					"5-6",
					"6-7",
					"7-5",
					"8-9",
					"9-8"
					};
			vg = VertexGroupFactory.getVertexGroupsFor(edges, 4);
			vertices = new HashMap<Integer, Integer>();

			for (int i = 0; i < vg.length; i++) {
				for (Integer v : vg[i].getVertices()) {
					vertices.put(v, i);
				}
			}
			System.out.println("Starting @ " + new Date().getTime());
			fB(vertices.keySet());

			System.exit(0);
		}
	}

}
