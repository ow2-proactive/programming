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

import java.io.IOException;

import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;


public class P2PRunner implements Runner {

    private P2PInformation info;

    private DaemonJVMProcess workerJVM;

    public P2PRunner(P2PInformation info) {
        this.info = info;
    }

    public void run() {
        if (workerJVM == null) {
            workerJVM = new DaemonJVMProcess(new StandardOutputMessageLogger());
            workerJVM.setClassname("org.objectweb.proactive.extra.p2p.service.StartP2PService");

            workerJVM.setParameters(createParameters());

            try {
                workerJVM.startProcess();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String createParameters() {
        StringBuffer buf = new StringBuffer("-s");
        for (String host : info.getPeerList()) {
            buf.append(" ");
            buf.append(host);
        }
        return buf.toString();
    }

    public void stop() {
        System.out.println("================= STOPPING P2P");
        if (workerJVM != null) {
            workerJVM.stopProcess();
            workerJVM = null;
        }
    }

}
