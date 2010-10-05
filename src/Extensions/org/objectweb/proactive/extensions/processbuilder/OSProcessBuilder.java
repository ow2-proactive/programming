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
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.objectweb.proactive.extensions.processbuilder.stream.LineReader;


/**
 * <p>
 * Abstract class based on {@link java.lang.ProcessBuilder}. <br>
 * Its addition to ProcessBuilder's functionality is that it runs the user's
 * command under a given user, and a subset of the cores/processors available on
 * the machine.<br>
 * Since all operating systems have different process launching mechanisms when
 * it comes to setting users this class has to extended for all each operating
 * system one wants to support.
 * </p>
 * <p>
 * For creation, use the class {@link OSProcessBuilderFactory}.
 * </p>
 * <p>
 * When implementing OS specific versions of this class, one should conform with
 * the script-to-java communication interface outlined in
 * {@link ProcessOutputInterpreter}.<br>
 * In case of a different solution, the <i>start</i> method has to be overridden
 * also.
 * </p>
 * @see OSProcessBuilderFactory
 * @see ProcessBuilder
 * 
 * @author Zsolt Istvan
 * 
 */
public abstract class OSProcessBuilder {

    // the underlying ProcessBuilder to whom all work will be delegated
    protected ProcessBuilder delegatedPB;

    // user - this should be a valid OS user entity (username and maybe a
    // password)
    private OSUser user = null;

    // descriptor of the core-binding (subset of cores on which the user's
    // process can execute)
    private CoreBindingDescriptor cores = null;

    private ProcessOutputInterpreter outputInterpreter = new ProcessOutputInterpreter();

    /**
     * Constructs a process builder with the specified operating system program
     * and arguments. This is a convenience constructor that sets the process
     * builder's command to a string list containing the same strings as the
     * command array, in the same order. It is not checked whether command
     * corresponds to a valid operating system command.
     * 
     * @param command
     *            A string array containing the program and its arguments
     */
    public OSProcessBuilder(String... command) {
        delegatedPB = new ProcessBuilder(command);
    }

    /**
     * Constructs a process builder with the specified operating system program
     * and arguments. This constructor does not make a copy of the command list.
     * Subsequent updates to the list will be reflected in the state of the
     * process builder. It is not checked whether command corresponds to a valid
     * operating system command.
     * 
     * @param command
     *            The list containing the program and its arguments
     * @throws NullPointerException
     *             If the argument is null
     */
    public OSProcessBuilder(List<String> command) throws NullPointerException {
        delegatedPB = new ProcessBuilder(command);
    }

    /**
     * Returns this process builder's operating system program and arguments.
     * The returned list is not a copy. Subsequent updates to the list will be
     * reflected in the state of this process builder.
     * 
     * @return
     */
    public List<String> command() {
        return delegatedPB.command();
    }

    /**
     * Returns this process builder's operating system program and arguments,
     * with added wrapping scripts/programs for user and core binding. This will
     * be used in the internal start method.
     * 
     * @return
     */
    protected abstract String[] wrapCommand();

    /**
     * Sets this process builder's operating system program and arguments. This
     * is a convenience method that sets the command to a string list containing
     * the same strings as the command array, in the same order. It is not
     * checked whether command corresponds to a valid operating system command.
     * 
     * @param command
     *            A string array containing the program and its arguments
     * @return this OSProcessBuilder
     */
    public OSProcessBuilder command(String... command) {
        delegatedPB.command(command);
        return this;
    }

    /**
     * Returns this process builder's associated user. The launched process will
     * be run under this user's environment and rights.
     * 
     * @return
     */
    public OSUser user() {
        return user;
    }

    /**
     * Sets this process builder's associated user. The launched process will be
     * run under this user's environment and rights.
     * 
     * @param userName
     */
    public void user(OSUser user) {
        this.user = user;
    }

    /**
     * Helper method for checking if the launcher can/is allowed to launch a
     * process under a given user.<br>
     * Please note that there can be a difference between being allowed to execute
     * as a user without a password, or being allowed to execute as an other user
     * when providing also the password for that user. 
     * 
     * @param user  User descriptor. It may or may not have a password specified.
     * @return Ability to launch a process using the given user descriptor.
     * @throws FatalProcessBuilderException 
     */
    public abstract Boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException;

    /**
     * Returns this process builder's associated corebinding. The launched
     * process will execute only on these cores.
     * 
     * @return
     */
    public CoreBindingDescriptor cores() {
        return cores;
    }

    /**
     * Sets this process builder's associated corebinding. The launched process
     * will execute only on these cores.
     * 
     * @param cores
     *            descriptor representing the core-binding
     */
    public void cores(CoreBindingDescriptor cores) {
        this.cores = cores;
    }

    /**
     * Helper method for checking the possibility of binding the to-be-created
     * process to a subset of the machine's cores.
     * 
     * @return
     */
    public abstract Boolean isCoreBindingSupported();

