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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.Tool;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.Tools;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement.PathBase;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeInternal;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;
import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;


public class CommandBuilderProActive implements CommandBuilder {

    final static String PROACTIVE_JAR = "ProActive.jar";

    final static String TOKEN = "___TOKEN___";

    /** Path to the ProActive installation */
    private PathElement proActivePath;

    /** Declared Virtual nodes */
    private Map<String, GCMVirtualNodeInternal> vns;

    /** Path to ${java.home}/bin/java */
    private PathElement javaPath = null;

    /** Arguments to be passed to java */
    private Set<String> jvmArgs;

    /**
     * ProActive classpath
     * 
     * If not set, then the default classpath is used
     */
    private List<PathElement> proactiveClasspath;
    private boolean overwriteClasspath;

    /** Application classpath */
    private List<PathElement> applicationClasspath;

    /** Security Policy file */
    private PathElement javaSecurityPolicy;

    /** Log4j configuration file */
    private PathElement log4jProperties;

    /** User properties file */
    private PathElement userProperties;

    /** application security policy file */
    private PathElement applicationPolicy;

    /** runtime security policy file */
    private PathElement runtimePolicy;

    /** JVM debug mode configuration */
    private List<String> debugCommandLine;

    /** indicates if the debug tag has been found in the GCMA */
    private boolean isDebugEnabled = false;

    public CommandBuilderProActive() {
        GCMD_LOGGER.trace(this.getClass().getSimpleName() + " created");
        vns = new HashMap<String, GCMVirtualNodeInternal>();
        jvmArgs = new HashSet<String>();
    }

    public void addVirtualNode(GCMVirtualNodeInternal vn) {
        addVirtualNode(vn.getName(), vn);
    }

    public void addVirtualNode(String id, GCMVirtualNodeInternal vn) {
        vns.put(id, vn);
    }

    public void addProActivePath(PathElement pe) {
        if (proactiveClasspath == null) {
            proactiveClasspath = new ArrayList<PathElement>();
        }

        proactiveClasspath.add(pe);
    }

    public void setProActiveClasspath(List<PathElement> pe) {
        proactiveClasspath = pe;
    }

    public void addApplicationPath(PathElement pe) {
        if (applicationClasspath == null) {
            applicationClasspath = new ArrayList<PathElement>();
        }

        applicationClasspath.add(pe);
    }

    public void setApplicationClasspath(List<PathElement> pe) {
        if (GCMD_LOGGER.isTraceEnabled()) {
            GCMD_LOGGER.trace(" Set ApplicationClasspath to:");
            for (PathElement e : pe) {
                GCMD_LOGGER.trace("\t" + e);
            }
        }

        applicationClasspath = pe;
    }

    public void setVirtualNodes(Map<String, GCMVirtualNodeInternal> vns) {
        if (GCMD_LOGGER.isTraceEnabled()) {
            GCMD_LOGGER.trace(" Set VirtualNodes to:");
            for (String vn : vns.keySet()) {
                GCMD_LOGGER.trace("\t" + vn);
            }
        }

        this.vns = vns;
    }

    public void addJVMArg(String arg) {
        if (arg != null) {
            if (arg != null) {
                GCMD_LOGGER.trace(" Added " + arg + " to JVMargs");
                jvmArgs.addAll(CommandBuilderHelper.parseArg(arg));
            }
        }
    }

    public void setJavaPath(PathElement pe) {
        if (pe != null) {
            GCMD_LOGGER.trace(" Set JavaPath to " + pe);
            javaPath = pe;
        }
    }

    public void setLog4jProperties(PathElement pe) {
        if (pe != null) {
            GCMD_LOGGER.trace(" Set log4jProperties relpath to " + pe.getRelPath());
            log4jProperties = pe;
        }
    }

    public void setSecurityPolicy(PathElement pe) {
        if (pe != null) {
            GCMD_LOGGER.trace(" Set securityPolicy relpath to " + pe.getRelPath());
            javaSecurityPolicy = pe;
        }
    }

    public void setApplicationPolicy(PathElement pe) {
        if (pe != null) {
            GCMD_LOGGER.trace(" Set applicationPolicy relpath to " + pe.getRelPath());
            applicationPolicy = pe;

        }

    }

    public void setRuntimePolicy(PathElement pe) {
        if (pe != null) {
            GCMD_LOGGER.trace(" Set runtimePolicy relpath to " + pe.getRelPath());
            runtimePolicy = pe;
        }
    }

