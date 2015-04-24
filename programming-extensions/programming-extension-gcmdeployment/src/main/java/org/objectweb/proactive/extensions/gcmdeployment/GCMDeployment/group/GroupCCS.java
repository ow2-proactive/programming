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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderProActive;


public class GroupCCS extends AbstractGroup {

    private static final long serialVersionUID = 62L;

    private String runTime = null;
    private int cpus = 0;
    private String stdout = null;
    private String stderr = null;
    private String preCommand = null;

    private PathElement scriptLocation = new PathElement("dist\\scripts\\gcmdeployment\\ccs.vbs",
        PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {

        StringBuilder command = new StringBuilder();
        command.append("cscript");
        command.append(" ");
        command.append(scriptLocation.getFullPath(hostInfo, commandBuilder));

        command.append(" ");
        command.append("/tasks:" + cpus);
        command.append(" ");

        String classpath = ((CommandBuilderProActive) commandBuilder).getClasspath(hostInfo);
        command.append(" ");
        command.append("/classpath:\"" + classpath + "\"");
        command.append(" ");

        String cbCommand = ((CommandBuilderProActive) commandBuilder).buildCommand(hostInfo, gcma, false);
        //cbCommand = Helpers.escapeWindowsCommand(cbCommand);
        cbCommand += " -c 1 ";

        command.append(" ");
        if (preCommand != null && !"".equals(preCommand)) {
            command.append("/application:\"" + preCommand + " & " + cbCommand + "\"");
        } else {
            command.append("/application:\"" + cbCommand + "\"");
        }
        command.append(" ");

        if (getStdout() != null) {
            command.append("/stdout:" + getStdout());
            command.append(" ");
        }

        if (getStderr() != null) {
            command.append("/stderr:" + getStderr());
            command.append(" ");
        }

        if (getRunTime() != null) {
            command.append("/runtime:" + getRunTime());
            command.append(" ");
        }

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        return null;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setPreCommand(String preCmd) {
        this.preCommand = preCmd;
    }
}