    /**
     * This method will return an object which implements the
     * {@link CoreBindingDescriptor} interface, and is initialized with the
     * number of cores that can be used by the OSProcessbuilder.
     * 
     * @return
     */
    /*
     * Implementation advice:
     * 
     * It is desirable that this descriptor abstracts away from the physical index of the avaliable
     * cores, and presents them to the user in a continuous manner, while dealing with the
     * abstract-to-physical core mapping internally. The reason behind this behavior is that cores
     * to which binding would surely fail should not be even shown to the user, as they are not
     * relevant.
     */
    public abstract CoreBindingDescriptor getAvaliableCoresDescriptor();

    /**
     * Returns this process builder's working directory. Subprocesses
     * subsequently started by this object's start() method will use this as
     * their working directory. The returned value may be null -- this means to
     * use the working directory of the current Java process, usually the
     * directory named by the system property user.dir, as the working directory
     * of the child process.
     * 
     * @return This process builder's working directory
     */
    public File directory() {
        return delegatedPB.directory();
    }

    /**
     * Sets this process builder's working directory. Subprocesses subsequently
     * started by this object's start() method will use this as their working
     * directory. The argument may be null -- this means to use the working
     * directory of the current Java process, usually the directory named by the
     * system property user.dir, as the working directory of the child process.
     * 
     * @param directory
     *            The new working directory
     * @return This process builder
     */
    public OSProcessBuilder directory(File directory) {
        delegatedPB.directory(directory);
        return this;
    }

    /**
     * Returns a string map view of this process builder's environment.
     * 
     * Whenever a process builder is created, the environment is initialized to
     * a copy of the current process environment (see {@link System#getenv()}).
     * Subprocesses subsequently started by this object's {@link #start()}
     * method will use this map as their environment.
     * 
     * <p>
     * The returned object may be modified using ordinary {@link java.util.Map
     * Map} operations. These modifications will be visible to subprocesses
     * started via the {@link #start()} method. Two <code>ProcessBuilder</code>
     * instances always contain independent process environments, so changes to
     * the returned map will never be reflected in any other
     * <code>ProcessBuilder</code> instance or the values returned by
     * {@link System#getenv System.getenv}.
     * 
     * <p>
     * If the system does not support environment variables, an empty map is
     * returned.
     * 
     * <p>
     * The returned map does not permit null keys or values. Attempting to
     * insert or query the presence of a null key or value will throw a
     * {@link NullPointerException}. Attempting to query the presence of a key
     * or value which is not of type {@link String} will throw a
     * {@link ClassCastException}.
     * 
     * <p>
     * The behavior of the returned map is system-dependent. A system may not
     * allow modifications to environment variables or may forbid certain
     * variable names or values. For this reason, attempts to modify the map may
     * fail with {@link UnsupportedOperationException} or
     * {@link IllegalArgumentException} if the modification is not permitted by
     * the operating system.
     * 
     * <p>
     * Since the external format of environment variable names and values is
     * system-dependent, there may not be a one-to-one mapping between them and
     * Java's Unicode strings. Nevertheless, the map is implemented in such a
     * way that environment variables which are not modified by Java code will
     * have an unmodified native representation in the subprocess.
     * 
     * <p>
     * The returned map and its collection views may not obey the general
     * contract of the {@link Object#equals} and {@link Object#hashCode}
     * methods.
     * 
     * <p>
     * The returned map is typically case-sensitive on all platforms.
     * 
     * <p>
     * If a security manager exists, its {@link SecurityManager#checkPermission
     * checkPermission} method is called with a
     * <code>{@link RuntimePermission}("getenv.*")</code> permission. This may
     * result in a {@link SecurityException} being thrown.
     * 
     * <p>
     * When passing information to a Java subprocess, <a
     * href=System.html#EnvironmentVSSystemProperties>system properties</a> are
     * generally preferred over environment variables.
     * </p>
     * 
     * @return This process builder's environment
     * 
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkPermission checkPermission}
     *             method doesn't allow access to the process environment
     * 
     * @see Runtime#exec(String[],String[],java.io.File)
     * @see System#getenv()
     */
    public Map<String, String> environment() {
        return delegatedPB.environment();
    }

    protected OSProcessBuilder environment(Map<String, String> env) {
        Set<String> keys = this.environment().keySet();
        for (String s : keys) {
            this.environment().remove(s);
        }
        this.environment().putAll(env);
        return this;
    }

    /**
     * Tells whether this process builder merges standard error and standard
     * output.
     * 
     * <p>
     * If this property is <code>true</code>, then any error output generated by
     * subprocesses subsequently started by this object's {@link #start()}
     * method will be merged with the standard output, so that both can be read
     * using the {@link Process#getInputStream()} method. This makes it easier
     * to correlate error messages with the corresponding output. The initial
     * value is <code>false</code>.
     * </p>
     * 
     * @return This process builder's <code>redirectErrorStream</code> property
     */
    public Boolean redirectErrorStream() {
        return new Boolean(delegatedPB.redirectErrorStream());
    }

