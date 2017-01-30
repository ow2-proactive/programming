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
