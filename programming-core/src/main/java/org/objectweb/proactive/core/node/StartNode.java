/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.node;

import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * This class is a utility class allowing to start a ProActive node with a JVM.
 * It is very useful to start a node on a given host that will receive later
 * active objects created by other distributed applications.
 * </p><p>
 * This class has a main method and can be used directly from the java command.
 * <br>
 * use<br>
 * &nbsp;&nbsp;&nbsp;java org.objectweb.proactive.core.node.StartNode<br>
 * to print the options from command line or see the java doc of the main method.
 * </p><p>
 * A node represents the minimum services ProActive needs to work with a remote JVM.
 * Any JVM that is going to interact with active objects has at least one associated
 * node. The node must have a remote implementation that allow an object to remotely
 * invoke its methods.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class StartNode {
    static Logger logger;

    protected static final int MAX_RETRY = 3;

    protected static final String NO_REBIND_OPTION_NAME = "-noRebind";

    static {
        ProActiveConfiguration.load();
        logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);

        if (logger.isDebugEnabled()) {
            logger.debug("Loading ProActive class");
        }

        try {
            Class.forName("org.objectweb.proactive.api.PAActiveObject");
        } catch (ClassNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.fatal("Loading of ProActive class FAILED");
            }

            e.printStackTrace();
            PALifeCycle.exitFailure();
        }
    }

    protected boolean noRebind = false;

    protected String nodeName;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected StartNode() {
    }

    private StartNode(String[] args) {
        if (args.length == 0) {
            nodeName = null;
            printUsage();
        } else {
            nodeName = args[0];
            checkOptions(args, 1);
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static void main(String[] args) {
        try {
            new StartNode(args).run();
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal(e.toString());
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void checkOptions(String[] args, int start) {
        for (int i = start; i < args.length; i++)
            checkOption(args[i]);
    }

    protected void createNode(String nodeName, boolean noRebind) throws NodeException, AlreadyBoundException {
        int exceptionCount = 0;

        while (true) {
            try {
                Node node = null;

                if (nodeName == null) {
                    node = NodeFactory.getDefaultNode();
                } else {
                    node = NodeFactory.createLocalNode(nodeName, !noRebind, null);
                }

                logger.info("OK. Node " + node.getNodeInformation().getName() + " ( " +
                            node.getNodeInformation().getURL() + " ) " + " is created in VM id=" +
                            UniqueID.getCurrentVMID());

                break;
            } catch (NodeException e) {
                exceptionCount++;

                if (exceptionCount == MAX_RETRY) {
                    throw e;
                } else {
                    logger.error("Error, retrying (" + exceptionCount + ")");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                }

                // end if
            }

            // try
        }

        // end while
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Run the complete creation of the node step by step by invoking the other
     * helper methods
     */
    protected void run() throws java.io.IOException, NodeException, AlreadyBoundException {
        // create node
        createNode(nodeName, noRebind);
    }

    /**
     * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
     * Checks one given option from the arguments
     */
    protected void checkOption(String option) {
        if (NO_REBIND_OPTION_NAME.equals(option)) {
            noRebind = true;
        } else {
            printUsage();
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void printUsage() {
        logger.info("usage: java " + this.getClass().getName() + " <node name> [options]");
        logger.info(" - options");
        logger.info("                      By default a ClassServer is automatically created");
        logger.info("                      to serve class files on demand.");
        logger.info("     " + NO_REBIND_OPTION_NAME + "      : indicates not to use rebind when registering the");
        logger.info("                      node to the registry. If a node of the same name");
        logger.info("                      already exists, the creation of the new node will fail.");
        logger.info("  for instance: java " + StartNode.class.getName() + " myNode");
        logger.info("                java " + StartNode.class.getName() + " myNode2  " + NO_REBIND_OPTION_NAME);
    }
}
