package org.objectweb.proactive.core.util.winagent;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;


public class PAAgentServiceRMIStarter {

    /**
     * This class is responsible for implementing actions that are started in
     * ProActiveAgent: - advertisement in RMI Registry
     * 
     * The created process from this class should be monitored by ProActiveAgent
     * component and restarted automatically on any failures
     */

    private static final String PAAGENT_NODE_NAME = "PA-AGENT_NODE";

    // command dispatch

    public static void main(String args[]) {
        String nodeName = PAAGENT_NODE_NAME;
        if (args.length > 0)
            nodeName = args[0];
        doAdvert(nodeName);
    }

    // registers local node in RMI registry

    private static void doAdvert(String nodeName) {
        try {
            // TODO: localhost?
            Node n = NodeFactory.createNode("//localhost/" + nodeName);
            System.out.println("The node was registered at " + n.getNodeInformation().getURL());
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            return;
        }

    }

}
