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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.processbuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


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
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
@PublicAPI
public interface OSProcessBuilder {
    /**
     * Returns this process builder's operating system program and arguments.
     * The returned list is not a copy. Subsequent updates to the list will be
     * reflected in the state of this process builder.
     * 
     * @return command as a list of strings
     */
    public List<String> command();

    /**
     * Sets this process builder's operating system program and arguments. This
     * is a convenience method that sets the command to a string list containing
     * the same strings as the command array, in the same order. It is not
     * checked whether command corresponds to a valid operating system command.
     * 
     * @param command
     *            A string array containing the program and its arguments
     * @return this OSProcessBuilderI
     */
    public OSProcessBuilder command(String... command);

    /**
     * Returns this process builder's associated user. The launched process will
     * be run under this user's environment and rights.
     * 
     * @return the user of this process builder or null is the process will be run under 
     *          the current user account.
     */
    public OSUser user();

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
    public boolean canExecuteAsUser(OSUser user) throws FatalProcessBuilderException;

    /**
     * Returns this process builder's associated core binding. The launched
     * process will execute only on these cores.
     * 
     * @return the description of core binding
     */
    public CoreBindingDescriptor cores();

    /**
     * Helper method for checking the possibility of binding the to-be-created
     * process to a subset of the machine's cores.
     * 
     * @return true if the binding is supported and false otherwise
     */
    public boolean isCoreBindingSupported();

    /**
     * This method will return an object which implements the
     * {@link CoreBindingDescriptor} interface, and is initialized with the
     * number of cores that can be used by the OSProcessbuilder.
     * 
     * @return the cores mapping
     */
    public CoreBindingDescriptor getAvaliableCoresDescriptor();

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
    public File directory();

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
    public OSProcessBuilder directory(File directory);

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
     * fail with {@link NotImplementedException} or
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
    public Map<String, String> environment();

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
    public boolean redirectErrorStream();

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
    public OSProcessBuilder redirectErrorStream(boolean redirectErrorStream);

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
    public Process start() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException;
}
