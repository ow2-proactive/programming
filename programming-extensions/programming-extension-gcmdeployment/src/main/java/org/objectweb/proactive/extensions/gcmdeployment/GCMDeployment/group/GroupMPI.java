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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.proactive.extensions.gcmdeployment.ListGenerator;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class GroupMPI extends AbstractGroup {

    private static final long serialVersionUID = 60L;
    public final static String DEFAULT_MPIPATH = "mpirun";
    private String hostList;
    private String machineFile = null;
    private String execDir;
    private String commandOptions;
    private String mpiDistributionPath;

    public String getCommandOption() {
        return commandOptions;
    }

    public void setCommandOption(String commandOption) {
        this.commandOptions = commandOption;
    }

    public String getMpiDistributionPath() {
        return this.mpiDistributionPath;
    }

    public void setMpiDistributionPath(String mpiDistributionPath) {
        this.mpiDistributionPath = mpiDistributionPath;
    }

    public String getMachineFile() {
        return machineFile;
    }

    public void setMachineFile(String machineFile) {
        this.machineFile = machineFile;
    }

    public void setExecDir(String execDir) {
        this.execDir = execDir;
    }

    public String getExecDir() {
        return execDir;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    public GroupMPI() {
        setCommandPath(DEFAULT_MPIPATH);
        mpiDistributionPath = "";
        hostList = "";
    }

    public GroupMPI(GroupMPI groupMPI) {
        super(groupMPI);
        this.hostList = groupMPI.hostList;
    }

    private File createMachineFile() {
        File machineFile = null;
        try {
            // Create temp machine file.

            machineFile = File.createTempFile("machinefile", ".tmp");
            //        	machineFile = new File(System.getProperty("user.home").concat("/machinefile"));
            // Delete temp file when program exits.
            machineFile.deleteOnExit();

            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(machineFile));

            for (String hostname : ListGenerator.generateNames(hostList)) {
                out.write(hostname);
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return machineFile;
    }

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        List<String> commands = new ArrayList<String>();
        StringBuilder command = new StringBuilder(getCommandPath() + " ");
        command.append(mpiDistributionPath + " ");

        // Create machine file using hostList defined in mpiGroup XML descriptor file
        File machineFile;
        if (this.machineFile == null) {
            machineFile = createMachineFile();
        } else {
            machineFile = new File(this.getMachineFile());
        }

        //Append number of processors (based on hostList)
        StringTokenizer hosts = new StringTokenizer(hostList, " ");
        String numberProcessors = Integer.toString(hosts.countTokens());
        command.append(numberProcessors + " ");

        command.append(machineFile.toString());

        if (this.execDir == null) {
            command.append(" \".\"");
        } else {
            command.append(" \"" + this.execDir + "\"");
        }

        commands.add(command.toString());

        return commands;
    }

}
