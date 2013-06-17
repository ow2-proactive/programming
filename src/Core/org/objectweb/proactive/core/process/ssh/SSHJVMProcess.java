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
package org.objectweb.proactive.core.process.ssh;

import java.util.List;

import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * <p>
 * The SSHJVMProcess class is able to start any class, of the ProActive library,
 * using ssh protocol. The difference between this class and SSHProcess class is that the target process
 * for this class is automatically a JVMProcess, whereas for the SSHProcess, the target process has to be defined
 * and can be any command and any process.
 * </p><p>
 * For instance:
 * </p><pre>
 * .......
 * SSHProcess ssh = new SSHJVMProcess(new StandardOutputMessageLogger());
 * ssh.setHostname("machine_name");
 * ssh.startProcess();
 * .....
 * </pre>
 * <p>
 * This piece of code creates a new SSHJVMProcess. It allows to log on a remote machine with the ssh protocol and then,
 * on this machine to create a Java Virtual Machine, by launching a ProActive java class.
 * </p>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class SSHJVMProcess extends SSHProcess implements JVMProcess {
    protected JVMProcessImpl jvmProcess;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new SSHJVMProcess
     * Used with XML Descriptor
     */
    public SSHJVMProcess() {
        super();
    }

    /**
     * Creates a new SSHJVMProcess
     * @param messageLogger The logger that handles input and error stream of the target JVMProcess
     */
    public SSHJVMProcess(RemoteProcessMessageLogger messageLogger) {
        this(messageLogger, messageLogger);
    }

    /**
     * Creates a new SSHJVMProcess
     * @param inputMessageLogger The logger that handles input stream of the target JVMProcess
     * @param errorMessageLogger The logger that handles error stream of the target JVMProcess
     */
    public SSHJVMProcess(RemoteProcessMessageLogger inputMessageLogger,
            RemoteProcessMessageLogger errorMessageLogger) {
        super(new JVMProcessImpl(inputMessageLogger, errorMessageLogger));
        jvmProcess = (JVMProcessImpl) targetProcess;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static void main(String[] args) {
        try {
            SSHProcess rsh = new SSHJVMProcess(new StandardOutputMessageLogger());
            rsh.setHostname("solida");
            rsh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- implements JVMProcess -----------------------------------------------
    //

    /**
     * Returns the classpath associated to the target JVMProcess
     * @return String
     */
    public String getClasspath() {
        return jvmProcess.getClasspath();
    }

    /**
     * Sets the classpath for the target JVMProcess
     * @param classpath The value of the classpath environment variable
     */
    public void setClasspath(String classpath) {
        checkStarted();
        jvmProcess.setClasspath(classpath);
    }

    /** Returns the boot classpath of the target JVMProcess
     * @return String the boot classpath of the java command
     */
    public String getBootClasspath() {
        checkStarted();
        return jvmProcess.getBootClasspath();
    }

    /**
     *  Sets the boot classpath  for the target JVMProcess
     * @param bootClasspath The boot classpath of the java command
     */
    public void setBootClasspath(String bootClasspath) {
        checkStarted();
        jvmProcess.setBootClasspath(bootClasspath);
    }

    /**
     * Returns the java path associated the target JVMProcess
     * @return String The path to the java command
     */
    public String getJavaPath() {
        return jvmProcess.getJavaPath();
    }

    /**
     * Sets the java path for the target JVMProcess
     * @param javaPath The value of the path to execute 'java' command
     */
    public void setJavaPath(String javaPath) {
        checkStarted();
        jvmProcess.setJavaPath(javaPath);
    }

    /**
     * Returns the location (path) to the policy file for the target JVMProcess
     * @return String The path to the policy file
     */
    public String getPolicyFile() {
        return jvmProcess.getPolicyFile();
    }

    /**
     * Sets the location of the policy file for the target JVMProcess
     * @param policyFile The value of the path to the policy file
     */
    public void setPolicyFile(String policyFile) {
        checkStarted();
        jvmProcess.setPolicyFile(policyFile);
    }

    public String getLog4jFile() {
        return jvmProcess.getLog4jFile();
    }

    public void setLog4jFile(String log4jFile) {
        checkStarted();
        jvmProcess.setLog4jFile(log4jFile);
    }

    /**
     * Returns the class name that the target JVMProcess is about to start
     * @return String The value of the class that the target JVMProcess is going to start
     */
    public String getClassname() {
        return jvmProcess.getClassname();
    }

    /**
     * Sets the value of the class to start for the target JVMProcess
     * @param classname The name of the class to start
     */
    public void setClassname(String classname) {
        checkStarted();
        jvmProcess.setClassname(classname);
    }

    /**
     * Returns parameters associated to the class that the target JVMProcess is going to start
     * @return String The value of the parameters of the class
     * @deprecated use {@link SSHJVMProcess#getParametersAsList)} instead
     */
    @Deprecated
    @Override
    public String getParameters() {
        return jvmProcess.getParameters();
    }

    /**
     * Returns parameters associated to the class that the target JVMProcess is going to start
     * @return String The value of the parameters of the class
     */
    @Override
    public List<String> getParametersAsList() {
        return jvmProcess.getParametersAsList();
    }

    /**
     * Adds an option to the jvm options
     * @param option The option to add
     */
    @Override
    public void addJvmOption(String option) {
        this.jvmProcess.addJvmOption(option);
    }

    /**
     * Sets the parameters of the class to start with the given value for the target JVMProcess
     * @param parameters Parameters to be given in order to start the class
     * @deprecated use {@link SSHJVMProcess#setParameters(List)} instead
     */
    @Override
    public void setParameters(String parameters) {
        checkStarted();
        jvmProcess.setParameters(parameters);
    }

    /**
     * Sets the parameters of the class to start with the given value for the target JVMProcess
     * @param parameters Paramaters to be given in order to start the class
     */
    @Override
    public void setParameters(List<String> parameters) {
        checkStarted();
        jvmProcess.setParameters(parameters);
    }

    /**
     * Reset to empty value parameters associated to the class that this process
     * is going to start
     */
    public void resetParameters() {
        jvmProcess.resetParameters();
    }

    /**
     * Sets the parameters of the jvm to start with the given parameters for the target JVMProcess
     * @param parameters Parameters to be given in order to start the jvm
     * @deprecated use {@link SSHJVMProcess#setJvmOptions(List)} instead
     */
    @Deprecated
    public void setJvmOptions(String parameters) {
        jvmProcess.setJvmOptions(parameters);
    }

    /**
     * Sets the parameters of the jvm to start with the given parameters for the target JVMProcess
     * @param parameters Parameters to be given in order to start the jvm
     */
    public void setJvmOptions(List<String> parameters) {
        jvmProcess.setJvmOptions(parameters);
    }

    /**
     * @deprecated use {@link SSHJVMProcess#getJvmOptionsAsList()} instead
     */
    @Deprecated
    public String getJvmOptions() {
        return jvmProcess.getJvmOptions();
    }

    public List<String> getJvmOptionsAsList() {
        return jvmProcess.getJvmOptionsAsList();
    }

    public void setOverwrite(boolean overwrite) {
        jvmProcess.setOverwrite(overwrite);
    }

    public void setExtendedJVM(JVMProcessImpl jvmProcess) {
        jvmProcess.setExtendedJVM(jvmProcess);
    }

    public int getNewGroupId() {
        return jvmProcess.getNewGroupId();
    }

    public void setPriority(PriorityLevel priority) {
        jvmProcess.setPriority(priority);
    }

    public void setOperatingSystem(OperatingSystem os) {
        jvmProcess.setOperatingSystem(os);
    }

    public OperatingSystem getOperatingSystem() {
        return jvmProcess.getOperatingSystem();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
}
