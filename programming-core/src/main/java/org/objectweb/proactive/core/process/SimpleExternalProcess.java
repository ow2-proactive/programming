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

import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


/**
 * <p>
 * The SimpleExternalProcess class is able to start any command line
 * </p>
 * <p>
 * For instance
 * </p><pre>
 * ..............
 * SimpleExternalProcess p = new SimpleExternalProcess("ls -la");
 * ..............
 * </pre>
 * <p>
 * The previous piece of code will run locally the command "ls -la"
 *
 * @author The ProActive Team
 * @version 1.0,  2002/06/20
 * @since   ProActive 0.9.3
 */
public class SimpleExternalProcess extends AbstractExternalProcess {

    private static final long serialVersionUID = 60L;
    private String targetCommand;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new SimpleExternalProcess
     * @param targetCommand The command to run
     */
    public SimpleExternalProcess(String targetCommand) {
        this(new StandardOutputMessageLogger(), targetCommand);
    }

    /**
     * Creates a new SimpleExternalProcess
     * @param messageLogger The logger that handles input and error stream of this process
     * @param targetCommand The command to run
     */
    public SimpleExternalProcess(RemoteProcessMessageLogger messageLogger, String targetCommand) {
        this(messageLogger, messageLogger, targetCommand);
    }

    /**
     * Creates a new SimpleExternalProcess
     * @param inputMessageLogger The logger that handles input stream of this process
     * @param errorMessageLogger The logger that handles error stream of this process
     * @param targetCommand The command to run
     */
    public SimpleExternalProcess(RemoteProcessMessageLogger inputMessageLogger,
            RemoteProcessMessageLogger errorMessageLogger, String targetCommand) {
        super(inputMessageLogger, errorMessageLogger);
        this.targetCommand = targetCommand;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    public String getProcessId() {
        return "simpleprocess";
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    public int getNodeNumber() {
        return 1;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    public UniversalProcess getFinalProcess() {
        checkStarted();
        return this;
    }

    public static void main(String[] args) {
        try {
            String targetCommand = null;
            if (args.length > 0) {
                targetCommand = args[0];
            } else {
                targetCommand = "ls -las";
            }
            SimpleExternalProcess p = new SimpleExternalProcess(targetCommand);
            p.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String buildCommand() {
        return targetCommand;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
