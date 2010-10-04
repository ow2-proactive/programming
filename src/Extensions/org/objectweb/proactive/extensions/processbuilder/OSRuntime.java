package org.objectweb.proactive.extensions.processbuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that mimics {@link Runtime} but uses {@link OSProcessBuilder} for
 * launching
 * 
 * @author Zsolt Istvan
 * 
 */
public class OSRuntime {

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
    public static Process exec(OSUser user, CoreBindingDescriptor cores, String[] command,
            Map<String, String> envp, File dir) throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        OSProcessBuilder ospb = OSProcessBuilderFactory.getBuilder();

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

        if (user == null || user.equals(""))
            throw new NullPointerException("User name must be specified!");
        else
            ospb.user(user);

        if (cores == null)
            throw new NullPointerException("Descriptor for cores is not specified!");
        else
            ospb.cores(cores);

        return ospb.start();

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
    public static Process exec(CoreBindingDescriptor cores, String[] command, Map<String, String> envp,
            File dir) throws IOException, OSUserException, CoreBindingException, FatalProcessBuilderException {
        OSProcessBuilder ospb = OSProcessBuilderFactory.getBuilder();

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

        if (cores == null)
            throw new NullPointerException("Descriptor for cores is not specified!");
        else
            ospb.cores(cores);

        return ospb.start();

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
    public static Process exec(OSUser user, String[] command, Map<String, String> envp, File dir)
            throws IOException, OSUserException, FatalProcessBuilderException {

        OSProcessBuilder ospb = OSProcessBuilderFactory.getBuilder();

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

        if (user == null || user.equals(""))
            throw new NullPointerException("User name must be specified!");
        else
            ospb.user(user);

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
    public static Process exec(String[] command, Map<String, String> envp, File dir) throws IOException,
            FatalProcessBuilderException {

        OSProcessBuilder ospb = OSProcessBuilderFactory.getBuilder();

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
    public static CoreBindingDescriptor getAvaliableCoresDescriptor() {
        return OSProcessBuilderFactory.getBuilder().getAvaliableCoresDescriptor();
    }

}
