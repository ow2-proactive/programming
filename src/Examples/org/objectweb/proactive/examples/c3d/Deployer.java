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
package org.objectweb.proactive.examples.c3d;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Deployer {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    GCMApplication gcmad;
    GCMVirtualNode renderer;
    GCMVirtualNode dispatcher;

    public Deployer() {
        // No args constructor
    }

    public Deployer(File applicationDescriptor) {
        try {
            ProActiveConfiguration.load();
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
            gcmad.startDeployment();
            renderer = gcmad.getVirtualNode("Renderer");
            dispatcher = gcmad.getVirtualNode("Dispatcher");
        } catch (ProActiveException e) {
            logger.error("Cannot load GCM Application Descriptor: " + applicationDescriptor, e);
        }
    }

    public Node[] getRendererNodes() {
        if (renderer == null)
            return null;

        logger.info("Waiting Renderer virtual node to be ready");
        renderer.waitReady();
        return renderer.getCurrentNodes().toArray(new Node[0]);
    }

    public Node getDispatcherNode() {
        if (dispatcher == null)
            return null;

        logger.info("Waiting Dispatcher virtual node to be ready");
        dispatcher.waitReady();
        return dispatcher.getANode();
    }

    public void shutdown() {
        gcmad.kill();
    }

    public void abortOnError(Exception e) {
        logger.error("Abort on errror", e);
        shutdown();
    }
}
