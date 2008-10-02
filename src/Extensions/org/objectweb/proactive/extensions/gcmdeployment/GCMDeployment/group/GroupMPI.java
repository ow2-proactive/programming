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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.objectweb.proactive.extensions.gcmdeployment.ListGenerator;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class GroupMPI extends AbstractGroup {
    public final static String DEFAULT_MPIPATH = "mpirun";
    private String hostList;
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

    public GroupMPI() {
        setCommandPath(DEFAULT_MPIPATH);
        mpiDistributionPath = "";
        hostList = "";
    }

    public GroupMPI(GroupMPI groupMPI) {
        super(groupMPI);
        this.hostList = groupMPI.hostList;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    private File createMachineFile() {
        File machineFile = null;
        try {
            // Create temp machine file.
            machineFile = File.createTempFile("machinefile", ".tmp");

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
        File machineFile = createMachineFile();

        //Append number of processors (based on hostList)
        StringTokenizer hosts = new StringTokenizer(hostList, " ");
        String numberProcessors = Integer.toString(hosts.countTokens());
        command.append(numberProcessors + " ");

        command.append(machineFile.toString());

        commands.add(command.toString());

        return commands;
    }
}