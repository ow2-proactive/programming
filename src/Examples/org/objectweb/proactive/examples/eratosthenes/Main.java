/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.eratosthenes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 * Main program for the Eratosthenes example. This class starts
 * an output listener, the first ActivePrimeContainer and a number source.
 * It also serves as creator of new ActivePrimeContainers.<br>
 * An XML descriptor file can be passed as first parameter, in this case,
 * the active objects are created in the nodes described by the virtual nodes
 * Containers, NumberSource and OutputListener. <br>
 * Main is not migratable due to the VirtualNode object.<br>
 * A control window allows to terminate the application and to pause
 * temporarily the NumberSource.
 * */
@ActiveObject
public class Main implements ActivePrimeContainerCreator, InitActive {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private PrimeOutputListener outputListener;
    private NumberSource source;
    private GCMVirtualNode containersVirtualNode;
    private Node listenerNode;
    private Node sourceNode;
    private GCMApplication pad;
    private boolean gui;
    private Node lastNode;
    private int nodeCount;

    /** Place four ActivePrimeContainers in a Node before using the next for the distributed version. */
    private static final int ACTIVEPRIMECONTAINERS_PER_NODE = 4;

    /**
     * Constructor for Main.
     */
    public Main() {
    }

    public Main(String xmlDescriptor, Boolean gui) throws ProActiveException {
        // read XML Descriptor
        if (xmlDescriptor.length() > 0) {

            pad = PAGCMDeployment.loadApplicationDescriptor(new File(xmlDescriptor));

        }
        this.gui = gui.booleanValue();
    }

    /** Creates a new ActivePrimeContainer starting with number n */
    public ActivePrimeContainer newActivePrimeContainer(long n, Slowable previous) {

        ActivePrimeContainer result = null;

        try {
            int containerSize;

            /*
             * Create the new container with size = SQRT(n) * 20, but at least 100 and at most 1000
             */
            containerSize = (int) Math.sqrt(n) * 20;
            if (containerSize < 100) {
                containerSize = 100;
            } else if (containerSize > 1000) {
                containerSize = 1000;
            }

            // find correct node or use default node  		
            Node node;
            if (containersVirtualNode != null) { // alternate between nodes for creating containers
                if (lastNode == null) {
                    lastNode = containersVirtualNode.getANode();
                    node = sourceNode;
                    nodeCount = 0;
                } else if (nodeCount < ACTIVEPRIMECONTAINERS_PER_NODE) {
                    node = lastNode;
                    nodeCount++;
                } else {
                    lastNode = node = containersVirtualNode.getANode();
                    nodeCount = 1;
                }
            } else {
                node = NodeFactory.getDefaultNode();
            }

            logger.info("    Creating container with size " + containerSize + " starting with number " + n);
            result = PAActiveObject.newActive(ActivePrimeContainer.class, new Object[] {
                    PAActiveObject.getStubOnThis(), outputListener, new Integer(containerSize), new Long(n),
                    previous }, node);

            // Workaround for a little bug in ProActive (Exception in receiveRequest)
            // may be removed as the bug is fixed
            // This call makes us wait while the newly created object is not yet in his runActivity() method
            long v = result.getValue();

        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void initActivity(Body b) {
        try {
            if (pad != null) {
                // create nodes
                pad.startDeployment();
                containersVirtualNode = pad.getVirtualNode("Containers");
                listenerNode = pad.getVirtualNode("OutputListener").getANode();
                sourceNode = pad.getVirtualNode("NumberSource").getANode();
            } else {
                listenerNode = sourceNode = NodeFactory.getDefaultNode();
            }

            // create output listener
            outputListener = PAActiveObject.newActive(ConsolePrimeOutputListener.class, new Object[] {},
                    listenerNode);

            outputListener.newPrimeNumberFound(2);

            // create number source  
            source = PAActiveObject.newActive(NumberSource.class, new Object[] {}, sourceNode);

            // create first container  			
            ActivePrimeContainer first = newActivePrimeContainer(3, source);

            source.setFirst(first);

            if (gui) {
                new ControlFrame(this);
            } else {
                source.pause(false); // start immediately if no gui
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void exit() {
        if (pad != null) {
            pad.kill();
        }

        System.exit(0);
    }

    public NumberSource getSource() {
        return source;
    }

    public static void main(String[] args) throws ProActiveException {
        String xmlDescriptor = "";
        boolean gui = true;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("-nogui")) {
                gui = false;
                if (args.length > 1) {
                    xmlDescriptor = args[1];
                }
            } else {
                xmlDescriptor = args[0];
            }
        }
        ProActiveConfiguration.load();
        Main main = PAActiveObject
                .newActive(Main.class, new Object[] { xmlDescriptor, Boolean.valueOf(gui) });
    }

    /** class for control window. */
    class ControlFrame extends JFrame implements ActionListener {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;
        private JButton exitButton;
        private JToggleButton pauseButton;
        private Main main;

        ControlFrame(Main m) {
            super("Eratosthenes control window");
            main = m;
            setSize(300, 80);
            getContentPane().setLayout(new java.awt.FlowLayout());
            pauseButton = new JToggleButton("Pause", true);
            exitButton = new JButton("Exit");
            pauseButton.addActionListener(this);
            exitButton.addActionListener(this);
            getContentPane().add(pauseButton);
            getContentPane().add(exitButton);
            setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == exitButton) {
                main.exit();
            } else if (e.getSource() == pauseButton) {
                main.getSource().pause(pauseButton.isSelected());
            }
        }
    }
}
