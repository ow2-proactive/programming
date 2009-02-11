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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.node;

import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


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
    private static final AtomicInteger nodeCounter = new AtomicInteger();
    private static Node defaultNode = null;

    private static final String HALFBODIES_NODE_NAME = "__PA__HalfbodiesNode";
    private static final AtomicInteger halfbodyCounter = new AtomicInteger();
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
     * 
     * @return
     * @throws NodeException
     */
    public static synchronized Node getDefaultNode() throws NodeException {
        ProActiveRuntime defaultRuntime = null;
        String jobID = PAActiveObject.getJobId();
        ProActiveSecurityManager securityManager = null;
        if (defaultNode == null) {
            try {
                defaultRuntime = RuntimeFactory.getDefaultRuntime();
                defaultNode = defaultRuntime.createLocalNode(DEFAULT_NODE_NAME +
                    nodeCounter.incrementAndGet(), false, securityManager, DEFAULT_VIRTUAL_NODE_NAME, jobID);
            } catch (ProActiveException e) {
                throw new NodeException("Cannot create the default Node", e);
            } catch (AlreadyBoundException e) { //if this exception is risen, we generate another random name for the node
                getDefaultNode();
            }
        }
        return defaultNode;
    }

    /**
     * 
     * @return
     * @throws NodeException
     */
    public static synchronized Node getHalfBodiesNode() throws NodeException {
        ProActiveRuntime defaultRuntime = null;
        ProActiveSecurityManager securityManager = null;
        if (halfBodiesNode == null) {
            try {
                defaultRuntime = RuntimeFactory.getDefaultRuntime();
                halfBodiesNode = defaultRuntime.createLocalNode(HALFBODIES_NODE_NAME +
                    halfbodyCounter.incrementAndGet(), false, securityManager, DEFAULT_VIRTUAL_NODE_NAME,
                        Job.DEFAULT_JOBID);
            } catch (ProActiveException e) {
                throw new NodeException("Cannot create the halfbodies hosting Node", e);
            } catch (AlreadyBoundException e) {
                // try another name
                getHalfBodiesNode();
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
     * @param jobId
     * 			A jobID or null
     * @return  the newly created node on the local JVM
     * @exception NodeException 
     * 			if the node cannot be created or if the nodeName is invalid
     */
    public static Node createLocalNode(String nodeName, boolean replacePreviousBinding,
            ProActiveSecurityManager psm, String vnname, String jobId) throws NodeException,
            AlreadyBoundException {
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
            return proActiveRuntime.createLocalNode(nodeName, replacePreviousBinding, psm, vnname, jobId);
        } catch (Exception e) {
            throw new NodeException("Failed to create a local node. name=" + nodeName, e);
        }
    }

    /** Creates a new node on the local ProActive runtime
     * 
     * The node URL can be in the form:
     * <ul>
     *  <li>nodeName</li>
     *  <li>//localhost/nodeName</li>
     *  <li>//<hostname>/nodeName</li>
     *  <li>protocol://hostname[:port]/nodeName</li>
     * </ul>
     * 
     * @param nodeURL the URL of the node to create
     * @return the newly created node on the local JVM
     * @exception NodeException if the node cannot be created
     * 
     * @deprecated replaced by {@link #createLocalNode(String, boolean, ProActiveSecurityManager, String, String)}
     */
    @Deprecated
    public static Node createNode(String nodeURL) throws NodeException, AlreadyBoundException {
        String nodeName = URIBuilder.getNameFromURI(nodeURL);
        return createLocalNode(nodeName, false, null, null, null);
    }

    /** Creates a new node on the local ProActive runtime
     * 
     * The node URL can be in the form:
     * <ul>
     *  <li>nodeName</li>
     *  <li>//localhost/nodeName</li>
     *  <li>//<hostname>/nodeName</li>
     *  <li>protocol://hostname[:port]/nodeName</li>
     * </ul>
     * 
     * @param nodeName the name of the node to create
     * @param replacePreviousBinding
     * @return the newly created node on the local JVM
     * @exception NodeException if the node cannot be created
     * 
     * @deprecated replaced by {@link #createLocalNode(String, boolean, ProActiveSecurityManager, String, String)}
     */
    @Deprecated
    public static Node createNode(String nodeURL, boolean replacePreviousBinding,
            ProActiveSecurityManager psm, String vnname, String jobId) throws NodeException,
            AlreadyBoundException {
        String nodeName = URIBuilder.getHostNameFromUrl(nodeURL);

        return createLocalNode(nodeName, replacePreviousBinding, psm, vnname, jobId);
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
        String jobID;

        if (logger.isDebugEnabled()) {
            logger.debug("NodeFactory: getNode() for " + nodeURL);
        }

        //do we have any association for this node?
        String protocol = URIBuilder.getProtocol(nodeURL);
        if (protocol == null) {
            protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        }

        //String noProtocolUrl = UrlBuilder.removeProtocol(nodeURL, protocol);
        try {
            //            url = URIBuilder.checkURI(nodeURL).toString();
            url = nodeURL; // #@#@ This modification can break proactive
            proActiveRuntime = RuntimeFactory.getRuntime(url);
            jobID = proActiveRuntime.getJobID(url);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get the node based on " + nodeURL, e);
        }

        Node node = new NodeImpl(proActiveRuntime, url, protocol, jobID);

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
