/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.dconnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class DebuggerConnection implements Serializable, NotificationListener {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private static DebuggerConnection debuggerConnection;
    private static Logger debuggerLogger = ProActiveLogger.getLogger(Loggers.DEBUGGER);
    private String nodeName;
    private ProActiveRuntime tunnelRuntime;
    private boolean created = false;
    private boolean creating = false;
    private int listeningPort = 0;
    private Node debugNode;
    private int debugDeployementId = 54136432;

    public static synchronized DebuggerConnection getDebuggerConnection() {
        if (debuggerConnection == null) {
            debuggerConnection = new DebuggerConnection();
        }
        return debuggerConnection;
    }

    public DebuggerConnection() {
        nodeName = "DebugNode_" + System.getProperty("debugID") + "_" + new SecureRandom().nextInt();
        parseDebuggingPort();
        subscribeJMXRuntimeEvent();
    }

    private void parseDebuggingPort() {
        String sPort;
        int iPort = -1;
        File file;
        BufferedReader reader = null;

        String name = System.getProperty("debugID");
        /*+ "_" +
            ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();*/
        if (name == null) {
            return;
        }
        try {
            file = new File(System.getProperty("java.io.tmpdir") + File.separator + name);
            reader = new BufferedReader(new FileReader(file));
            sPort = reader.readLine();
            iPort = Integer.parseInt(sPort);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        this.listeningPort = iPort;
    }

    /**
     * Get the information for connect a debugger. Create the debug node if it
     * does not exist.
     *
     * @return DebuggerInformation
     */
    public synchronized DebuggerInformation getDebugInfo() {
        getOrCreateNode();
        if (creating) {
            try {
                wait(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new DebuggerInformation(debugNode, listeningPort);
    }

    private synchronized void getOrCreateNode() {
        if (!created && !creating) {
            JVMProcessImpl vm = new JVMProcessImpl(new StandardOutputMessageLogger());
            vm.setClassname(StartPARuntime.class.getName());
            try {
                vm.setParameters("-d " + debugDeployementId + " -p " +
                    RuntimeFactory.getDefaultRuntime().getURL());
                creating = true;
                vm.startProcess();
                ProActiveRuntimeImpl.getProActiveRuntime().getMBean().sendNotification(
                        NotificationType.debuggerConnectionActivated);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ProActiveException e1) {
                // TODO Auto-generated catch block
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
            e.printStackTrace();
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
                debugNode = tunnelRuntime.createLocalNode(nodeName, false, null, "PA_DebugVN", null);
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
