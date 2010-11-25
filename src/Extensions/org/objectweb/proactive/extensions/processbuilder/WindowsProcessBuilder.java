/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.processbuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.rzo.yajsw.os.ms.win.w32.WindowsProcess;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Windows.<br>
 * It relies on yajsw API (see http://yajsw.sourceforge.net) that exposes the Windows native
 * API calls to create process under a specific user.
 * <p>
 * This builder does not accept OSUser with a private key, only username and password
 * authentication is possible.
 * 
 * @since ProActive 4.4.0
 */
public final class WindowsProcessBuilder extends OSProcessBuilder {

    /**
     * Windows error codes.
     */
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final int ERROR_LOGON_FAILURE = 1326;

    /**
     * Creates a new instance of this class.
     */
    public WindowsProcessBuilder(final OSUser user, final CoreBindingDescriptor cores) {
        super(user, cores);
    }

    @Override
    public Boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        if (user.hasPrivateKey()) {
            // remove this when SSH support has been added to the product ;)
            throw new FatalProcessBuilderException("SSH support is not implemented!");
        }
        if (!user.hasPassword()) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean isCoreBindingSupported() {
        return false;
    }

    @Override
    protected String[] wrapCommand() {
        return new String[0];
    }

    @Override
    protected void prepareEnvironment() throws FatalProcessBuilderException {
    }

    @Override
    protected void additionalCleanup() {
    }

    /**
     * If the command length is longer than 1024 chars the process is wrapped  
     * in a CMD shell and the command is passed to the shell through stdin 
     */
    @Override
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        // Create the windows process from yajsw lib 
        final WindowsProcess p = new WindowsProcess();
        p.setUser(super.user().getUserName());
        p.setPassword(super.user().getPassword());

        // Inherit environment (Currently not work ... must be defined later)
        //p.setEnvironment(super.delegatedPB.environment());

        // This will force CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT;
        p.setVisible(false);

        // Makes the stdin, stdout and stderr available
        p.setPipeStreams(true, false);

        // Inherit the working dir from the original process builder
        final File wdir = super.delegatedPB.directory();
        if (wdir != null) {
            p.setWorkingDir(wdir.getCanonicalPath());
        }

        // Inherit the command from the original process builder
        final StringBuilder commandBuilder = new StringBuilder();
        final List<String> command = super.delegatedPB.command();
        // Merge into a single string to get the length
        for (int i = 0; i < command.size(); i++) {
            commandBuilder.append(command.get(i));
            if (i + 1 < command.size()) {
                commandBuilder.append(' ');
            }
        }
        final String str = commandBuilder.toString();
        if (str.length() > 1024) {
            // Invoke a CMD shell and feed stdin with the command that will be stored 
            // in an env variable then launch the command	
            p.setCommand("cmd.exe /V:ON /c \"SET /p OSPB_COMMAND=&CALL !OSPB_COMMAND!\"");
            if (p.start()) {
                // Write the command into process stdin      		 	
                final PrintWriter out = new PrintWriter(p.getOutputStream(), true);
                out.println(str);
                out.close();
                // The CMD process has launched the command and we have no way to    
                // get the error code of the launched command        		
            } else {
                final int err = WindowsProcess.getLastError();
                if (err == ERROR_FILE_NOT_FOUND) {
                    // This can happen if cmd.exe is not in the path
                    throw new IOException("Unable to find cmd.exe make sure it is in the path errno=" + err);
                }
                // This can happen if cmd.exe is not in the path
                throw new FatalProcessBuilderException("Unable to run command wrapper errno=" +
                    WindowsProcess.getLastError());
            }
        } else {
            p.setCommand(str);
            if (!p.start()) {
                // Get the last error and depending on the error code
                // throw the correct exception            
                final int err = WindowsProcess.getLastError();
                final String localizedMessage = WindowsProcess.formatMessageFromLastErrorCode(err);
                final String message = localizedMessage + " error=" + err;
                switch (err) {
                    case ERROR_FILE_NOT_FOUND:
                        throw new IOException(message);
                    case ERROR_LOGON_FAILURE:
                        throw new OSUserException(message);
                    default:
                        throw new FatalProcessBuilderException(message);
                }
            }
        }
        return new ProcessWrapper(p);
    }

    /**
     * Wraps a WindowsProcess and exposes it as a java.lang.Process 
     */
    private final class ProcessWrapper extends Process {
        private final WindowsProcess wp;

        public ProcessWrapper(final WindowsProcess wp) {
            this.wp = wp;
        }

        @Override
        public void destroy() {
            this.wp.destroy();
        }

        @Override
        public int exitValue() {
            return this.wp.getExitCode();
        }

        @Override
        public InputStream getErrorStream() {
            return this.wp.getErrorStream();
        }

        @Override
        public InputStream getInputStream() {
            return this.wp.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return this.wp.getOutputStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            return (this.wp.waitFor() ? 0 : -1);
        }
    }
}