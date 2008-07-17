/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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
