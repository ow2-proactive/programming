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
package org.objectweb.proactive.core.node;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;


/**
 * <p>
 * The <code>NodeFactory</code> provides a generic way to create and lookup <code>Node</code>
 * without protocol specific code (such as RMI or HTTP).
 * </p><p>
 * <code>NodeFactory</code> provides a set of static methods to create and lookup <code>Node</code>.
 * To create a node it is only necessary to associate the protocol in the node url.
 * For instance :
 * </p>
 * <pre>
 *    rmi://localhost/node1
 *    http://localhost/node2
 * </pre>
 * <p>
 * As long as a protocol specific factory has been registered for the
 * given protocol, the creation of the node will be delegated to the right factory.
 * </p><p>
 * This class also provide the concept of default node and default protocol. When the protocol is not
 * specified in the node URL, the default protocol is used. When an active object is created in the local
 * JVM but without being attached to any node, a default node is created to hold that active object.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.1,  2002/08/28
 * @since   ProActive 0.9
 *
 */
@PublicAPI
public class NodeFactory {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.NODE);

    public static final String DEFAULT_VIRTUAL_NODE_NAME = "DefaultVN";

    private static final String DEFAULT_NODE_NAME = "Node";
    private static Node defaultNode = null;

    private static final String HALFBODIES_NODE_NAME = "HalfbodiesNode_";
    private static Node halfBodiesNode = null;

    static {
        ProActiveConfiguration.load();
    }

    //test with class loader
    //private static final ClassLoader myClassLoader = new NodeClassLoader();

    //
    // -- PUBLIC METHODS - STATIC -----------------------------------------------
    //

    /**
     * Returns the default local Node if it already exists, otherwise it
     * creates the local default Node and returns it.
     * @return the default local Node
     * @throws NodeException
     */
    public static synchronized Node getDefaultNode() throws NodeException {
        ProActiveSecurityManager securityManager = null;

        ProActiveRuntime runtime = ProActiveRuntimeImpl.getProActiveRuntime();

        while (defaultNode == null) {
            try {
                // hopefully no collision will occur
                defaultNode = runtime.createLocalNode(DEFAULT_NODE_NAME + ProActiveRandom.nextPosInt(),
                        false, securityManager, DEFAULT_VIRTUAL_NODE_NAME);
            } catch (ProActiveException e) {
                throw new NodeException("Cannot create the default Node", e);
            } catch (AlreadyBoundException e) {
                //if this exception is risen, we generate another random name for the node
                ProActiveLogger.logEatedException(logger, e);
            }

        }

        return defaultNode;
    }

    /**
     * Returns the halfbodies node if it already exists, otherwise it creates
     * a halfbodies  node and returns it. 
     * @return the Node that containes the halfbodies
     * @throws NodeException
     */
    public static synchronized Node getHalfBodiesNode() throws NodeException {
        ProActiveRuntime defaultRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        ProActiveSecurityManager securityManager = null;
        while (halfBodiesNode == null) {
            try {
                halfBodiesNode = defaultRuntime.createLocalNode(HALFBODIES_NODE_NAME +
                    ProActiveRandom.nextPosInt(), false, securityManager, DEFAULT_VIRTUAL_NODE_NAME);
            } catch (ProActiveException e) {
                throw new NodeException("Cannot create the halfbodies hosting Node", e);
            } catch (AlreadyBoundException e) {
                // try another name
                ProActiveLogger.logEatedException(logger, e);
            }
        }

        return halfBodiesNode;
    }

    /**
     * Return true if the given node is a halfbodies hosting node, false otherwise.
     * @param node the node to be tested.
     * @return true if the given node is a halfbodies hosting node, false otherwise.
     */
    public static boolean isHalfBodiesNode(Node node) {
        return isHalfBodiesNode(node.getNodeInformation().getURL());
    }

    /**
     * Return true if the given node is a halfbodies hosting node, false otherwise.
     * @param nodeUrl the url of the node to be tested.
     * @return true if the given node is a halfbodies hosting node, false otherwise.
     */
    public static boolean isHalfBodiesNode(String nodeUrl) {
        return nodeUrl.contains(HALFBODIES_NODE_NAME);
    }

    /*
     * check if the node name does not conflict with halfbodiesnode name.
     */
    private static void checkNodeName(String nodeName) throws NodeException {
        if (nodeName == null) {
            throw new NodeException("Node name cannot be null");
        }

        if (!nodeName.matches("[a-zA-Z0-9_-]+")) {
            throw new NodeException(nodeName + " is not a valid Node name");
        }

        if (nodeName.startsWith(HALFBODIES_NODE_NAME)) {
            throw new NodeException(nodeName + " is a reserved Node name");
        }
    }

    /**
     * Returns true if the given node belongs to this JVM false else.
     * @return true if the given node belongs to this JVM false else
     */
    public static boolean isNodeLocal(Node node) {
        return node.getVMInformation().getVMID().equals(UniqueID.getCurrentVMID());
    }

    /** Creates a new node on the local ProActive runtime.
     * 
     * @param nodeName 
     * 			name of the node to create. It musts comply to the following regular expression: "[a-zA-Z0-9_-]+"
     * @param replacePreviousBinding
     * 			Should an already existing node with the same name be replaced or not			
     * @param psm
     * 			A {@link ProActiveSecurityManager} or null
     * @param vnname
     * 			A Virtual Node name or null
     * @return  the newly created node on the local JVM
     * @exception NodeException 
     * 			if the node cannot be created or if the nodeName is invalid
     */
    public static Node createLocalNode(String nodeName, boolean replacePreviousBinding,
            ProActiveSecurityManager psm, String vnname) throws NodeException, AlreadyBoundException {
        ProActiveRuntime proActiveRuntime;

        // Throws an Exception is the name is invalid
        checkNodeName(nodeName);

        if (logger.isDebugEnabled()) {
            logger.debug("NodeFactory: createNode(" + nodeName + ")");
        }

        if (vnname == null) {
            vnname = DEFAULT_VIRTUAL_NODE_NAME;
        }

        //NodeFactory factory = getFactory(protocol);
        //then create a node
        try {
            proActiveRuntime = RuntimeFactory.getDefaultRuntime();
            return proActiveRuntime.createLocalNode(nodeName, replacePreviousBinding, psm, vnname);
        } catch (Exception e) {
            throw new NodeException("Failed to create a local node. name=" + nodeName, e);
        }
    }

    /**
     * Returns the reference to the node located at the given url.
     * This url can be either local or remote.
     * @param nodeURL The url of the node
     * @return Node. The reference of the node
     * @throws NodeException if the node cannot be found
     */
    public static Node getNode(String nodeURL) throws NodeException {
        ProActiveRuntime proActiveRuntime;
        String url;

        if (logger.isDebugEnabled()) {
            logger.debug("NodeFactory: getNode() for " + nodeURL);
        }

        try {
            //            url = URIBuilder.checkURI(nodeURL).toString();
            url = nodeURL; // #@#@ This modification can break proactive
            proActiveRuntime = RuntimeFactory.getRuntime(url);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        }

        Node node = new NodeImpl(proActiveRuntime, url);

        return node;
    }

    /**
     * Kills the node of the given url
     * @param nodeURL
     * @throws NodeException if a problem occurs when killing the node
     */
    public static void killNode(String nodeURL) throws NodeException {
        ProActiveRuntime proActiveRuntime;
        String url;

        try {
            url = URIBuilder.checkURI(nodeURL).toString();
            proActiveRuntime = RuntimeFactory.getRuntime(url);
            String nodeName = URIBuilder.getNameFromURI(nodeURL);
            proActiveRuntime.killNode(nodeName);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        } catch (URISyntaxException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        }
    }
}
