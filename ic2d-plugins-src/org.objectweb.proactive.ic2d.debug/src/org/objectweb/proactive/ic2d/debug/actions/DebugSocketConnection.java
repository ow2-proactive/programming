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
package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.debug.dconnection.DebuggerInformation;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.debug.TunnelingTimeOutException;
import org.objectweb.proactive.ic2d.debug.connection.DebuggerSocketClient;
import org.objectweb.proactive.ic2d.debug.connection.DebuggerSocketServer;
import org.objectweb.proactive.ic2d.debug.dialogs.DebuggerBranchInfoDialog;
import org.objectweb.proactive.ic2d.debug.dialogs.TunnelingCreationWaitingDialog;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;


public class DebugSocketConnection {

    private RuntimeObject target;

    /**
     * A variable used to avoid garbage collection of the
     * {@link DebuggerSocketServer}
     */
    private DebuggerSocketServer server = null;

    /**
     * A variable used to avoid garbage collection of the
     * {@link DebuggerSocketClient}
     */
    private DebuggerSocketClient client = null;

    public DebugSocketConnection(RuntimeObject target) {
        this.target = target;
    }

    /**
     * Create a tunnel between the JVM of the object and the IC2D host
     */
    public void connectSocketDebugger() {
        DebuggerInformation nodeInfo = null;

        // create a feedback popup
        TunnelingCreationWaitingDialog t = new TunnelingCreationWaitingDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell());

        try {
            int nbOfTry = 1;
            while (true) {
                t.labelUp(nbOfTry); // update the popup
                nodeInfo = target.getDebugInfo();
                System.out.println("nodeInfo: " + nodeInfo);

                if (nodeInfo != null && nodeInfo.getDebuggerNode() != null)
                    break;

                if (nbOfTry > 50) {
                    throw new TunnelingTimeOutException("Reached the maximum connection attempt number (" +
                        nbOfTry + ")");
                }

                nbOfTry++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            int port = nodeInfo.getDebuggeePort();

            Node node = nodeInfo.getDebuggerNode();
            System.out.println("remote port: " + port);
            System.out.println("debug node: " + node);
            System.out.println("--");

            server = (DebuggerSocketServer) PAActiveObject.newActive(DebuggerSocketServer.class.getName(),
                    null);
            client = (DebuggerSocketClient) PAActiveObject.newActive(DebuggerSocketClient.class.getName(),
                    new Object[] {}, node);
            server.setTarget(client);
            client.setTarget(server);
            // server will be connected on a random port
            server.setPort(0);
            client.setPort(port);
            // client will be connected on its host
            client.setHost("localhost");
            server.connect();
            System.out.println("client port = " + client.getPort());
            System.out.println("server port = " + server.getPort());

            t.close(); //close the popup
            // create the port information windows
            new DebuggerBranchInfoDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                server.getPort());
            Console.getInstance(Activator.CONSOLE_NAME).log(
                    "Debugger connection tunnel open on : \"localhost:" + server.getPort() + "\".");

        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (TunnelingTimeOutException e) {
            t.showError();
            t.close();
        }
    }

    public void removeDebugger() {
        if (client != null) {
            try {
                client.closeConnection();
                client.terminate();
            } catch (Exception e) {
            }
            client = null;
        }
        if (server != null) {
            try {
                server.terminate();
            } catch (Exception e) {
            }
            server = null;
        }
        target.removeDebugger();
    }

    public boolean hasDebuggerConnected() {
        return target.hasDebuggerConnected();
    }
}
