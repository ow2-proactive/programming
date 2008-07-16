package org.objectweb.proactive.extra.p2p.daemon;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extra.p2p.service.StartP2PService;


public class PAAgentServiceP2PStarter {

    /**
     * This class is responsible for implementing actions that are started in
     * ProActiveAgent: - registration in P2P network
     * 
     * The created process from this class should be monitored by ProActiveAgent
     * component and restarted automatically on any failures
     */

    public static void main(String args[]) {
        List<String> hosts = new LinkedList<String>(Arrays.asList(args));
        startP2P(hosts);
    }

    // starts P2P service locally initialized with a list of first-contact hosts

    private static void startP2P(List<String> hosts) {
        StartP2PService p2pStarter = new StartP2PService(new Vector<String>(hosts));
        try {
            p2pStarter.start();
        } catch (ProActiveException e) {
            return;
        }
    }
}
