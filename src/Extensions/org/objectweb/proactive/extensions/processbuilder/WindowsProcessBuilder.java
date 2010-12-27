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
import java.util.List;
import java.util.Map;

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
public final class WindowsProcessBuilder implements OSProcessBuilder {
    /**
     * Windows error codes.
     */
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final int ERROR_LOGON_FAILURE = 1326;

    // the underlying ProcessBuilder to whom all work will be delegated
    // if no specified user
    protected final ProcessBuilder delegatedPB;

    // user - this should be a valid OS user entity (username and maybe a
    // password). The launched process will be run under this user's environment and rights.
    private final OSUser user;

    // descriptor of the core-binding (subset of cores on which the user's
    // process can execute)
    private final CoreBindingDescriptor cores;

    /**
     * Creates a new instance of this class.
     */
    protected WindowsProcessBuilder(final OSUser user, final CoreBindingDescriptor cores, final String paHome) {
        this.delegatedPB = new ProcessBuilder();
        this.user = user;
        this.cores = cores;
    }

    public boolean isCoreBindingSupported() {
        return false;
    }

    public Process start() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        Process p = null;

        if (user() != null || cores() != null) {
            // user or core binding is specified - do the fancy stuff
            p = setupAndStart();

        } else {
            // no extra service needed, just fall through to the delegated pb
            delegatedPB.environment().putAll(environment());
            p = delegatedPB.start();
        }

        return p;
    }

    public List<String> command() {
        return this.delegatedPB.command();
    }

    public OSProcessBuilder command(String... command) {
        this.delegatedPB.command(command);
        return this;
    }

    public OSUser user() {
        return this.user;
    }

    public CoreBindingDescriptor cores() {
        return this.cores;
    }

    public CoreBindingDescriptor getAvaliableCoresDescriptor() {
        return this.cores;
    }

    public File directory() {
        return this.delegatedPB.directory();
    }

    public OSProcessBuilder directory(File directory) {
        this.delegatedPB.directory(directory);
        return this;
    }

    public Map<String, String> environment() {
        return this.delegatedPB.environment();
    }

    public boolean redirectErrorStream() {
        return this.delegatedPB.redirectErrorStream();
    }

    public OSProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        this.delegatedPB.redirectErrorStream(redirectErrorStream);
        return this;
    }

    public boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        if (user.hasPrivateKey()) {
            // remove this when SSH support has been added to the product ;)
            throw new FatalProcessBuilderException("SSH support is not implemented!");
        }
        if (!user.hasPassword()) {
            return false;
        }
        return true;
    }

    /**
     * Create a native representation of a process that will run in background
     * that emans no interaction with the desktop 
     */
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        // Create the windows process from yajsw lib 
        final WindowsProcess p = new WindowsProcess();
        p.setUser(this.user().getUserName());
        p.setPassword(this.user().getPassword());

        // Inherit environment (Currently not work ... must be defined later)
        //p.setEnvironment(super.delegatedPB.environment());

        // This will force CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT;
        p.setVisible(false);

        // Makes the stdin, stdout and stderr available
        p.setPipeStreams(true, false);

        // Inherit the working dir from the original process builder
        final File wdir = this.delegatedPB.directory();
        if (wdir != null) {
            p.setWorkingDir(wdir.getCanonicalPath());
        }

        // Inherit the command from the original process builder
        final StringBuilder commandBuilder = new StringBuilder();
        final List<String> command = this.delegatedPB.command();
        // Merge into a single string to get the length
        for (int i = 0; i < command.size(); i++) {
            commandBuilder.append(command.get(i));
            if (i + 1 < command.size()) {
                commandBuilder.append(' ');
            }
        }
        final String str = commandBuilder.toString();

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

    public static void main(String[] args) {
        WindowsProcessBuilder b = new WindowsProcessBuilder(new OSUser("tutu", "tutu"), null, null);
        b.command("cmd.exe /c notepad.exe");
        try {
            Process p = b.start();
            Thread.sleep(5000);

            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}