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
package org.objectweb.proactive.examples.webservices.c3dWS;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


@ActiveObject
public class Deployer {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    private GCMApplication gcmad;
    private GCMVirtualNode renderer;
    private GCMVirtualNode dispatcher;

    public Deployer() {
        // No args constructor
    }

    public Deployer(GCMApplication gcmad, GCMVirtualNode renderer, GCMVirtualNode dispatcher) {
        this.gcmad = gcmad;
        this.renderer = renderer;
        this.dispatcher = dispatcher;
    }

    public Node[] getRendererNodes() {
        logger.debug("Waiting Renderer virtual node to be ready");
        renderer.waitReady();
        return renderer.getCurrentNodes().toArray(new Node[0]);
    }

    public Node getDispatcherNode() {
        logger.debug("Waiting Dispatcher virtual node to be ready");
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
