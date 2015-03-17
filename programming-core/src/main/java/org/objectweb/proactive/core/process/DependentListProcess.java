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
package org.objectweb.proactive.core.process;

import java.io.IOException;


/**
 * This class contains a list of processes that have a dependency
 * with its predecessor.
 * @author The ProActive Team
 * @version 1.0, 01 Dec 2005
 * @since ProActive 3.0
 *
 */
public class DependentListProcess extends AbstractSequentialListProcessDecorator {

    private static final long serialVersionUID = 61L;
    public DependentListProcess() {
        super();
    }

    /**
     * Add a process to the processes queue - first process is not a dependent process unlike
     * the others
     * @param process
     */
    @Override
    public void addProcessToList(ExternalProcess process) {
        if (processes.size() == 0) {
            processes.add(process);
        }
        // process has to be an instance of dependentProcess
        // implements DependentProcess
        else if (process instanceof DependentProcess) {
            this.processes.add(process);
        } else {
            throw new ClassCastException(" process must be a dependent process !");
        }
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    @Override
    public String getProcessId() {
        return "dps";
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    public boolean isDependent() {
        return true;
    }

    public boolean isHierarchical() {
        return false;
    }

    public String getHostname() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setHostname(String hostname) {
        // TODO Auto-generated method stub
    }

    @Override
    protected ExternalProcess createProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    public void startProcess() throws IOException {
        // TODO Auto-generated method stub
    }
}
