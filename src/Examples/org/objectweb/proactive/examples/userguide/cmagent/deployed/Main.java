package org.objectweb.proactive.examples.userguide.cmagent.deployed;

import java.io.File;
import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

public class Main {

	private static GCMApplication proActiveDescriptor = null;
	
	private static GCMVirtualNode deploy(String appDeploymentDescriptor) throws ProActiveException
	{
        ProActiveConfiguration.load();
        
            proActiveDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(appDeploymentDescriptor));
            proActiveDescriptor.startDeployment();
            GCMVirtualNode result =  proActiveDescriptor.getVirtualNodes().values().iterator().next();
            result.waitReady();
            return result;
                
	}
	
	
	
	    public static void main(String[] args) {

	        try {
	            GCMVirtualNode agent = deploy(args[0]);
	            

	            List<Node> nodeList = agent.getCurrentNodes();
	            Node firstNode=nodeList.get(0);
	            
	            CMAgentInitialized ao = (CMAgentInitialized) PAActiveObject.newActive(CMAgentInitialized.class
	                    .getName(), new Object[] {}, firstNode);
	           
	            //@snippet-end cma_deploy_object
	            //TODO 7. Get the current state from the active object
	            String currentState = ao.getCurrentState().toString();

	            //TODO 8. Print the state
	            System.out.println(currentState);

	            //TODO 9. Stop the active object
	            PAActiveObject.terminateActiveObject(ao, false);
	            
	            PALifeCycle.exitSuccess();
	        
	        } catch (Exception e) {
	           e.printStackTrace();
	        } finally {
	            if (proActiveDescriptor != null) {
	                proActiveDescriptor.kill();
	            }

	            PALifeCycle.exitSuccess();
	        }

	    }
}