    /**
     * Sets this process builder's <code>redirectErrorStream</code> property.
     * 
     * <p>
     * If this property is <code>true</code>, then any error output generated by
     * subprocesses subsequently started by this object's {@link #start()}
     * method will be merged with the standard output, so that both can be read
     * using the {@link Process#getInputStream()} method. This makes it easier
     * to correlate error messages with the corresponding output. The initial
     * value is <code>false</code>.
     * </p>
     * 
     * @param redirectErrorStream
     *            The new property value
     * @return This process builder
     */
    public OSProcessBuilder redirectErrorStream(Boolean redirectErrorStream) {
        delegatedPB.redirectErrorStream(redirectErrorStream);
        return this;
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
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
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

        return p;
    }

    /**
     * Method that is run just before the start() of the delegated process builder.
     * Use this to perform setup that particular (not related to command & working directory).
     * Writing passwords to environment variables should be done here.
     */
    protected void prepareEnvironment() throws FatalProcessBuilderException {
        // Implement if needed
    }

    /**
     * Method that gets called even if the process which we tried to start fails.
     * All that is done here will be executed no-matter-what.
     */
    protected void additionalCleanup() {
        // Implement if needed
    }

    /**
     * Starts a new process using the attributes of this process builder.
     * 
     * <p>
     * The new process will invoke the command and arguments given by
     * {@link #command()}, in a working directory as given by
     * {@link #directory()}, with a process environment as given by
     * {@link #environment()}, and optionally running under the user as given by
     * {@link #user()}, and bound to cores as given by {@link #cores()}.
     * 
     * <p>
     * This method checks that the command is a valid operating system command.
     * Which commands are valid is system-dependent, but at the very least the
     * command must be a non-empty list of non-null strings.
     * 
     * <p>
     * If there is a security manager, its {@link SecurityManager#checkExec
     * checkExec} method is called with the first component of this object's
     * <code>command</code> array as its argument. This may result in a
     * {@link SecurityException} being thrown.
     * 
     * <p>
     * Starting an operating system process is highly system-dependent. Among
     * the many things that can go wrong are:
     * <ul>
     * <li>The operating system program file was not found.
     * <li>Access to the program file was denied.
     * <li>The working directory does not exist.
     * <li>The specified user may not exist
     * <li>Core-binding may tried on non-existent cores - or already bound cores
     * </ul>
     * 
     * <p>
     * Subsequent modifications to this process builder will not affect the
     * returned {@link Process}.
     * </p>
     * 
     * @return A new {@link Process} object for managing the subprocess
     * 
     * @throws NullPointerException
     *             If an element of the command list is null
     * 
     * @throws IndexOutOfBoundsException
     *             If the command is an empty list (has size <code>0</code>)
     * 
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkExec checkExec} method doesn't
     *             allow creation of the subprocess
     * 
     * @throws IOException
     *             If an I/O error occurs
     * 
     * @throws CoreBindingException
     *             If binding to cores was unsuccessful / core binding is not
     *             supported but user specified a warning.
     * 
     * @throws OSUserException
     *             If the process builder is unable to start the process under a
     *             given user.
     * 
     * @throws FatalProcessBuilderException
     *             This exception is raised upon internal failures of the
     *             process builder.
     */
    public final Process start() throws IOException, OSUserException, CoreBindingException,
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
     * This method will take as input a string, which may be an error message coming
     * from the scripts, and creates the corresponding exception. In case the string
     * is not a valid error message, this method will exit without raising exceptions.
     * @param eline Error message
     * @throws IOException
     * @throws FatalProcessBuilderException
     * @throws CoreBindingException
     * @throws OSUserException
     */
    protected void createAndThrowException(String eline) throws IOException, FatalProcessBuilderException,
            CoreBindingException, OSUserException {
        outputInterpreter.createAndThrowException(eline, null);
    }

    /**
     * Class which listens to the launcher scripts' output, and in case of a
     * failure it raises the corresponding exception. <br>
     * For detailed description of the java-script interface please refer to
     * {@link #waitForUserCommandStart(Process)} method's
     * javadoc.
     * 
     * @author Zsolt Istvan
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

                // DEBUG
                // TODO replace with logger maybe
                //System.out.println("[ERROR]  " + eline);
            }
            if (eline != null && eline.trim().startsWith(OK_MESSAGE)) {
                while ((iline = bireader.readLine()) != null && !iline.trim().startsWith(OK_MESSAGE)) {
                    // read this also - so user will not get any output of our
                    // scripts

                    // DEBUG
                    // TODO replace with logger maybe
                    //System.out.println("[OUTPUT] " + iline);
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

}
