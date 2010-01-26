/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.scilab;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * MSDeployEngine contains all methods to deploy Scilab Engines from
 * a deployment descriptor
 * @author The ProActive Team
 *
 */
public class MSDeployEngine {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_DEPLOY);
    private static HashMap<String, Node> mapNode = new HashMap<String, Node>(); // List of deployed VNs 

    /**
     * @param pathDescriptor
     * @return list of virtual node contained in the deployment descriptor
     */
    public static String[] getListVirtualNode(String pathDescriptor) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSDeployEngine In:getListVirtualNode:" + pathDescriptor);
        }

        GCMApplication desc;
        Map<String, GCMVirtualNode> arrayVn;
        String[] arrayNameVn = null;
        try {
            desc = PAGCMDeployment.loadApplicationDescriptor(new File(pathDescriptor));
            arrayVn = desc.getVirtualNodes();
            arrayNameVn = arrayVn.keySet().toArray(new String[] {});
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        return arrayNameVn;
    }

    public static synchronized int getNbMappedNodes(String nameVirtualNode, String pathDescriptor) {
        GCMApplication desc;
        GCMVirtualNode vn;
        try {
            desc = PAGCMDeployment.loadApplicationDescriptor(new File(pathDescriptor));
            vn = desc.getVirtualNode(nameVirtualNode);
            return (int) vn.getNbRequiredNodes();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @param nameVirtualNode
     * @param pathDescriptor
     * @param arrayIdEngine
     * @return HashMap of deployed Scilab Engines
     */
    public synchronized static HashMap<String, MSEngine> deploy(String nameVirtualNode,
            String pathDescriptor, String[] arrayIdEngine) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSDeployEngine In:deploy:" + pathDescriptor);
        }

        GCMApplication desc;
        GCMVirtualNode vn;
        List<Node> nodes;
        MSEngine mSEngine;
        HashMap<String, MSEngine> mapEngine = new HashMap<String, MSEngine>();

        try {
            desc = PAGCMDeployment.loadApplicationDescriptor(new File(pathDescriptor));
            desc.startDeployment();
            vn = desc.getVirtualNode(nameVirtualNode);
            vn.waitReady();
            nodes = vn.getCurrentNodes();

            int length = Math.min(nodes.size(), arrayIdEngine.length);

            for (int i = 0; i < length; i++) {
                mSEngine = deploy(arrayIdEngine[i], nodes.get(i));
                mapEngine.put(arrayIdEngine[i], mSEngine);
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        return mapEngine;
    }

    /**
     *
     * @param idEngine
     * @param currentNode
     * @return a Scilab Engine deployed on the current node
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    private synchronized static MSEngine deploy(String idEngine, Node currentNode)
            throws ActiveObjectCreationException, NodeException {
        Object[] param = new Object[] { idEngine };
        MSEngine mSEngine = (MSEngine) PAActiveObject.newActive(MSEngine.class.getName(), param, currentNode);
        mapNode.put(idEngine, currentNode);
        mSEngine.setImmediateServices();
        return mSEngine;
    }

    /**
     *
     * @param idEngine
     * @return a local Scilab Engine
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static MSEngine deploy(String idEngine) throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSDeployEngine In:deploy");
        }

        Object[] param = new Object[] { idEngine };
        MSEngine mSEngine = (MSEngine) PAActiveObject.newActive(MSEngine.class.getName(), param);
        mSEngine.setImmediateServices();
        return mSEngine;
    }

    public static Node getEngineNode(String idEngine) {
        return mapNode.get(idEngine);
    }
}
