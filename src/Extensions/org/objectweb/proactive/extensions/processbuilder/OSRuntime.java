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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that mimics {@link Runtime} but uses {@link OSProcessBuilder} for
 * launching
 * 
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
@PublicAPI
public class OSRuntime {
    final private OSProcessBuilderFactory pbFactory;

    /**
     * Create an {@link OSRuntime} with the default ProActive configuration.
     * 
     * A {@link PAOSProcessBuilderIFactory} is used to create {@link OSProcessBuilder}
     * 
     * @throws ProActiveException If configuration fails 
     */
    public OSRuntime() throws ProActiveException {
        this(new PAOSProcessBuilderFactory());
    }

    /**
     * Create an {@link OSRuntime} with a custom {@link OSProcessBuilderIFactory}
     * 
     * A custom {@link OSProcessBuilderIFactory} can be used to extend or modify the default behavior 
     * @param pbFactory
     */
    public OSRuntime(OSProcessBuilderFactory pbFactory) {
        this.pbFactory = pbFactory;
    }

    protected OSProcessBuilder configure(final OSProcessBuilder ospb, final String[] command,
            final Map<String, String> envp, final File dir) {

        if (command.length == 0)
            throw new IndexOutOfBoundsException();
        else
            ospb.command(command);

        if (dir != null)
            ospb.directory(dir);

        if (envp != null) {
            Map<String, String> envm = ospb.environment();
            envm.putAll(envp);
        }

        return ospb;
    }

    /**
     * Method for executing an operating system command under a specific user
     * and bound to several cores of the CPU.
     * 
     * @param user
     *            User under which to execute. <i>ATTENTION:</i> Null or empty
     *            value will fire an exception - use the 'exec' method which has
     *            no user parameter instead.
     * @param cores
     *            Descriptor of a subset/range of cores to bind to.
     *            <i>ATTENTION:</i> Null value will fire an exception - use the
     *            'exec' method which has no core binding parameter instead.
     * @param command
     *            Array of Strings that represent the operating system command
     *            and its arguments.
     * @param envp
     *            Map containing the environment variables that will be set
     *            before execution. (can be NULL)
     * @param dir
     *            Folder which will be seen as working directory by the process
     *            launched. (NULL means current directory)
     * @return
     * @throws IOException
     *             This exception is thrown in case the executable does not
     *             exist or is not accessible
     * @throws OSUserException
     *             This exception is thrown whenever it is impossible to start
     *             the command under the specified user
     * @throws CoreBindingException
     *             This exception is thrown when it is impossible to bind to the
     *             specific subset of cores
     * @throws FatalProcessBuilderException
     *             This exception signals internal failures of the launching
     *             process - this could be due to missing dependencies of
     *             {@link ProcessBuilder}
     */
    public Process exec(OSUser user, CoreBindingDescriptor cores, String[] command, Map<String, String> envp,
            File dir) throws IOException, OSUserException, CoreBindingException, FatalProcessBuilderException {
        if (user == null) {
            throw new NullPointerException("User name must be specified");
        }
        if (cores == null) {
            throw new NullPointerException("Descriptor for cores is not specified!");
        }

        OSProcessBuilder ospb = this.pbFactory.getBuilder(user, cores);

        return this.configure(ospb, command, envp, dir).start();
    }

