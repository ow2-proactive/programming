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
package org.objectweb.proactive.core.process.lsf;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.process.ExternalProcess;


/**
 * Custom implementation of LSFProcess for CHINA GRID
 * @author The ProActive Team
 * @version 1.0,  2005/09/20
 * @since   ProActive 3.0
 */
public class CNLSFProcess extends LSFBSubProcess {
    protected String queueName;

    protected String jobname = "grid";

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new LSFBsubProcess
     * Used with XML Descriptors
     */
    public CNLSFProcess() {
        super();
    }

    /**
     * Creates a new LSFBsubProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched with the bsub command
     */
    public CNLSFProcess(ExternalProcess targetProcess) {
        super(targetProcess);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected void internalStartProcess(String commandToExecute) throws java.io.IOException {
        ArrayList<String> al = new ArrayList<String>();

        //we divide the command into tokens
        //it's basically 3 blocks, the script path, the option and the rest
        Pattern p = Pattern.compile("(.*) .*(-c).*'(.*)'");
        Matcher m = p.matcher(commandToExecute);
        if (!m.matches()) {
            System.err.println("Could not match command ");
            System.err.println(commandToExecute);
        }
        for (int i = 1; i <= m.groupCount(); i++) {
            //            System.out.println(m.group(i));
            al.add(m.group(i));
        }
        String[] command = al.toArray(new String[] {});

        try {
            externalProcess = Runtime.getRuntime().exec(command);
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(externalProcess.getInputStream()));
            java.io.BufferedReader err = new java.io.BufferedReader(new java.io.InputStreamReader(externalProcess.getErrorStream()));
            java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(externalProcess.getOutputStream()));
            handleProcess(in, out, err);
        } catch (java.io.IOException e) {
            isFinished = true;
            //throw e;
            e.printStackTrace();
        }
    }

    @Override
    protected String internalBuildCommand() {
        return buildCNBSubCommand();
    }

    protected String buildCNBSubCommand() {
        String executable = scriptLocation.substring(0, scriptLocation.lastIndexOf("/") + 1) + "startExecutable.sh";
        StringBuilder bSubCommand = new StringBuilder();
        bSubCommand.append("/bin/sh -c  'echo ");
        bSubCommand.append(targetProcess.getCommand());
        bSubCommand.append(" > ");
        bSubCommand.append(executable);
        bSubCommand.append(" ; chmod 744 " + executable + " ; ");
        bSubCommand.append(command_path);
        bSubCommand.append(" -n " + processor + " ");
        if (getCompositionType() == GIVE_COMMAND_AS_PARAMETER) {
            bSubCommand.append(scriptLocation + " '");
        }
        return bSubCommand.toString();
    }
}
