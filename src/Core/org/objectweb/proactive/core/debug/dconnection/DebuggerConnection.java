/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.UnmarshalException;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.StartNode;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;


public class DebuggerConnection implements Serializable {

    private static final long serialVersionUID = 2591499219438855959L;

    private static String nodeName = "DebugNode";
    private static DebuggerInformation info;

    private static boolean created = false;

    public DebuggerConnection() {
    }

    static public int getPort() {
        String sPort;
        int iPort = -1;
        File file;
        BufferedReader reader = null;

        String name = System.getProperty("debugID");
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
        return iPort;
    }

    /**
     * Get the information for connect a debugger. Create the debug node if it
     * does not exist.
     *
     * @return DebuggerInformation
     */
    public static DebuggerInformation getDebugInfo() {
        if (info == null) {
            Node node = getOrCreateNode();
            info = new DebuggerInformation(node, getPort());
        } else if (info.getDebuggerNode() == null) {
            info = new DebuggerInformation(getOrCreateNode(), info.getDebuggeePort());
        }
        return info;
    }

    private static Node getOrCreateNode() {
        Node node = getNode();
        try {
            if (node == null && !created) {
                JVMProcessImpl vm = new JVMProcessImpl(new StandardOutputMessageLogger());
                vm.setClassname(StartNode.class.getName());
                vm.setParameters(nodeName);
                vm.startProcess();
                created = true;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                node = getNode();
            }
        } catch (IOException e) {
            e.printStackTrace();
            created = false;
            return null;
        }
        return node;
    }

    public static Node getNode() {
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(500);
                Node node = NodeFactory.getNode(nodeName);
                if (node != null) {
                    return node;
                }
            } catch (NodeException e) {
            } catch (InterruptedException e2) {
            }
        }
        return null;
    }

    /**
     * Kill the debug node if the number of active objects <= 0
     */
    public static void removeDebugger() {
        if (info != null) {
            try {
                Node node = info.getDebuggerNode();
                info = null;
                if (node != null) {
                    // bug fix:
                    // return 0 the 1st time, 1 the second time (if there is at least one node), etc
                    // then n tests might be needed to find if there are n nodes...
                    node.getNumberOfActiveObjects();
                    if (node.getNumberOfActiveObjects() <= 0) {
                        try {
                            created = false;
                            node.getProActiveRuntime().killRT(true);
                        } catch (EOFException e) {
                        } catch (UnmarshalException e) {
                        }
                    }
                }
            } catch (NodeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return true if there is a debugger connected, false otherwise
     */
    public static boolean hasDebuggerConnected() {
        return info != null;
    }

}
