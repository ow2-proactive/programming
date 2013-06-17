/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.faulttolerance;

import java.io.IOException;
import java.util.Arrays;

import org.objectweb.proactive.core.process.JVMProcessImpl;


public class Main {
    public void launchServer() throws IOException {
        //@snippet-start fault_tolerance_1
        JVMProcessImpl jvmProcessImpl = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        jvmProcessImpl.setClassname(org.objectweb.proactive.core.body.ft.servers.StartFTServer.class
                .getName());

        // optional line: Default arguments
        jvmProcessImpl.setParameters(Arrays.asList("-proto", "cic", "-name", "FTServer", "-port", "1100",
                "-fdperiod", "30"));

        jvmProcessImpl.startProcess();
        //@snippet-end fault_tolerance_1
    }

    public static void main(String[] args) {
        Main server = new Main();

        try {
            server.launchServer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
