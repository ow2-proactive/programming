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
package org.objectweb.proactive.core.process.rocks;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;


public class ClusterForkProcess extends AbstractExternalProcessDecorator {

    private static final long serialVersionUID = 61L;
    final protected String DEFAULT_COMMAND = "cluster-fork";

    public ClusterForkProcess() {
        super();
        command_path = DEFAULT_COMMAND;
        hostname = null;
    }

    public ClusterForkProcess(ExternalProcess _targetProcess) {
        super(_targetProcess);
        command_path = DEFAULT_COMMAND;
        hostname = null;
    }

    /* ==== AbstractExternalProcessDecorator ==== */
    @Override
    protected String internalBuildCommand() {
        if (logger.isDebugEnabled()) {
            logger.debug("cluster-fork command is " + command_path);
        }
        return command_path + " --bg ";
    }

    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess;
    }

    public int getNodeNumber() {
        return UNKNOWN_NODE_NUMBER;
    }

    public String getProcessId() {
        return "clusterfork_" + targetProcess.getProcessId();
    }
}