    public PathElement getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(PathElement userProperties) {
        if (userProperties != null) {
            GCMD_LOGGER.trace(" Set userProperties relpath to " + userProperties.getRelPath());
            this.userProperties = userProperties;
        }
    }

    /**
     * Returns the java executable to be used
     * 
     * <ol>
     * <li> Uses the java element inside GCMA/proactive/config </li>
     * <li> Uses the java tool defined by the hostInfo </li>
     * <li> returns "java" and lets the $PATH magic occur </li>
     * 
     * @param hostInfo
     * @return the java command to be used for this host
     */
    private String getJava(HostInfo hostInfo) {
        String javaCommand = "java";

        if (javaPath != null) {
            javaCommand = javaPath.getFullPath(hostInfo, this);
        } else {
            Tool javaTool = hostInfo.getTool(Tools.JAVA.id);
            if (javaTool != null) {
                javaCommand = javaTool.getPath();
            }
        }
        return javaCommand;
    }

    /**
     * Return a String defining the Classpath for the ProActive runtime
     * to deploy on a remote host.
     * 
     * @param hostInfo the HostInfo object defining attributes of a target host.   
     * @return a String defining a platform-specific classpath.
     */
    public String getClasspath(HostInfo hostInfo) {
        StringBuilder sb = new StringBuilder();

        if (!overwriteClasspath) {
            // ProActive.jar contains a JAR index
            // see: http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#JAR%20Index

            char fs = hostInfo.getOS().fileSeparator();
            sb.append(this.getPath(hostInfo));
            sb.append(fs);
            sb.append("dist");
            sb.append(fs);
            sb.append("lib");
            sb.append(fs);
            sb.append(PROACTIVE_JAR);
            sb.append(hostInfo.getOS().pathSeparator());

            if (isDebugEnabled) {
                String javaCommand = null;

                if (javaPath != null) {
                    javaCommand = javaPath.getFullPath(hostInfo, this);
                } else {
                    Tool javaTool = hostInfo.getTool(Tools.JAVA.id);
                    if (javaTool != null) {
                        javaCommand = javaTool.getPath();
                    }
                }

                if (javaCommand == null) {
                    // Java location must be set when the debug mode is enabled
                    // TODO throw an exception
                    GCMA_LOGGER
                            .warn("GCMApplication/application/proactive/configuration/java is NOT set. Remote debbuging will fail");
                } else {
                    // Check if we are able to guess tool.jar location
                    File f = new File(javaCommand.trim());
                    if (!f.exists() || javaCommand.lastIndexOf(fs) < 0) {
                        GCMA_LOGGER
                                .warn("Unable to find tool.jar, please specify a full or relative path for java (" +
                                    javaCommand + ")");
                    } else {
                        sb.append(javaCommand.substring(0, javaCommand.lastIndexOf(fs)));
                        sb.append(fs);
                        sb.append("..");
                        sb.append(fs);
                        sb.append("lib");
                        sb.append(fs);
                        sb.append("tools.jar");
                        sb.append(hostInfo.getOS().pathSeparator());
                    }
                }
            }
        }

        if (proactiveClasspath != null) {
            for (PathElement pe : proactiveClasspath) {
                sb.append(pe.getFullPath(hostInfo, this));
                sb.append(hostInfo.getOS().pathSeparator());
            }
        }

        if (applicationClasspath != null) {
            for (PathElement pe : applicationClasspath) {
                sb.append(pe.getFullPath(hostInfo, this));
                sb.append(hostInfo.getOS().pathSeparator());
            }
        }

        // Trailing pathSeparator don't forget to remove it later
        return sb.substring(0, sb.length() - 1);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder#buildCommand(org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo, org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal)
     */
    public String buildCommand(HostInfo hostInfo, GCMApplicationInternal gcma) {
        return buildCommand(hostInfo, gcma, true);
    }

    /**
     * Build a command defining a ProActive runtime launching on a target host.
     * @param hostInfo HostInfo defining target's host properties.
     * @param gcma GCMA containing ProActive application attributes to launch.
     * @param withClasspath if true, specify directly the classpath in the command with
     * -cp argument after 'java' command (Classpath surrounded by "), otherwise classpath is not defined.
     * @return
     */
    public String buildCommand(HostInfo hostInfo, GCMApplicationInternal gcma, boolean withClasspath) {
        if ((proActivePath == null) && (hostInfo.getTool(Tools.PROACTIVE.id) == null)) {
            throw new IllegalStateException(
                "ProActive installation path must be specified with the relpath attribute inside the proactive element (GCMA), or as tool in all hostInfo elements (GCMD). HostInfo=" +
                    hostInfo.getId());
        }

        if (!hostInfo.isCapacitiyValid()) {
            throw new IllegalStateException(
                "To enable capacity autodetection nor VM Capacity nor Host Capacity must be specified. HostInfo=" +
                    hostInfo.getId());
        }

        StringBuilder command = new StringBuilder();
        // Java
        command.append(getJava(hostInfo));
        command.append(" ");

        Tool jp = hostInfo.getTool(Tools.JAVA_PARAMETERS.id);
        if (jp != null) {
            command.append(" " + jp.getPath() + " "); // Not really a path, but it's ok
        }

        for (String arg : jvmArgs) {
            command.append(arg);
            command.append(" ");
        }

        if (CentralPAPropertyRepository.PA_TEST.isTrue()) {
            command.append(CentralPAPropertyRepository.PA_TEST.getCmdLine());
            command.append("true ");
        }

        if (withClasspath) {
            //add classpath string surrounded by ",
            //to deal with OS that accept paths without escaped spaces chars
            StringBuilder sb = new StringBuilder();
            command.append("-cp \"");
            command.append(getClasspath(hostInfo));
            command.append("\" ");
        } else
            command.append(" ");

        // Log4j
        if (log4jProperties != null) {
            command.append(CentralPAPropertyRepository.LOG4J.getCmdLine());
            command.append("\"");
            command.append("file:");
            command.append(log4jProperties.getFullPath(hostInfo, this));
            command.append("\"");
            command.append(" ");
        } else {
            command.append(CentralPAPropertyRepository.PA_LOG4J_COLLECTOR.getCmdLine());
            command.append(gcma.getLogCollectorUrl());
            command.append(" ");
        }

        // Java Security Policy
        if (javaSecurityPolicy != null) {
            command.append(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine());
            command.append("\"");
            command.append(javaSecurityPolicy.getFullPath(hostInfo, this));
            command.append("\"");
            command.append(" ");
        } else {
            command.append(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine());
            command.append("\"");
            command.append(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue());
            command.append("\"");
            command.append(" ");
        }

        if (hostInfo.getNetworkInterface() != null) {
            command.append(CentralPAPropertyRepository.PA_NET_INTERFACE.getCmdLine() +
                hostInfo.getNetworkInterface());
            command.append(" ");
        }

        if (runtimePolicy != null) {
            command.append(CentralPAPropertyRepository.PA_RUNTIME_SECURITY.getCmdLine());
            command.append("\"");
            command.append(runtimePolicy.getFullPath(hostInfo, this));
            command.append("\"");
            command.append(" ");
        }

        if (hostInfo.getDataSpacesScratchURL() != null) {
            command.append(CentralPAPropertyRepository.PA_DATASPACES_SCRATCH_URL.getCmdLine());
            command.append("\"");
            command.append(hostInfo.getDataSpacesScratchURL());
            command.append("\"");
            command.append(" ");
        }

        if (hostInfo.getDataSpacesScratchPath() != null) {
            command.append(CentralPAPropertyRepository.PA_DATASPACES_SCRATCH_PATH.getCmdLine());
            command.append("\"");
            command.append(hostInfo.getDataSpacesScratchPath().getFullPath(hostInfo, this));
            command.append("\"");
            command.append(" ");
        }

        if (isDebugEnabled) {
            command.append(" " + getDebugCommand(hostInfo, gcma.getDeploymentId()) + " ");
        }

        // Class to be started and its arguments
        command.append(StartPARuntime.class.getName());
        command.append(" ");

        String parentURL;
        try {
            parentURL = RuntimeFactory.getDefaultRuntime().getURL();
        } catch (ProActiveException e) {
            GCMD_LOGGER.error(
                    "Cannot determine the URL of this runtime. Childs will not be able to register", e);
            parentURL = "unkownParentURL";
        }
        command.append("-" + StartPARuntime.Params.parent.shortOpt() + " " + parentURL);
        command.append(" ");

        if (hostInfo.getVmCapacity() != 0) {
            command.append("-" + StartPARuntime.Params.capacity.shortOpt() + " " + hostInfo.getVmCapacity());
            command.append(" ");
        }

        command.append("-" + StartPARuntime.Params.topologyId.shortOpt() + " " + hostInfo.getToplogyId());
        command.append(" ");

        command.append("-" + StartPARuntime.Params.deploymentId.shortOpt() + " " + gcma.getDeploymentId());
        command.append(" ");

        // TODO cdelbe Check FT properties here
        // was this.ftService.buildParamsLine();

        StringBuilder ret = new StringBuilder();

        if (hostInfo.getHostCapacity() == 0) {
            ret.append(command);
        } else {
            switch (hostInfo.getOS()) {
                case unix:
                    String cmd = command.toString();
                    for (int i = 0; i < hostInfo.getHostCapacity(); i++) {
                        ret.append(cmd.replaceAll(TOKEN, "" + i));
                        ret.append(" &");
                    }
                    ret.deleteCharAt(ret.length() - 1);
                    break;

                case windows:
                    char fs = hostInfo.getOS().fileSeparator();
                    ret.append("\"");
                    ret.append(getPath(hostInfo));
                    ret.append(fs);
                    ret.append("dist");
                    ret.append(fs);
                    ret.append("scripts");
                    ret.append(fs);
                    ret.append("gcmdeployment");
                    ret.append(fs);
                    ret.append("startn.bat");
                    ret.append("\"");

                    ret.append(" ");
                    ret.append(hostInfo.getHostCapacity());

                    ret.append(" ");
                    ret.append("\"");
                    ret.append(command);
                    ret.append("\"");
                    break;
            }
        }

        GCMD_LOGGER.trace(ret);
        return ret.toString();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder#buildCommand(org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo, org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal)
     */
    public List<List<String>> buildCommandLocal(HostInfo hostInfo, GCMApplicationInternal gcma) {
        return buildCommandLocal(hostInfo, gcma, true);
    }

    /**
     * Build a command defining a ProActive runtime launching on a target host.
     * This command builder uses a List to build the command instead of a String builder, this is less error-prone and works much better on Windows
     * @param hostInfo HostInfo defining target's host properties.  
     * @param gcma GCMA containing ProActive application attributes to launch.
     * @param withClasspath if true, specify directly the classpath in the command with
     * -cp argument after 'java' command (Classpath surrounded by "), otherwise classpath is not defined.
     * @return
     */
    public List<List<String>> buildCommandLocal(HostInfo hostInfo, GCMApplicationInternal gcma,
            boolean withClasspath) {
        if ((proActivePath == null) && (hostInfo.getTool(Tools.PROACTIVE.id) == null)) {
            throw new IllegalStateException(
                "ProActive installation path must be specified with the relpath attribute inside the proactive element (GCMA), or as tool in all hostInfo elements (GCMD). HostInfo=" +
                    hostInfo.getId());
        }

        if (!hostInfo.isCapacitiyValid()) {
            throw new IllegalStateException(
                "To enable capacity autodetection nor VM Capacity nor Host Capacity must be specified. HostInfo=" +
                    hostInfo.getId());
        }

        ArrayList<String> command = new ArrayList<String>();
        // Java
        command.add(getJava(hostInfo));

        Tool jp = hostInfo.getTool(Tools.JAVA_PARAMETERS.id);
        if (jp != null) {
            command.add(jp.getPath()); // Not really a path, but it's ok
        }

        for (String arg : jvmArgs) {
            command.add(arg);
        }

        if (CentralPAPropertyRepository.PA_TEST.isTrue()) {
            command.add(CentralPAPropertyRepository.PA_TEST.getCmdLine() + "true");
        }

        if (withClasspath) {
            //add classpath string surrounded by ", 
            //to deal with OS that accept paths without escaped spaces chars
            StringBuilder sb = new StringBuilder();
            command.add("-cp");
            command.add(getClasspath(hostInfo));
        }
        // Log4j
        if (log4jProperties != null) {
            command.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
                log4jProperties.getFullPath(hostInfo, this));
        } else {
            command.add(CentralPAPropertyRepository.PA_LOG4J_COLLECTOR.getCmdLine() +
                gcma.getLogCollectorUrl());
        }

        // Java Security Policy
        if (javaSecurityPolicy != null) {
            command.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
                javaSecurityPolicy.getFullPath(hostInfo, this));
        } else {
            command.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
                CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue());
        }

