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
package org.objectweb.proactive.examples.nbody.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.mop.StubObject;
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
    private GCMVirtualNode workers;

    /**
     * A list of remote references to terminate
     */
    private List<Object> referencesToTerminate;

    public Deployer() {
        // No args constructor 
    }

    public Deployer(GCMApplication gcmad, GCMVirtualNode workers) {
        this.gcmad = gcmad;
        this.workers = workers;
        this.referencesToTerminate = new ArrayList<Object>();
    }

    public Node[] getWorkerNodes() {
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
