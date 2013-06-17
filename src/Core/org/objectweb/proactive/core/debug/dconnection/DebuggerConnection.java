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
package org.objectweb.proactive.core.debug.dconnection;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;


public class DebuggerConnection implements Serializable, NotificationListener {

    private static DebuggerConnection debuggerConnection = null;
    private static Logger debuggerLogger = ProActiveLogger.getLogger(Loggers.DEBUGGER);
    private String nodeName;
    private ProActiveRuntime tunnelRuntime;
    private boolean created = false;
    private boolean creating = false;
    private int listeningPort = -1;
    private Node debugNode;
    private int debugDeployementId = 54136432;

    public static synchronized DebuggerConnection getDebuggerConnection() {
        if (debuggerConnection == null)
            debuggerConnection = new DebuggerConnection();
        return debuggerConnection;
    }

    public DebuggerConnection() {
        nodeName = "DebugNode_" + System.getProperty("debugID") + "_" + new SecureRandom().nextInt();
        subscribeJMXRuntimeEvent();
    }

    public void update() {
        creating = false;
        created = false;
    }

    private static Integer tryPattern1(String processName) {
        Integer result = null;

        /* tested on: */
        /* - windows xp sp 2, java 1.5.0_13 */
        /* - mac os x 10.4.10, java 1.5.0 */
        /* - debian linux, java 1.5.0_13 */
        /* all return pid@host, e.g 2204@antonius */

        Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(processName);
        if (matcher.matches()) {
            result = new Integer(Integer.parseInt(matcher.group(1)));
        }
        return result;
    }

    private int findDebuggerPort() throws DebuggerException {
        RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
        String processName = rtb.getName();
        Integer pid = tryPattern1(processName);

        String address = null;

        try {
            Class<?> virtualMachineCl = Class.forName("com.sun.tools.attach.VirtualMachine");
            try {
                Method attachMethod = virtualMachineCl.getMethod("attach", String.class);
                Object vm = null;

                try {
                    vm = attachMethod.invoke(null, pid.toString());
                } catch (Exception e) {
                    // Failed to attach 
                    throw new DebuggerException("Failed to attach to the current JVM", e);
                }

                Method getAgentPropertiesMethod = vm.getClass().getMethod("getAgentProperties");
                try {
                    Properties props = (Properties) getAgentPropertiesMethod.invoke(vm);
                    address = props.getProperty("sun.jdwp.listenerAddress");

                    if ((address == null) || "".equals(address.trim())) {
                        throw new DebuggerException(
                            "The JVM is either not in debug mode or is not listening for a debugger to attach (probably one is already attached)");
                    }

                } catch (Exception e) {
                    // Probably an IOException
                    throw new DebuggerException("Failed to get the sun.jdwp.listenerAddress property", e);
                } finally {
                    Method detachMethod = vm.getClass().getMethod("detach");
                    detachMethod.invoke(vm);
                }
            } catch (Exception e) {
                // Java 6 but something gone wrong
                throw new DebuggerException("Failed to attach to the current VM", e);
            }

        } catch (ClassNotFoundException e) {
            String version = System.getProperty("java.specification.version");
            if ("1.5".equals(version)) { // Java 4 and older are not supported
                throw new DebuggerException(
                    "Remote debugging not yet available with Java 5. Please use a JDK 6");
            } else {
                throw new DebuggerException(
                    "Remote debbuging not available. Attach API not found in the classpath. $JDK6/lib/tools.jar must be in the classpath",
                    e);
            }
        }

        System.out.println("DebuggerConnection.findDebuggerPort() >>>>>>'" + address + "'");
        listeningPort = Integer.parseInt(address.split(":")[1]);

        if (listeningPort < 1) {
            throw new DebuggerException("cannot determine the port to attach to, answer was '" +
                listeningPort + "'");
        }

        return listeningPort;
    }

    /**
     * Get the information for connect a debugger. Create the debug node if it
     * does not exist.
     *
     * @return DebuggerInformation
     * @throws ProActiveException 
     */
    public synchronized DebuggerInformation getDebugInformation() throws DebuggerException {
        int port = -3;

        port = findDebuggerPort();
        getOrCreateNode();

        Sleeper sleeper = new Sleeper(500);

        while (creating) {
            sleeper.sleep();
        }

        return new DebuggerInformation(debugNode, port);
    }

    private synchronized void getOrCreateNode() {
        if (!created && !creating) {
            JVMProcessImpl vm = new JVMProcessImpl(new StandardOutputMessageLogger());
            vm.setClassname(StartPARuntime.class.getName());
            try {
                vm.setParameters(Arrays.asList("-d", "" + debugDeployementId, "-p", RuntimeFactory
                        .getDefaultRuntime().getURL().toString()));
                creating = true;
                vm.startProcess();
                ProActiveRuntimeImpl.getProActiveRuntime().getMBean().sendNotification(
                        NotificationType.debuggerConnectionActivated);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ProActiveException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * Kill the runtime hsting the debug node
     */
    public void removeDebugger() {
        try {
            tunnelRuntime.killRT(true);
            ProActiveRuntimeImpl.getProActiveRuntime().getMBean().sendNotification(
                    NotificationType.debuggerConnectionTerminated);
        } catch (Exception e) {
            //            e.printStackTrace();
        } finally {
            tunnelRuntime = null;
            created = false;
            creating = false;
        }
    }

    /**
     * @return true if there is a debugger connected, false otherwise
     */
    public boolean hasDebuggerConnected() {
        return tunnelRuntime != null;
    }

    private void subscribeJMXRuntimeEvent() {
        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
        part.addDeployment(debugDeployementId);
        JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
    }

    public void handleNotification(Notification notification, Object handback) {
        try {
            String type = notification.getType();

            if (NotificationType.GCMRuntimeRegistered.equals(type)) {
                GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                        .getUserData();
                if (data.getDeploymentId() != debugDeployementId) {
                    return;
                }

                tunnelRuntime = data.getChildRuntime();
                debugNode = tunnelRuntime.createLocalNode(nodeName, false, null, "PA_DebugVN");
                created = true;
                creating = false;
            }
        } catch (Exception e) {
            // If not handled by us, JMX eats the Exception !
            debuggerLogger.warn(e);
        } finally {
            notifyAll();
        }
    }

}