        if (hostInfo.getNetworkInterface() != null) {
            command.add(CentralPAPropertyRepository.PA_NET_INTERFACE.getCmdLine() +
                hostInfo.getNetworkInterface());
        }

        if (runtimePolicy != null) {
            command.add(CentralPAPropertyRepository.PA_RUNTIME_SECURITY.getCmdLine() +
                runtimePolicy.getFullPath(hostInfo, this));
        }

        if (hostInfo.getDataSpacesScratchURL() != null) {
            command.add(CentralPAPropertyRepository.PA_DATASPACES_SCRATCH_URL.getCmdLine() +
                hostInfo.getDataSpacesScratchURL());
        }

        if (hostInfo.getDataSpacesScratchPath() != null) {
            command.add(CentralPAPropertyRepository.PA_DATASPACES_SCRATCH_PATH.getCmdLine() +
                hostInfo.getDataSpacesScratchPath().getFullPath(hostInfo, this));
        }

        if (isDebugEnabled) {
            command.addAll(getDebugCommand(hostInfo, gcma.getDeploymentId()));
        }

        // Class to be started and its arguments
        command.add(StartPARuntime.class.getName());

        String parentURL;
        try {
            parentURL = RuntimeFactory.getDefaultRuntime().getURL();
        } catch (ProActiveException e) {
            GCMD_LOGGER.error(
                    "Cannot determine the URL of this runtime. Children will not be able to register", e);
            parentURL = "unkownParentURL";
        }
        command.add("-" + StartPARuntime.Params.parent.shortOpt());
        command.add(parentURL);

