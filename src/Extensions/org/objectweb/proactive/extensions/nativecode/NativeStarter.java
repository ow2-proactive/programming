/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.nativecode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.nativeinterface.Native;
import org.objectweb.proactive.extensions.nativeinterface.ProActiveNative;
import org.objectweb.proactive.extensions.nativeinterface.spmd.NativeSpmd;
import org.objectweb.proactive.extensions.nativeinterfacempi.MpiApplicationFactory;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class NativeStarter {
    private List<GCMApplication> gcma;

    public NativeStarter() {
        this.gcma = new ArrayList<GCMApplication>();
    }

    /**
     * Process deployment descriptor, creating wrapping AO
     * @param descriptor the GCMA descriptor to be deployed
     * @throws ProActiveException
     */
    public void processDescriptor(File descriptor) throws ProActiveException {
        // Access the nodes of the descriptor file
        System.out.println("[NativeStarter] Processing descriptor " + descriptor);
        GCMApplication applicationDescriptor = PAGCMDeployment.loadApplicationDescriptor(descriptor);
        System.out.println("[NativeStarter] Start deployment of " + descriptor);
        applicationDescriptor.startDeployment();

        this.gcma.add(applicationDescriptor);

        Map<String, GCMVirtualNode> vnMap = applicationDescriptor.getVirtualNodes();
        Collection<GCMVirtualNode> vns = vnMap.values();
        System.out.println("[NativeStarter] virtual node count " + vns.size());
        for (GCMVirtualNode virtualNode : vns) {
            System.out.println("[NativeStarter] virtual node wait ready ");
            virtualNode.waitReady();
            System.out.println("[NativeStarter] virtual node wait ready --> OK");
        }

        // All Nodes deployed

        System.out.println("[NativeStarter] All nodes deployed");

        // Start
        ArrayList<NativeSpmd> spmdList = new ArrayList<NativeSpmd>();
        for (GCMVirtualNode virtualNode : vns) {
            List<Node> nodes = virtualNode.getCurrentNodes();
            NativeSpmd nativeSpmd_01 = Native.newNativeSpmd(virtualNode.getName(), nodes,
                    new MpiApplicationFactory());
            spmdList.add(nativeSpmd_01);
        }

        if (!spmdList.isEmpty()) {

            ProActiveNative.deploy(spmdList);

            // active wait
            ProActiveNative.deploymentFinished();
        }
    }

    /**
     * Wait for end signal and wipe environment
     */
    public void waitAndKill() {
        ProActiveNative.waitFinished();
        for (GCMApplication gcma : this.gcma) {
            gcma.kill();
        }

        System.exit(0);
    }

    public static void main(String[] args) throws Exception {

        NativeStarter starter = new NativeStarter();

        for (int i = 0; i < args.length; i++) {
            starter.processDescriptor(new File(args[i]));
        }

        starter.waitAndKill();
    }
}
