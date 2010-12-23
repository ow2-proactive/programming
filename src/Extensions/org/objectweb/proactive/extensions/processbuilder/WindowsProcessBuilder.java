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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;

import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Windows.<br>
 * It relies on a parunas.exe native tool that creates a process under a specific user.
 * <p>
 * This builder does not accept OSUser with a private key, only username and password
 * authentication is possible.
 * 
 * @since ProActive 4.4.0
 */
public final class WindowsProcessBuilder implements OSProcessBuilder {
    private static final String PARUNAS = "parunas.exe";

    /** Logon failure: unknown user name or bad password */
    public static final int ERROR_LOGON_FAILURE = 1326;

    // path to tool
    private final String SCRIPTS_LOCATION;

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
        SCRIPTS_LOCATION = paHome + "\\dist\\scripts\\processbuilder\\win\\";
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

    /**
     * The client process is created by the parunas.exe tool
     */
    private Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {

        // The user unwrapped command
        final List<String> userCmdList = this.delegatedPB.command();

        // Prepares the command as in ProcessImpl
        final String[] userCmdArr = internalPrepareCommandList(userCmdList);

        // Inherit working directory
        final File wdir = this.delegatedPB.directory();

        // Check rights, exists and permission under the specified user
        internalCheck(this.user().getUserName(), this.user().getPassword(), userCmdArr[0], wdir);

        // Merge the user command into a single string according to same rules as in ProcessImpl
        final String mergedUserCmd = internalMergeCommand(userCmdArr);

        // Wrap the user command into a call to parunas.exe    	
        List<String> wrappedCommand = new ArrayList<String>();
        wrappedCommand.add(SCRIPTS_LOCATION + PARUNAS);
        wrappedCommand.add("/u:" + this.user().getUserName());
        // Inherit the working dir from the original process builder
        if (wdir != null) {
            try {
                wrappedCommand.add("/w:" + wdir.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // The wrapped command is an argument of the parunas tool
        wrappedCommand.add(mergedUserCmd);

        // wrap user code in pretty wrapper scripts
        this.delegatedPB.command(wrappedCommand);

        // The parunas tool itself is executed in the same directory as the current process
        this.delegatedPB.directory(null);

        Process p;
        try {
            p = this.delegatedPB.start();
        } catch (IOException e) {
            throw new FatalProcessBuilderException("Cannot launch because parunas.exe tool is missing!", e);
        } finally {
            // set the command back as we found it :)
            this.delegatedPB.command(userCmdList);
            this.delegatedPB.directory(wdir);
        }

        // Feed the stdin with the password                      
        final OutputStream stdin = p.getOutputStream();
        stdin.write((this.user().getPassword() + "\n").getBytes());
        stdin.flush();

        return p;
    }

    private String[] internalPrepareCommandList(final List<String> commandList) {
        final int nbCommands = commandList.size();

        String[] cmd;

        // If no commands throw an exception
        if (nbCommands == 0) {
            throw new IllegalArgumentException("Empty command");
            // If only one command parse and fill an array of commands
        } else if (nbCommands == 1) {
            // Default tokens are " \t\n\r\f"
            StringTokenizer st = new StringTokenizer(commandList.get(0));
            cmd = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                cmd[i] = st.nextToken();
            }
            // If more than one command just fill the cmd array as it is
        } else {
            cmd = commandList.toArray(new String[nbCommands]);
        }

        // Check for null elements
        for (String arg : cmd) {
            if (arg == null) {
                throw new NullPointerException();
            }
        }

        // Win32 CreateProcess requires cmd[0] to be normalized
        cmd[0] = new File(cmd[0]).getPath();

        return cmd;
    }

    // This method merges a command into a single string
    private String internalMergeCommand(final String[] cmd) {
        StringBuilder cmdbuf = new StringBuilder(80);
        for (int i = 0; i < cmd.length; i++) {
            if (i > 0) {
                cmdbuf.append(' ');
            }
            String s = cmd[i];
            cmdbuf.append(s);
        }
        return cmdbuf.toString();
    }

    // Performs various checks: 
    // - username/password are correct
    // - program filepath exists and can be executed under the specific user
    // - working dir exists 
    private void internalCheck(final String username, final String password, final String filepath,
            final File wdir) throws OSUserException, IOException, FatalProcessBuilderException {

        final HANDLEByReference phUser = new HANDLEByReference();

        try {
            if (!Advapi32.INSTANCE.LogonUser(username, ".", // The domain is the local machine
                    password, WinBase.LOGON32_LOGON_INTERACTIVE, WinBase.LOGON32_PROVIDER_DEFAULT, phUser)) {
                int lastError = Kernel32.INSTANCE.GetLastError();
                // Check last error
                if (lastError == ERROR_LOGON_FAILURE) {
                    throw new OSUserException("Unknown user name or bad password! errno=" + lastError);
                }
                throw new FatalProcessBuilderException("Unable to logon as user! errno=" + lastError);
            }

            // If the file specified is not absolute we cannot check
            // if it exists or if its readable
            final File fileToCheck = new File(filepath);
            if (!fileToCheck.isAbsolute()) {
                return;
            }

            try {
                if (!Advapi32.INSTANCE.ImpersonateLoggedOnUser(phUser.getValue())) {
                    int lastError = Kernel32.INSTANCE.GetLastError();
                    throw new FatalProcessBuilderException("Unable to impersonate as user! errno=" +
                        lastError);
                }

                // Check if file exists
                if (!fileToCheck.exists()) {
                    throw new IOException("Cannot run program \"" + fileToCheck +
                        "\": The system cannot find the file specified");
                }

                // Check if file can be r
                if (!fileToCheck.canRead()) {
                    throw new IOException("Cannot run program \"" + fileToCheck + "\": Access denied");
                }

                // Check if working directory exists
                if (wdir != null && wdir.exists()) {
                    throw new IOException("Cannot run program \"" + fileToCheck +
                        "\": The working directory " + wdir + " does not exists");
                }
            } finally {
                // Revert to self
                if (!Advapi32.INSTANCE.RevertToSelf()) {
                    int lastError = Kernel32.INSTANCE.GetLastError();
                    throw new FatalProcessBuilderException("Unable to revert impersonated user! errno=" +
                        lastError);
                }
            }
        } finally {
            // Close handle
            Kernel32.INSTANCE.CloseHandle(phUser.getValue());
        }
    }

    public List<String> command() {
        return this.delegatedPB.command();
    }

    public OSProcessBuilder command(String... command) {
        this.command(command);
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
}