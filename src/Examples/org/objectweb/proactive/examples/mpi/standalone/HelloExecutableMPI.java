/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.mpi.standalone;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PALifeCycle; 
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger; 
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

//@snippet-start Hello_MPI_example

public class HelloExecutableMPI implements java.io.Serializable {
	//@snippet-break Hello_MPI_example
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
	//@snippet-resume Hello_MPI_example

    public static void main(String[] args) throws Exception {

        GCMApplication applicationDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(args[0]));
        applicationDescriptor.startDeployment();

		//@snippet-break Hello_MPI_example
        Thread.sleep(10000); //workaround
		//@snippet-resume Hello_MPI_example

        PALifeCycle.exitSuccess();
    }
}
//@snippet-end Hello_MPI_example
