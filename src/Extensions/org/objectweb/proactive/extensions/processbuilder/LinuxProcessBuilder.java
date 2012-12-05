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
package org.objectweb.proactive.extensions.processbuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.objectweb.proactive.extensions.processbuilder.stream.LineReader;


/**
 * Class that extends the {@link OSProcessBuilder} for machines running Linux.<br>
 * It relies on scritps for custom launching of the user command.
 * 
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
public class LinuxProcessBuilder implements OSProcessBuilder {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.OSPB);

    private static final String CHECK_SUDO = "check_sudo.sh";
    private static final String LAUNCH_SCRIPT = "launch.sh";
    private static final String ENV_VAR_USER_PASSWORD = "PA_OSPB_USER_PASSWORD";
    private static final String ENV_VAR_USER_KEY_CONTENT = "PA_OSPB_USER_KEY_CONTENT";

    // the underlying ProcessBuilder to whom all work will be delegated
    // if no specified user
    protected final ProcessBuilder delegatedPB;

    // user - this should be a valid OS user entity (username and maybe a
    // password). The launched process will be run under this user's environment and rights.
    private final OSUser user;

    // descriptor of the core-binding (subset of cores on which the user's
    // process can execute)
    private final CoreBindingDescriptor cores;

    // The output interpreter
    private final ProcessOutputInterpreter outputInterpreter;

    // path to scripts
    private final String scriptLocation;

    protected final String token;

    protected LinuxProcessBuilder(final OSUser user, final CoreBindingDescriptor cores, final String paHome) {
        this.scriptLocation = paHome + "/dist/scripts/processbuilder/linux/";
        this.delegatedPB = new ProcessBuilder();
        this.user = user;
        this.cores = cores;
        this.outputInterpreter = new ProcessOutputInterpreter();
        this.token = ProActiveRandom.nextString(15);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#command()
     */
    public List<String> command() {
        return this.delegatedPB.command();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#command(java.lang.String[])
     */
    public OSProcessBuilder command(String... command) {
        this.delegatedPB.command(command);
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#user()
     */
    public OSUser user() {
        return this.user;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#canExecuteAsUser(org.objectweb.proactive.extensions.processbuilder.OSUser)
     */
    public boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException {
        try {
            String[] args = { this.scriptLocation + CHECK_SUDO, user.getUserName(),
                    (user.hasPassword()) ? this.scriptLocation : "" };
            Process p;

            try {
                // running a script that sudo-s into user and runs whoami
                String[] environment = {
                        ENV_VAR_USER_PASSWORD + "=" + ((user.hasPassword()) ? user.getPassword() : ""),
                        ENV_VAR_USER_KEY_CONTENT + "=" +
                            ((user.hasPrivateKey()) ? new String(user.getPrivateKey()) : "") };
                p = Runtime.getRuntime().exec(args, environment);
            } catch (IOException e) {
                additionalCleanup();
                throw new FatalProcessBuilderException("Cannot launch because scripts are missing!", e);
            }

            additionalCleanup();
            InputStream inputstream = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(isr);
            String line;

            while ((line = bufferedreader.readLine()) != null) {
                // if whoami returns the same user name as user(), we are ok to
                // go

                if (line.equals(user.getUserName())) {
                    bufferedreader.close();
                    return true;
                } else {
                    try {
                        createAndThrowException(line);
                    } catch (FatalProcessBuilderException fpbe) {
                        throw fpbe;
                    } catch (Exception e) {
                        throw new FatalProcessBuilderException("Cannot launch!", e);
                    }
                }

            }

            bufferedreader.close();
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method will take as input a string, which may be an error message coming
     * from the scripts, and creates the corresponding exception. In case the string
     * is not a valid error message, this method will exit without raising exceptions.
     * @param eline Error message
     * @throws IOException
     * @throws FatalProcessBuilderException
     * @throws CoreBindingException
     * @throws OSUserException
     */
    private void createAndThrowException(String eline) throws IOException, FatalProcessBuilderException,
            CoreBindingException, OSUserException {
        outputInterpreter.createAndThrowException(eline, null);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#cores()
     */
    public CoreBindingDescriptor cores() {
        return cores;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#isCoreBindingSupported()
     */
    public boolean isCoreBindingSupported() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#getAvaliableCoresDescriptor()
     */
    public CoreBindingDescriptor getAvaliableCoresDescriptor() {
        if (this.cores != null) {
            throw new NotImplementedException("The cores mapping is not yet implemented");
        }
        return this.cores;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#directory()
     */
    public File directory() {
        return delegatedPB.directory();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#directory(java.io.File)
     */
    public OSProcessBuilder directory(File directory) {
        delegatedPB.directory(directory);
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#environment()
     */
    public Map<String, String> environment() {
        if (this.user != null) {
            throw new NotImplementedException(
                "The environment modification of a user process is not implemented");
        }
        return this.delegatedPB.environment();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#redirectErrorStream()
     */
    public boolean redirectErrorStream() {
        return delegatedPB.redirectErrorStream();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#redirectErrorStream(booleann)
     */
    public OSProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
        delegatedPB.redirectErrorStream(redirectErrorStream);
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderI#start()
     */
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
     * Method which will start an OS process based on the command and arguments-
     * works similar to {@link java.lang.ProcessBuilder}.start(...).<br>
     * <p>
     * What happens in addition is: <br>
     * When the process starts, scripts will try to fulfill requests regarding
     * user name and cores. <br>
     * They will use <i>stdout</i> to signal back to the OSProcessBuilder. <br>
     * That is why this method will <b>block</b> until the inner scripts start
     * executing the actual user's command.<br>
     * If this method returns and no exceptions were thrown we can be sure that
     * user-setting and resource-setting has been done with success.<br>
     * </p>
     * For further information: {@link ProcessOutputInterpreter} <br>
     * 
     * @return Process
     * @throws IOException
     * @throws FatalProcessBuilderException
     * @throws CoreBindingException
     * @throws OSUserException
     */
    private Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        // TODO check if there will ever be race conditions on the command swap
        // part

        List<String> original = this.delegatedPB.command();
        File wdir = this.delegatedPB.directory();

        // do this first, as it may throw an exception
        this.prepareEnvironment();

        // wrap user code in pretty wrapper scripts
        this.delegatedPB.command(wrapCommand());
        this.delegatedPB.directory(null);
        Process p;
        try {
            p = this.delegatedPB.start();
            if (logger.isDebugEnabled()) {
                logger.debug("Started token: " + this.token + " command:" + this.delegatedPB.command());
            }
        } catch (IOException e) {
            this.delegatedPB.command(original);
            this.delegatedPB.directory(wdir);
            this.additionalCleanup();
            throw new FatalProcessBuilderException("Cannot launch because scripts are missing!", e);
        }
        // set the command back as we found it :)
        this.delegatedPB.command(original);
        this.delegatedPB.directory(wdir);
        this.additionalCleanup();

        outputInterpreter.waitForUserCommandStart(p);
        // when we are here, we know that user code is executing!

        return new OSLinuxProcess(p, this.user, this.token);
    }

    private void prepareEnvironment() throws FatalProcessBuilderException {
        /*
         * In case we attempt to launch a command under an other user, and we want to use a
         * password, it has to be passed to the scripts through the environment variable {@value
         * #ENV_VAR_USER_PASSWORD}. <br> So before doing the actual launching, we set the env.
         * variable; and after the process has started, we unset the variable, so no one can read it
         * through the inner process builder's environment() method.
         */
        if (user() != null) {
            if (user().hasPrivateKey()) {
                delegatedPB.environment().put(ENV_VAR_USER_KEY_CONTENT, new String(user().getPrivateKey()));
            }

            if (user().hasPassword()) {
                delegatedPB.environment().put(ENV_VAR_USER_PASSWORD, user().getPassword());
            }
        }

    }

    protected void additionalCleanup() {
        delegatedPB.environment().remove(ENV_VAR_USER_PASSWORD);
        delegatedPB.environment().remove(ENV_VAR_USER_KEY_CONTENT);
    }

    protected String[] wrapCommand() {
        String uname = (user() == null) ? "" : user().getUserName();
        String cpart = (cores() == null) ? "" : cores().toString();
        String wpath = (directory() == null) ? "" : directory().getAbsolutePath();
        ArrayList<String> icmd = new ArrayList<String>();
        // under linux the launcher needs:
        // [script folder] [TOKEN] [ABS_PATH_TO_WORKDIR] [USERNAME] [CORES] [COMMAND...
        icmd.add(this.scriptLocation + LAUNCH_SCRIPT);
        icmd.add(this.token);
        icmd.add(this.scriptLocation);
        icmd.add(wpath);
        icmd.add(uname);
        icmd.add(cpart);
        icmd.addAll(command());

        if (user() != null && (user().hasPassword() || user().hasPrivateKey())) {
            for (int i = 1; i < 5; i++)
                if (icmd.get(i).contains(" ")) {
                    icmd.set(i, "'" + icmd.get(i).replace("'", "'\"'\"'") + "'");
                }
        }

        return icmd.toArray(new String[0]);
    }

    /**
     * Class which listens to the launcher scripts' output, and in case of a
     * failure it raises the corresponding exception. <br>
     * For detailed description of the java-script interface please refer to
     * {@link #waitForUserCommandStart(Process)} method's
     * javadoc.
     * 
     * @author The ProActive Team
     * 
     */
    private class ProcessOutputInterpreter {
        final static String ERROR_PREFIX = "_OS_PROCESS_LAUNCH_ERROR_";
        final static String OK_MESSAGE = "_OS_PROCESS_LAUNCH_INIT_FINISHED_";
        final static String ERROR_CAUSE = "CAUSE";

        /**
         * This method will block until the user command starts executing.
         * <p>
         * <b> Java - Native Scripts Interface </b> <br>
         * The launching mechanism is based on an OS-independent and an
         * OS-specific component. This java class is the OS-independent part,
         * while the native scripts (or executables) are the OS-specific
         * components. Those native elements are used in the OS-specific
         * implementations of the OSProcessBuilder, and wrap the user's command.
         * </p>
         * <p>
         * <b>Communication:</b><br>
         * <p>
         * The java part and the native part will communicate on two channels:
         * <i>standard output</i> and <i>standard error</i>.<br>
         * Under communication we understand the fact that the native part is
         * sending messages to the java part.<br>
         * Messages sent on <i>standard error</i>:
         * <ul>
         * <li>Error messages in form: {@value #ERROR_PREFIX}
         * valid_java_exception_class {@value #ERROR_CAUSE} error_message -
         * these will create an exception, which will be thrown immediately</li>
         * <li>{@value #OK_MESSAGE} - when this message is sent, it has to be
         * sent also on standard output, and signals the beginning of the
         * execution of the user's command. (see below)</li>
         * </ul>
         * <br>
         * Messages sent on <i>standard output</i>:
         * <ul>
         * <li>{@value #OK_MESSAGE} - this signals the end of the launching
         * mechanism, and the beginning of the user command's execution.<br>
         * After the receival of this message, on <b>both</b> channels, this
         * method will exit.</li>
         * </ul>
         * Any other output will be <i>ignored both on std.err. and std.out</i>.
         * (unless used for debugging purposes).<br>
         * </p>
         * <p>
         * <b>ATTENTION!</b> In order to enhance the performance of this message
         * reader, there is only a single thread reading the channels. First the
         * standard error will be read until receiving an error message (in this
         * case an exception is thrown, and the launching is abandoned), or the
         * receiving of the OK message. If the OK message was received then the
         * standard output will be read until the receiving of the OK message.
         * This way we ensure that the user will not see any output coming from
         * the native scripts, and the launching will be similar to a java.exec
         * call. <br>
         * <b>Do not</b> output to std.out from the native scripts as it could
         * dead-block the process (if the buffer fills up)! - of course, after
         * the user's command starts executing it is not this method's job to
         * deal with the two channels, thus everything goes back to 'normal'.
         * </p>
         * @param process The process to listen to
         * @throws IOException
         * @throws OSUserException
         * @throws CoreBindingException
         * @throws FatalProcessBuilderException
         */
        private void waitForUserCommandStart(Process process) throws IOException, OSUserException,
                CoreBindingException, FatalProcessBuilderException {

            if (process == null) {
                return;
            }

            InputStream inputstream = process.getInputStream();
            InputStream errorstream = (!redirectErrorStream()) ? process.getErrorStream() : process
                    .getInputStream();
            LineReader bireader = new LineReader(inputstream);
            LineReader bereader = new LineReader(errorstream);
            String eline;
            String iline;

            while ((eline = bereader.readLine()) != null &&
                !(eline.trim().startsWith(OK_MESSAGE) || eline.trim().startsWith(ERROR_PREFIX))) {
                // read it so we do not pollute output of user
                if (logger.isDebugEnabled()) {
                    logger.debug("Token:" + token + " script stderr: " + eline);
                }
            }
            if (eline != null && eline.trim().startsWith(OK_MESSAGE)) {
                while ((iline = bireader.readLine()) != null && !iline.trim().startsWith(OK_MESSAGE)) {
                    // read this also - so user will not get any output of our
                    // scripts

                    if (logger.isDebugEnabled()) {
                        logger.debug("Token:" + token + " script stdout: " + iline);
                    }
                }

                if (iline == null) {
                    throw new FatalProcessBuilderException("Fatal error -  no output from scripts!");
                }

            } else {
                // we've got a problem
                if (eline != null) {
                    outputInterpreter.createAndThrowException(eline, null);
                } else
                    throw new FatalProcessBuilderException("Fatal error -  no output from scripts!");
            }
        }

        /**
         * Create an exception from an error line in the script output.<br>
         * An error looks like:<br> {@value #ERROR_PREFIX}
         * "full_name_of_exception_class_to_be_raised" {@value #ERROR_CAUSE}
         * "error_message" <br>
         * It is possible to add also a list of trace messages.
         * 
         * @param eline The error message which gets interpreted and an exception is created from it
         * @param trace A list of debug messages, which will be added to the exception's description
         * @throws IOException
         * @throws FatalProcessBuilderException
         * @throws CoreBindingException
         * @throws OSUserException
         */
        private void createAndThrowException(String eline, List<String> trace) throws IOException,
                FatalProcessBuilderException, CoreBindingException, OSUserException {

            if (!(eline.indexOf(ERROR_PREFIX) < eline.indexOf(ERROR_CAUSE))) {
                return;
            }

            // let's process the string which is in form:
            // ERROR_PREFIX exception_class ERROR_CAUSE text_description
            String type = eline.substring(eline.indexOf(ERROR_PREFIX) + ERROR_PREFIX.length(), eline
                    .indexOf(ProcessOutputInterpreter.ERROR_CAUSE));
            type = type.replace(" ", "");
            String descr = eline.substring(eline.indexOf(ERROR_CAUSE) + ERROR_CAUSE.length());
            Exception obj;

            try {
                @SuppressWarnings("unchecked")
                Class<Exception> exc = (Class<Exception>) Class.forName(type);
                @SuppressWarnings("rawtypes")
                Constructor constructor = exc.getConstructor(new Class[] { String.class });
                if (trace != null) {
                    descr += "\nTrace: " + trace.toString();
                }
                obj = (Exception) constructor.newInstance(new Object[] { descr });

            } catch (ClassNotFoundException e) {
                // weeeird. Are scripts OK?
                throw new FatalProcessBuilderException("Cannot launch because scripts are corrupted!", e);

            } catch (Exception e) {
                throw new FatalProcessBuilderException("Cannot launch!", e);
            }

            try {
                // now that we have the exception, we are going to 'cast' it
                throw obj;

            } catch (IOException e) {
                // since we want to return an error message as similar to the
                // java.lang.ProcessBuilder's as possible, we will add a nice
                // description before the IOException.

                throw new IOException("Cannot run the program \"" + command().get(0) + "\"" +
                    ((user() == null) ? "" : " as \"" + user().getUserName() + "\"") +
                    ((cores() == null) ? "" : "on cores " + cores().listBoundCores()) +
                    ((directory() == null) ? "" : " (in directory \"" + directory().getPath() + "\")") +
                    ". Cause: " + e.getMessage());

            } catch (CoreBindingException e) {
                throw new CoreBindingException(e.getMessage());

            } catch (OSUserException e) {
                throw new OSUserException(e.getMessage());

            } catch (FatalProcessBuilderException e) {
                throw new FatalProcessBuilderException(e.getMessage());

            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                } else {
                    // mistyped class names in the scripts?
                    throw new FatalProcessBuilderException("Cannot launch!", e);
                }

            }
        }
    }

    static class OSLinuxProcess extends Process {
        final private Process process;
        final private OSUser user;
        final private String token;

        public OSLinuxProcess(Process p, OSUser user, String token) {
            this.process = p;
            this.user = user;
            this.token = token;
        }

        @Override
        public void destroy() {
            try {
                LinuxProcessBuilder lpb = new LinuxProcessBuilder(user, null, ProActiveRuntimeImpl
                        .getProActiveRuntime().getProActiveHome());
                lpb.command("sh", "-c", "ps h -u " + this.user.getUserName() + " -o pid --sid $(pgrep -f " +
                    this.token + ") ; ps h -o pid --sid $(pgrep -f Kill\\ me\\ " + this.token +
                    " | xargs ps h -o sid --pid ) | xargs kill -9 ");
                Process p = lpb.start();
                p.waitFor();

                if (logger.isDebugEnabled()) {
                    BufferedReader br;

                    br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        logger.debug("token: " + this.token + " stdout:" + line);
                    }
                    br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        logger.debug("token: " + this.token + " stderr:" + line);
                    }

                    logger.debug("token: " + this.token + " exitval:" + p.exitValue());
                }
            } catch (Exception e) {
                logger.info("Failed to destroy OS process with token: " + this.token);
            }
        }

        @Override
        public OutputStream getOutputStream() {
            return this.process.getOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return this.process.getInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return this.process.getErrorStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            return this.process.waitFor();
        }

        @Override
        public int exitValue() {
            return this.process.exitValue();
        }
    }
}