    /**
     * Method for executing an operating system command under the current user
     * (to whom the current JVM belongs) bound to several cores of the CPU.
     * 
     * @param cores
     *            Descriptor of a subset/range of cores to bind to.
     *            <i>ATTENTION:</i> Null value will fire an exception - use the
     *            'exec' method which has no core binding parameter instead.
     * @param command
     *            Array of Strings that represent the operating system command
     *            and its arguments.
     * @param envp
     *            Map containing the environment variables that will be set
     *            before execution. (can be NULL)
     * @param dir
     *            Folder which will be seen as working directory by the process
     *            launched. (NULL means current directory)
     * @return
     * @throws IOException
     *             This exception is thrown in case the executable does not
     *             exist or is not accessible
     * @throws CoreBindingException
     *             This exception is thrown when it is impossible to bind to the
     *             specific subset of cores
     * @throws FatalProcessBuilderException
     *             This exception signals internal failures of the launching
     *             process - this could be due to missing dependencies of
     *             {@link ProcessBuilder}
     */
    public Process exec(CoreBindingDescriptor cores, String[] command, Map<String, String> envp, File dir)
            throws IOException, CoreBindingException, FatalProcessBuilderException {
        if (cores == null) {
            throw new NullPointerException("Descriptor for cores is not specified!");
        }

        OSProcessBuilder ospb = this.pbFactory.getBuilder(cores);
        ospb = this.configure(ospb, command, envp, dir);

        Process p = null;
        try {
            p = ospb.start();
        } catch (OSUserException e) {
            // this can not happen - because it was not set;
            e.printStackTrace();
        }
        return p;
    }

    /**
     * Method for executing an operating system command under a given user name.<br>
     * 
     * @param user
     *            User under which to execute <i>ATTENTION:</i> Null value will
     *            fire an exception - use the 'exec' method which has no user
     *            parameter instead.
     * @param command
     *            Array of Strings that represent the operating system command
     *            and its arguments.
     * @param envp
     *            Map containing the environment variables that will be set
     *            before execution. (can be NULL)
     * @param dir
     *            Folder which will be seen as working directory by the process
     *            launched. (NULL means current directory)
     * @return
     * @throws IOException
     *             This exception is thrown in case the executable does not
     *             exist or is not accessible
     * @throws OSUserException
     *             This exception is thrown whenever it is impossible to start
     *             the command under the specified user
     * @throws FatalProcessBuilderException
     *             This exception signals internal failures of the launching
     *             process - this could be due to missing dependencies of
     *             {@link ProcessBuilder}
     */
    public Process exec(OSUser user, String[] command, Map<String, String> envp, File dir)
            throws IOException, OSUserException, FatalProcessBuilderException {
        if (user == null) {
            throw new NullPointerException("User name must be specified");
        }

        OSProcessBuilder ospb = this.pbFactory.getBuilder(user);
        ospb = this.configure(ospb, command, envp, dir);

        Process p = null;
        try {
            p = ospb.start();
        } catch (CoreBindingException e) {
            // this can not happen - because it was not set;
            e.printStackTrace();
        }
        return p;
    }

    /**
     * Method for executing an operating system command.<br>
     * This is a convenience method for <i>exec(null, null, String[], Map,
     * File).</i> i.e. process will run under the current user, with no explicit
     * core binding.
     * 
     * @param command
     *            Array of Strings that represent the operating system command
     *            and its arguments.
     * @param envp
     *            Map containing the environment variables that will be set
     *            before execution. (can be NULL)
     * @param dir
     *            Folder which will be seen as working directory by the process
     *            launched. (NULL means current directory)
     * @return
     * @throws IOException
     *             This exception is thrown in case the executable does not
     *             exist or is not accessible
     * @throws FatalProcessBuilderException
     *             This exception signals internal failures of the launching
     *             process - this could be due to missing dependencies of
     *             {@link ProcessBuilder}
     */
    public Process exec(String[] command, Map<String, String> envp, File dir) throws IOException,
            FatalProcessBuilderException {

        OSProcessBuilder ospb = this.pbFactory.getBuilder();
        ospb = this.configure(ospb, command, envp, dir);

        Process p = null;
        try {
            p = ospb.start();
        } catch (CoreBindingException e) {
            // this can not happen - because it was not set;
            e.printStackTrace();
        } catch (OSUserException e) {
            // this can not happen - because it was not set;
            e.printStackTrace();
        }
        return p;
    }

    /**
     * This method will return an object which implements the
     * {@link CoreBindingDescriptor} interface, and is initialized with the
     * number of cores that can be used for launching the process. By modifying
     * this descriptor, one can limit the core-usage to a subset of all
     * available cores.
     * 
     * @return
     */
    public CoreBindingDescriptor getAvaliableCoresDescriptor() {
        return this.pbFactory.getBuilder().getAvaliableCoresDescriptor();
    }

}
