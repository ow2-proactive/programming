/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.process.mpi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.DependentProcess;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.JVMNodeProcess;


/**
 * MPI Dependent Process implementation.
 * This implementation works only for ProActive deployment, and not to submit single commands
 * @author The ProActive Team
 * @version 1.0,  2005/11/10
 * @since   ProActive 3.0
 */
public class MPIDependentProcess extends MPIProcess implements DependentProcess {

    /**
     * Create a new MPIProcess
     * Used with XML Descriptors
     */
    public MPIDependentProcess() {
        super();
        this.setTargetProcess(new JVMNodeProcess());
    }

    public MPIDependentProcess(ExternalProcess targetProcess) {
        super(targetProcess);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    @Override
    public String getProcessId() {
        return "mpidp";
    }

    //
    //--------------------------Implements DependentProcess---------------------
    public void setDependencyParameters(Object[] dependencyParameters) {
        if (this.hostsFileName.compareTo(DEFAULT_HOSTSFILENAME_PATH) == 0) {
            // change hostsfile name to avoid doublon
            String firstHostName = ((Node[]) dependencyParameters)[0].getNodeInformation().getName();

            StringBuilder sb = new StringBuilder(this.hostsFileName);
            sb.append("_").append(firstHostName);
            this.hostsFileName = sb.toString();
        }

        buildHostsFile((Node[]) dependencyParameters);
    }

    protected void buildHostsFile(Node[] nodes) {
        logger.info("Generating machinefile... to " + hostsFileName);
        try {
            PrintWriter mf_writer;
            mf_writer = new PrintWriter(new BufferedWriter(new FileWriter(hostsFileName)));
            for (int i = 0; i < nodes.length; i++) {
                mf_writer.println(nodes[i].getVMInformation().getHostName());
            }
            mf_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Machinefile generated");
    }
}
