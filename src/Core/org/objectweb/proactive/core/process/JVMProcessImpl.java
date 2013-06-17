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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.HostsInfos;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * <p>
 * The JVMProcess class is able to start locally any class of the ProActive library by
 * creating a Java Virtual Machine.
 * </p><p>
 * For instance:
 * </p>
 * <pre>
 * .............
 * JVMProcessImpl process = new JVMProcessImpl(new StandardOutputMessageLogger());
 * process.setClassname("org.objectweb.proactive.core.node.StartNode");
 * process.setParameters("nodeName");
 * process.startProcess();
 * .............
 * </pre>
 * <p>
 * This piece of code launches the ProActive java class org.objectweb.proactive.core.node.StartNode
 * with nodeName as parameter.
 * </p>
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 */
public class JVMProcessImpl extends AbstractExternalProcess implements JVMProcess, Serializable {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_PROCESS);

    //private final static String POLICY_FILE = "proactive.java.policy";
    private final static String POLICY_OPTION = "-Djava.security.policy=";
    private final static String LOG4J_OPTION = "-Dlog4j.configuration=file:";

    //private final static String LOG4J_FILE = "proactive-log4j";
    public final static String DEFAULT_CLASSPATH = convertClasspathToAbsolutePath(System
            .getProperty("java.class.path"));
    public final static String DEFAULT_JAVAPATH = System.getProperty("java.home") + File.separator + "bin" +
        File.separator + "java";
    public static String DEFAULT_POLICY_FILE = CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue();
    public static String DEFAULT_LOG4J_FILE = CentralPAPropertyRepository.LOG4J.getValue();

    static {
        if (DEFAULT_POLICY_FILE != null) {
            DEFAULT_POLICY_FILE = getAbsolutePath(DEFAULT_POLICY_FILE);
        }
        if (DEFAULT_LOG4J_FILE != null) {
            DEFAULT_LOG4J_FILE = getAbsolutePath(DEFAULT_LOG4J_FILE);
        }
    }

    public final static String DEFAULT_CLASSNAME = org.objectweb.proactive.core.node.StartNode.class
            .getName();
    public final static String DEFAULT_JVMPARAMETERS = "";

    // How many paths leading to a JVMProcessImpl have been encountered
    private static int groupID = 0;

    protected String classpath = DEFAULT_CLASSPATH;
    protected String bootClasspath;
    protected String javaPath = DEFAULT_JAVAPATH;
    protected String policyFile = DEFAULT_POLICY_FILE;
    protected String log4jFile = DEFAULT_LOG4J_FILE;
    protected String classname = DEFAULT_CLASSNAME;

    /** The jvm options to add to the command */
    protected final HashSet<String> jvmOptions;

    /** 
     * This array will be used to know which options have been modified in case 
     * this process extends another jvmprocess in the descriptor
     */
    protected final ArrayList<String> modifiedOptions;

    /** The main class parameters to add at the end of the command */
    protected final ArrayList<String> parameters;

    /**
     * This attributes is used when this jvm extends another one.
     * If set to yes, the jvm options of the extended jvm will be ignored.
     * If false, jvm options of this jvm will be appended to extended jvm ones. Default is false.
     */
    protected boolean overwrite;
    protected PriorityLevel priority;
    protected OperatingSystem os;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new JVMProcess with empty message loggers.
     * Used with XML Descriptor.
     */
    public JVMProcessImpl() {
        this(null);
    }

    /**
     * Creates a new JVMProcess
     * @param messageLogger The logger that handles input and error stream of this process
     */
    public JVMProcessImpl(RemoteProcessMessageLogger messageLogger) {
        this(messageLogger, messageLogger);
    }

    /**
     * Creates a new JVMProcess
     * @param inputMessageLogger The logger that handles input stream of this process
     * @param errorMessageLogger The logger that handles error stream of this process
     */
    public JVMProcessImpl(RemoteProcessMessageLogger inputMessageLogger,
            RemoteProcessMessageLogger errorMessageLogger) {
        super(inputMessageLogger, errorMessageLogger);
        this.jvmOptions = new HashSet<String>();
        this.modifiedOptions = new ArrayList<String>();
        this.parameters = new ArrayList<String>();
        this.priority = PriorityLevel.normal;
        this.os = OperatingSystem.getOperatingSystem();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static void main(String[] args) {
        try {
            JVMProcessImpl rsh = new JVMProcessImpl(new StandardOutputMessageLogger());
            rsh.setClassname(org.objectweb.proactive.core.node.StartNode.class.getName());
            rsh.setParameters(Arrays.asList(args));
            rsh.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // -- implements JVMProcess -----------------------------------------------
    //
    @Override
    public String getClasspath() {
        return classpath;
    }

    @Override
    public void setClasspath(String classpath) {
        checkStarted();
        modifiedOptions.add("classpath");
        this.classpath = classpath;
    }

    @Override
    public String getJavaPath() {
        return javaPath;
    }

    @Override
    public void setJavaPath(String javaPath) {
        checkStarted();
        if (javaPath == null) {
            throw new IllegalArgumentException("Java path canot be null");
        }
        modifiedOptions.add("javaPath");
        this.javaPath = javaPath;
    }

    @Override
    public String getBootClasspath() {
        return bootClasspath;
    }

    @Override
    public void setBootClasspath(String bootClasspath) {
        checkStarted();
        modifiedOptions.add("bootClasspath");
        this.bootClasspath = bootClasspath;
    }

    @Override
    public String getPolicyFile() {
        return policyFile;
    }

    @Override
    public void setPolicyFile(String policyFile) {
        checkStarted();
        modifiedOptions.add("policyFile");
        this.policyFile = policyFile;
    }

    @Override
    public String getLog4jFile() {
        return log4jFile;
    }

    @Override
    public void setLog4jFile(String log4jFile) {
        modifiedOptions.add("log4jFile");
        this.log4jFile = log4jFile;
    }

    @Override
    public String getClassname() {
        return classname;
    }

    @Override
    public void setClassname(String classname) {
        checkStarted();
        this.classname = classname;
    }

    @Override
    public void resetParameters() {
        this.parameters.clear();
    }

    @Deprecated
    @Override
    public String getParameters() {
        StringBuilder sb = new StringBuilder();
        for (String param : this.parameters) {
            sb.append(param).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public List<String> getParametersAsList() {
        return this.parameters;
    }

    @Deprecated
    @Override
    public void setParameters(String params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        checkStarted();
        this.parameters.clear();
        for (String opt : params.split(" ")) {
            if (opt.length() != 0) {
                this.parameters.add(opt);
            }
        }
    }

    @Override
    public void setParameters(List<String> params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        checkStarted();
        this.parameters.clear();
        this.parameters.addAll(params);
    }

    @Override
    public void addJvmOption(String option) {
        if (option == null) {
            throw new IllegalArgumentException("A jvm option cannot be null");
        }
        this.jvmOptions.add(option);
    }

    /**
     * @deprecated use {@link JVMProcessImpl#getJvmOptionsAsList()} instead
     */
    @Deprecated
    @Override
    public String getJvmOptions() {
        StringBuilder sb = new StringBuilder();
        for (String option : this.jvmOptions) {
            sb.append(option).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public List<String> getJvmOptionsAsList() {
        return new ArrayList<String>(this.jvmOptions);
    }

    /**
     * @deprecated use {@link JVMProcessImpl#setJvmOptions(List)} instead
     */
    @Deprecated
    @Override
    public void setJvmOptions(String opts) {
        if (opts == null) {
            throw new IllegalArgumentException("Jvm options cannot be null");
        }
        checkStarted();
        this.jvmOptions.clear();
        for (String opt : opts.split(" ")) {
            if (opt.length() != 0) {
                this.jvmOptions.add(opt);
            }
        }
    }

    @Override
    public void setJvmOptions(List<String> opts) {
        if (opts == null) {
            throw new IllegalArgumentException("Jvm options cannot be null");
        }
        checkStarted();
        this.jvmOptions.clear();
        this.jvmOptions.addAll(opts);
    }

    @Override
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public void setExtendedJVM(JVMProcessImpl jvmProcess) {
        changeSettings(jvmProcess);
    }

    @Override
    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    @Override
    public void setOperatingSystem(OperatingSystem os) {
        this.os = os;
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return this.os;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getProcessId()
     */
    @Override
    public String getProcessId() {
        return "jvm";
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getNodeNumber()
     */
    @Override
    public int getNodeNumber() {
        return 1;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#getFinalProcess()
     */
    @Override
    public UniversalProcess getFinalProcess() {
        return this;
    }

    /**
     * @see org.objectweb.proactive.core.process.UniversalProcess#startProcess()
     */
    @Override
    public void startProcess() throws IOException {
        checkStarted();
        super.isStarted = true;
        if (super.username != null) {
            HostsInfos.setUserName(super.hostname, super.username);
        }

        //before starting the process we execute the filetransfer
        super.startFileTransfer();

        List<String> commandAsList = buildJavaCommand();
        String[] commandToExecute = commandAsList.toArray(new String[commandAsList.size()]);

        if (logger.isDebugEnabled()) {
            logger.debug("Running command: " + this.buildCommand());
        }
        try {
            super.shouldRun = true;
            super.externalProcess = Runtime.getRuntime().exec(commandToExecute);
            BufferedReader in = new BufferedReader(new InputStreamReader(externalProcess.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(externalProcess.getErrorStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(externalProcess.getOutputStream()));
            handleProcess(in, out, err);
        } catch (java.io.IOException e) {
            isFinished = true;
            throw e;
        }
    }

    public int getNewGroupId() {
        return JVMProcessImpl.groupID++;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected String buildCommand() {
        StringBuilder sb = new StringBuilder();
        for (String s : this.buildJavaCommand()) {
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }

    protected ArrayList<String> buildJavaCommand() {
        final ArrayList<String> javaCommand = new ArrayList<String>();

        if (!priority.equals(PriorityLevel.normal)) {
            switch (os) {
                case unix:
                    javaCommand.add(priority.unixCmd());
                    break;
                case windows:
                    javaCommand.add(priority.windowsCmd());
                    break;
                default:
                    throw new IllegalStateException("Unknown Operating System");
            }
        }

        // append java command
        if (this.javaPath == null) {
            javaCommand.add("java");
        } else {
            javaCommand.add(javaPath);
        }

        if (this.bootClasspath != null) {
            javaCommand.add("-Xbootclasspath:" + checkWhiteSpaces(this.bootClasspath));
        }

        // append policy option
        if (policyFile != null) {
            javaCommand.add(POLICY_OPTION + checkWhiteSpaces(policyFile));
        }

        // append log4j option
        if (log4jFile != null) {
            javaCommand.add(LOG4J_OPTION + checkWhiteSpaces(log4jFile));
        }

        // append user specified jvm options
        javaCommand.addAll(this.jvmOptions);

        // append classpath
        if ((classpath != null) && (classpath.length() > 0)) {
            javaCommand.add("-cp");
            javaCommand.add(checkWhiteSpaces(classpath));
        }

        // append classname
        javaCommand.add(this.classname);

        // append user specified parameters
        javaCommand.addAll(this.parameters);

        if (logger.isDebugEnabled()) {
            logger.debug("Java command: " + javaCommand.toString());
        }
        return javaCommand;
    }

    protected void changeSettings(JVMProcess jvmProcess) {
        if (!modifiedOptions.contains("classpath")) {
            this.classpath = jvmProcess.getClasspath();
        }
        if (!modifiedOptions.contains("bootClasspath")) {
            this.bootClasspath = jvmProcess.getBootClasspath();
        }
        if (!modifiedOptions.contains("javaPath")) {
            this.javaPath = jvmProcess.getJavaPath();
        }
        if (!modifiedOptions.contains("policyFile")) {
            this.policyFile = jvmProcess.getPolicyFile();
        }
        if (!modifiedOptions.contains("log4jFile")) {
            this.log4jFile = jvmProcess.getLog4jFile();
        }
        if (!overwrite) {
            this.jvmOptions.clear();
            this.setJvmOptions(jvmProcess.getJvmOptionsAsList());
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static String convertClasspathToAbsolutePath(String classpath) {
        StringBuffer absoluteClasspath = new StringBuffer();
        String pathSeparator = File.pathSeparator;
        StringTokenizer st = new StringTokenizer(classpath, pathSeparator);
        while (st.hasMoreTokens()) {
            absoluteClasspath.append(new File(st.nextToken()).getAbsolutePath());
            absoluteClasspath.append(pathSeparator);
        }
        return absoluteClasspath.substring(0, absoluteClasspath.length() - 1);
    }

    private static String getAbsolutePath(String path) {
        if (path.startsWith("file:")) {
            //remove file part to build absolute path
            path = path.substring(5);
        }
        return new File(path).getAbsolutePath();
    }

    private String checkWhiteSpaces(String path) {
        if (!path.startsWith("\"") && !path.startsWith("'")) {
            //if path does not start with " or ' we can check if there is whitespaces, 
            //if it does, we let the user handle its path
            if (path.indexOf(" ") > 0) {
                //if whitespaces, we surround all the path with double quotes
                path = "\"" + path + "\"";
            }
        }
        return path;
    }
}