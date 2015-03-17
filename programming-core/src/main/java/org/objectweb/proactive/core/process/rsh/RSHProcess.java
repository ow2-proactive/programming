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
package org.objectweb.proactive.core.process.rsh;

import org.objectweb.proactive.core.process.AbstractExternalProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.SimpleExternalProcess;
import org.objectweb.proactive.core.process.UniversalProcess;


/**
 * <p>
 * The RSHProcess class is able to start any class, of the ProActive library,
 * using rsh protocol.
 * </p><p>
 * For instance:
 * </p><pre>
 * ..........
 * RSHProcess rsh = new RSHProcess(new SimpleExternalProcess("ls -lsa"));
 * rsh.setHostname("hostname.domain.fr");
 * rsh.startProcess();
 * .......... or
 * RSHProcess rsh = new RSHProcess(new JVMProcessImpl(new StandardOutputMessageLogger()));
 * ssh.setHostname("hostname.domain.fr");
 * ssh.startProcess();
 * .....
 * </pre>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class RSHProcess extends AbstractExternalProcessDecorator {

    private static final long serialVersionUID = 61L;
    public final static String DEFAULT_RSHPATH = "/usr/bin/rsh ";
    public final static String DEFAULT_RSH_COPYPROTOCOL = "rcp";

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new RSHProcess
     * Used with XML Descriptors
     */
    public RSHProcess() {
        super();

        FILE_TRANSFER_DEFAULT_PROTOCOL = DEFAULT_RSH_COPYPROTOCOL;
        this.command_path = DEFAULT_RSHPATH;
    }

    /**
     * Creates a new RSHProcess
     * @param targetProcess The target process associated to this process. The target process
     * represents the process that will be launched after logging remote host with rsh protocol
     */
    public RSHProcess(ExternalProcess targetProcess) {
        super(targetProcess);

        FILE_TRANSFER_DEFAULT_PROTOCOL = DEFAULT_RSH_COPYPROTOCOL;
        this.command_path = DEFAULT_RSHPATH;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "rsh_" + targetProcess.getProcessId();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return targetProcess.getNodeNumber();
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return targetProcess.getFinalProcess();
    }

    public static void main(String[] args) {
        try {
            RSHProcess rsh = new RSHProcess(new SimpleExternalProcess("ls -lsa"));
            rsh.setHostname("solida");
            rsh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String internalBuildCommand() {
        return buildRSHCommand() + buildEnvironmentCommand();
    }

    protected String buildRSHCommand() {
        if (IS_WINDOWS_SYSTEM) {
            return buildWindowsRSHCommand();
        } else {
            return buildUnixRSHCommand();
        }
    }

    protected String buildUnixRSHCommand() {
        StringBuilder command = new StringBuilder();
        command.append(DEFAULT_RSHPATH);
        // append username
        if (username != null) {
            command.append(" -l ");
            command.append(username);
        }

        // append host
        command.append(" ");
        command.append(hostname);
        command.append(" ");
        return command.toString();
    }

    protected String buildWindowsRSHCommand() {
        StringBuilder command = new StringBuilder();
        command.append("rsh");
        command.append(" ");
        command.append(hostname);
        // append username
        if (username != null) {
            command.append(" -l ");
            command.append(username);
        }

        // append host
        command.append(" ");
        return command.toString();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
