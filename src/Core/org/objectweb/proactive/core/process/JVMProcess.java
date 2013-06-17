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

import java.util.List;

import org.objectweb.proactive.utils.OperatingSystem;


/**
 * <p>
 * The JVMProcess class is able to start localy any class of the ProActive library by
 * creating a Java Virtual Machine.
 * </p>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public interface JVMProcess extends ExternalProcess {

    /**
     * Returns the classpath associated to this process
     * @return String
     */
    public String getClasspath();

    /**
     * Sets the classpath for this process
     * @param classpath The value of the classpath environment variable
     */
    public void setClasspath(String classpath);

    /**
     * Returns the java path associated to this process.
     * @return String The path to the java command
     */
    public String getJavaPath();

    /**
     * Sets the java path for this process
     * @param javaPath The value of the path to execute 'java' command
     */
    public void setJavaPath(String javaPath);

    /** Returns the boot classpath associated to this process
     * @return String the boot classpath of the java command
     */
    public String getBootClasspath();

    /**
     *  Sets the boot classpath associated to this process
     * @param bootClasspath The boot classpath of the java command
     */
    public void setBootClasspath(String bootClasspath);

    /**
     * Returns the location (path) to the policy file
     * @return String The path to the policy file
     */
    public String getPolicyFile();

    /**
     * Sets the location of the policy file
     * @param policyFilePath The value of the path to the policy file
     */
    public void setPolicyFile(String policyFilePath);

    /**
     * Returns the location of the log4j property file.
     * @return String the location of the log4j property file
     */
    public String getLog4jFile();

    /**
     * Sets the location of the log4j property file.
     * @param log4fFilePath The value of the path to the log4j property file
     */
    public void setLog4jFile(String log4fFilePath);

    /**
     * Returns the class name that this process is about to start
     * @return String The value of the class that this process is going to start
     */
    public String getClassname();

    /**
     * Sets the value of the class to start for this process
     * @param classname The name of the class to start
     */
    public void setClassname(String classname);

    /**
     * Reset to empty value parameters associated to the class that this process
     * is going to start
     */
    public void resetParameters();

    /**
     * Returns parameters associated to the class that this process is going to start
     * @return String The value of the parameters of the class
     */
    public String getParameters();

    /**
     * Returns parameters associated to the class that this process is going to start
     * @return String The value of the parameters of the class
     */
    public List<String> getParametersAsList();

    /**
     * Sets the parameters of the class to start with the given value
     * @param parameters Paramaters to be given in order to start the class
     * @deprecated use {@link JVMProcess#setParameters(List)} instead
     */
    @Deprecated
    public void setParameters(String parameters);

    /**
     * Sets the parameters of the class to start with the given value
     * @param parameters Paramaters to be given in order to start the class
     */
    public void setParameters(List<String> parameters);

    /**
     * Adds an option to the jvm options
     * @param option The option to add
     */
    public void addJvmOption(String option);

    /**
     * Sets the options of the jvm to start
     * <p>
     * For instance:
     * </p>
     * <pre>
     * jvmProcess.setJvmOptions("-verbose -Xms300M -Xmx300m");
     * </pre>
     * @param options Options to be given in order to start the jvm
     * @deprecated use {@link JVMProcess#setJvmOptions(List)} instead
     */
    @Deprecated
    public void setJvmOptions(String options);

    /**
     * Sets the options of the jvm to start
     * <p>
     * For instance:
     * </p>
     * <pre>
     * jvmProcess.setJvmOptions(Arrays.asList("-verbose", "-Xms300M","-Xmx300m"));
     * </pre>
     * @param options Options to be given in order to start the jvm
     */
    public void setJvmOptions(List<String> options);

    /**
     * Returns this jvm options
     * @return this jvm options
     * @deprecated use {@link JVMProcess#getJvmOptionsAsList()} instead
     */
    @Deprecated
    public String getJvmOptions();

    /**
     * Returns this jvm options as a list of string
     * @return this jvm options
     */
    public List<String> getJvmOptionsAsList();

    /**
     * Sets the overwrite attribute with the given value
     * @param overwrite
     */
    public void setOverwrite(boolean overwrite);

    /**
     * Allows this JVMProcess to extend another JVMProcessImpl.
     * First implementation of this method. This method must be used carefully.
     * Here is the basic behavior:
     * If attributes are modified on this JVM using set methods, they keep the modified
     * value, otherwise they take the value of the extended jvm. This doesn't apply for
     * classname, and  parameters.
     * Moreover, for the jvm options, the default behavior is to append the options of this
     * jvm to the extended jvm ones, unless the setOverwrite is called previously with true as parameters
     * In that case the jvm options of the extended jvm are ignored.
     * At this point this method is only used in deployment descriptors.
     * @param jvmProcess the extended jvm
     */
    public void setExtendedJVM(JVMProcessImpl jvmProcess);

    public void setPriority(PriorityLevel priority);

    public void setOperatingSystem(OperatingSystem os);

    public OperatingSystem getOperatingSystem();

    public enum PriorityLevel {
        low(19, "low"), normal(0, "normal"), high(-10, "high");
        private int unixValue;
        private String windowsValue;

        PriorityLevel(int unixValue, String windowsValue) {
            this.unixValue = unixValue;
            this.windowsValue = windowsValue;
        }

        public String unixCmd() {
            return "nice -n " + unixValue + " ";
        }

        public String windowsCmd() {
            return "start /" + windowsValue + " ";
        }
    }
}
