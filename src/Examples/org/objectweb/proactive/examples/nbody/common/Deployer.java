/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.examples.nbody.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Deployer {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    GCMApplication gcmad;
    GCMVirtualNode workers;

    /**
     * A list of remote references to terminate
     */
    public List<Object> referencesToTerminate;

    public Deployer() {
        // No args constructor 
    }

    public Deployer(File applicationDescriptor) {
        try {
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
            gcmad.startDeployment();
            workers = gcmad.getVirtualNode("Workers");
            this.referencesToTerminate = new ArrayList<Object>();
        } catch (ProActiveException e) {
            logger.error("Cannot load GCM Application Descriptor: " + applicationDescriptor, e);
        }
    }

    public Node[] getWorkerNodes() {
        if (workers == null)
            return null;

        logger.info("Waiting Workers virtual node becomes ready");
        workers.waitReady();
        return workers.getCurrentNodes().toArray(new Node[0]);
    }

    /**
     * Adds an active object reference;
     * Reference must be an instance of <code>StubObject</code>
     * @param aoReference An active object reference
     */
    public void addAoReference(Object aoReference) {
        if (aoReference instanceof StubObject) {
            this.referencesToTerminate.add(aoReference);
        }
    }

    /**
     * Reference must be an instance of <code>StubObject</code>
     * @param aoReferences An active object reference
     */
    public void addAoReferences(Object[] aoReferences) {
        for (Object aoReference : aoReferences) {
            this.addAoReference(aoReference);
        }
    }

    /**
     * Terminates all known active objects.
     * Terminating active objects is important because of the migrations
     * they are not necessarily on the deployed resources 
     * @param immediate if this boolean is <code>true</code>, the termination is then synchronous; <code>false</code> otherwise.     
     */
    public void terminateAll(boolean immediate) {
        for (Object aoReference : this.referencesToTerminate) {
            PAActiveObject.terminateActiveObject(aoReference, immediate);
        }
        this.referencesToTerminate.clear();
    }

    /**
     * Terminates all known active objects and shuts down all deployed resources.
     * @param immediate if this boolean is <code>true</code>, the termination is then synchronous; <code>false</code> otherwise.
     */
    public void terminateAllAndShutdown(boolean immediate) {
        this.terminateAll(immediate);
        this.shutdown();
    }

    public void shutdown() {
        this.gcmad.kill();
        PALifeCycle.exitSuccess();
    }

    public void abortOnError(Exception e) {
        logger.error("Abort on errror", e);
        this.terminateAllAndShutdown(false);
    }
}