        if (hostInfo.getVmCapacity() != 0) {
            command.add("-" + StartPARuntime.Params.capacity.shortOpt());
            command.add("" + hostInfo.getVmCapacity());
        }

        command.add("-" + StartPARuntime.Params.topologyId.shortOpt());
        command.add("" + hostInfo.getToplogyId());

        command.add("-" + StartPARuntime.Params.deploymentId.shortOpt());
        command.add("" + gcma.getDeploymentId());

        // TODO cdelbe Check FT properties here
        // was this.ftService.buildParamsLine();

        ArrayList<List<String>> commandList = new ArrayList<List<String>>();
        for (int i = 0; i < hostInfo.getHostCapacity(); i++) {
            ArrayList<String> commandi = new ArrayList<String>(command);
            for (int j = 0; j < commandi.size(); j++) {
                commandi.set(j, commandi.get(j).replaceAll(TOKEN, "" + i));
            }
            commandList.add(commandi);
        }

        GCMD_LOGGER.trace(commandList);
        return commandList;
    }

    public void setProActivePath(String proActivePath) {
        setProActivePath(proActivePath, PathBase.HOME.name());
    }

    public void setProActivePath(String proActivePath, String base) {
        if (proActivePath != null) {
            this.proActivePath = new PathElement(proActivePath, base);
            GCMD_LOGGER.trace(" Set ProActive relpath to " + this.proActivePath.getRelPath());
        }
    }

    public String getPath(HostInfo hostInfo) {
        String ret = null;

        Tool proactive = null;
        proactive = hostInfo.getTool(Tools.PROACTIVE.id);
        if (proactive != null) {
            // GCMD overridden the ProActive location. Use the GCMD declaration
            ret = proactive.getPath();
        } else {
            // Use the GCMA definition
            if (proActivePath != null) {
                ret = proActivePath.getFullPath(hostInfo, this);
            } else {
                ret = null;
            }
        }

        return ret;
    }

    private static String getAbsolutePath(String path) {
        if (path.startsWith("file:")) {
            //remove file part to build absolute path
            path = path.substring(5);
        }
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            GCMDeploymentLoggers.GCMA_LOGGER.error(e.getMessage());
            return path;
        }
    }

    public void setOverwriteClasspath(boolean overwriteClasspath) {
        this.overwriteClasspath = overwriteClasspath;
    }

    public void setDebugCommand(String debugCommand) {
        debugCommandLine = new ArrayList<String>();
        if (debugCommand != null) {
            GCMD_LOGGER.trace(" Set " + debugCommand + " to debugCommand");
            debugCommandLine.addAll(CommandBuilderHelper.parseArg(debugCommand));
        }
    }

    protected List<String> getDebugCommand(HostInfo hostInfo, long deploymentId) {
        //        Tool javaTool = hostInfo.getTool(Tools.JAVA.id);
        //        String java = "java";
        //        if (javaTool != null) {
        //            java = javaTool.getPath();
        //        }
        if (debugCommandLine == null) {
            debugCommandLine = new ArrayList<String>();
            String token = "padebug_" + deploymentId + "_" + TOKEN;
            debugCommandLine.add("-DdebugID=" + token);
            debugCommandLine.add("-Xdebug");
            debugCommandLine.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=0");
        }
        return debugCommandLine;
    }

    public void enableDebug(boolean b) {
        isDebugEnabled = b;
    }
}
