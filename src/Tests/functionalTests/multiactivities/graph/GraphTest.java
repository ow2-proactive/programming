package functionalTests.multiactivities.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.multiactivity.AnnotationProcessor;

public class GraphTest {
	private static VertexGroup[] vg;
	private static Map<Integer, Integer> vertices;
	private static ExecutorService execs = Executors.newCachedThreadPool();
	private static Date startTime;

	// FB
	private static void fB(final Set<Integer> remaining) {
		if (remaining.size() == 0) {
			return;
		}
			

		// pick pivot v
		Integer[] all = remaining.toArray(new Integer[0]);
		Integer pivot = all[0];
		VertexGroup pivotOwner = vg[vertices.get(pivot)];

		//System.out.println("Going deeper into the rabbit hole..."+remaining.size());

		Set<Integer> f = pivotOwner.markForward(pivot, null);
		Set<Integer> b = pivotOwner.markBackward(pivot, null);

		f.retainAll(remaining);
		b.retainAll(remaining);

		pivotOwner.cleanupAfter(pivot);

		// report F/\B
		Set<Integer> common = new HashSet<Integer>();
		common.addAll(b);
		common.retainAll(f);
		if (common.size() > 1) {
			//if (remaining.size()==1000){
			System.out.println("Reporting " + common.size() + " (from "
					+ remaining.size() + ") \n@ " + new Date().getTime());
			
			//System.out.println("Duration was " + (new Date().getTime() - startTime.getTime()) + " ms");
			
			//}
			for (VertexGroup group : vg) {
				group.addToScc(common);
			}

			// System.out.println("Found "+common.size()+" out of "+remaining.size());
		}

		final Set<Integer> ff = f;
		final Set<Integer> fb = b;
		final Set<Integer> fminb;
		fminb = new HashSet<Integer>();
		fminb.addAll(ff);
		fminb.removeAll(fb);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// fb(f\b)
				//System.out.println("started");
				fB(fminb);
			}
		}).start();

		final Set<Integer> bminf;
		bminf = new HashSet<Integer>();
		bminf.addAll(fb);
		bminf.removeAll(ff);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// fb(f\b)
				//System.out.println("started");
				fB(bminf);
			}
		}).start();

		// fb(v\(f u b))
		final Set<Integer> aux = new HashSet<Integer>();
		aux.addAll(remaining);
		aux.removeAll(fb);
		aux.removeAll(ff);
		new Thread(new Runnable() {

			@Override
			public void run() {
				// fb(f\b)
				//System.out.println("started");
				fB(aux);
			}
		}).start();
		//System.out.println("exited");
		return;

	}

	public static void main(String[] args) throws InterruptedException {
			if (args.length > 1) {
				System.out.println("* parsing input");
				try {
					File f = new File(args[0]);
					FileInputStream fis;
					fis = new FileInputStream(f);
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader br = new BufferedReader(isr);

					List<String> graph = new LinkedList<String>();
					String line = br.readLine();
					while (line != null) {
						graph.add(line);
						line = br.readLine();
					}

					List<String> nodes = new LinkedList<String>();
					if (args.length > 2) {
						File fNodes = new File(args[2]);
						FileInputStream fisNodes;
						fisNodes = new FileInputStream(fNodes);
						InputStreamReader isrNodes = new InputStreamReader(
								fisNodes);
						BufferedReader brNodes = new BufferedReader(isrNodes);

						String node = brNodes.readLine();
						while (node != null) {
							nodes.add(node);
							node = brNodes.readLine();
						}
					}
					Date total = new Date();
					vg = VertexGroupFactory.getVertexGroupsFor(graph.toArray(new String[0]), 
							new Integer(args[1]), args.length > 2, nodes.toArray(new String[0]));
					
					vertices = new HashMap<Integer, Integer>();

					for (int i = 0; i < vg.length; i++) {
						for (Integer v : vg[i].getVertices()) {
							vertices.put(v, i);
						}
					}
					startTime = new Date();
					System.out.println("Starting @ " + startTime.getTime());
					fB(vertices.keySet());
					
					execs.awaitTermination(30, TimeUnit.SECONDS);
					System.out.println("Duration was "
							+ (new Date().getTime() - startTime.getTime()) + " ms");
					System.out.println("Duration (all) was "
							+ (new Date().getTime() - total.getTime()) + " ms");
					
					
					
					System.exit(0);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("Usage -- Parameter: path to graph file");
			}
		/*} else {
			System.out.println("Vertex Group is not annotated correctly: ");
			AnnotationProcessor
					.printInvalidReferences(VertexGroup.class);
		}*/ 
	}

}
