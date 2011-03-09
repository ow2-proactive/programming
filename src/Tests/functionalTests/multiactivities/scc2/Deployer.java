package functionalTests.multiactivities.scc2;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

/**
 * Create a given number of workers and place them on a given set of nodes, or locally.
 * @author Izso
 *
 */
public class Deployer {
	
	private GraphWorker[] workers;
	
	public GraphWorker[] createAndDeploy(int cnt, String[] hosts, int threads, boolean hardLimited) {
		workers = new GraphWorker[cnt];
		boolean ok = true;
		
		for (int i=0; i<cnt; i++) {
			Object[] params = new Object[3];
			params[0] = "Node"+i+"";
			params[1] = threads;
			params[2] = hardLimited;
			try {
				if (hosts.length > 0) {

					workers[i] = PAActiveObject.newActive(GraphWorker.class,
							params, hosts[i % hosts.length]);
				} else {
					workers[i] = PAActiveObject.newActive(GraphWorker.class,
							params);
				}
				workers[i].init();
			} catch (ActiveObjectCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ok = false;
			} catch (NodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ok = false;
			}
		}
		
		return ok ? workers : null;
	}
	
	public GraphWorker[] createAndDeploy(int cnt, int threads, boolean hardLimited) {
		return createAndDeploy(cnt, new String[0], threads, hardLimited);
	}
	
	public GraphWorker[] getWorkers() {
		return workers;
	}
	
	public void kill(GraphWorker[] w) {
		for (GraphWorker gw : w) {
		    /*
		    System.out.println("\n");
		    System.out.println(gw.getActiveServeCount());
		    System.out.println(gw.getActiveServeTsts());
		    */
			PAActiveObject.terminateActiveObject(gw, true);
		}
	}
	
	public void killAll(){
		kill(workers);
	}
	
	

}
