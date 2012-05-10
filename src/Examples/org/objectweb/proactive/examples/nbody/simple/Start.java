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
package org.objectweb.proactive.examples.nbody.simple;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.nbody.common.Cube;
import org.objectweb.proactive.examples.nbody.common.Deployer;
import org.objectweb.proactive.examples.nbody.common.Displayer;
import org.objectweb.proactive.examples.nbody.common.Planet;


/**
 * <P>
 * This starts the nbody example, on the most simple example. Every Active Object (Domains) is
 * associated to one single Planet. Synchronization is achieved by using another type of Active
 * Object, a Maestro, which waits for all Domains to finish the current iteration before asking
 * them to start the following one.
 * </P>
 *
 * @author The ProActive Team
 * @version 1.0,  2005/04
 * @since   ProActive 2.2
 */
public class Start {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) {
        org.objectweb.proactive.examples.nbody.common.Start.main(args);
    }

    public static void main(int totalNbBodies, int maxIter, Displayer displayer, Deployer deployer) {
        logger.info("RUNNING simplest VERSION");

        Cube universe = new Cube(-100, -100, -100, 200, 200, 200);
        Domain[] domainArray = new Domain[totalNbBodies];
        Node[] nodes = deployer.getWorkerNodes();

        for (int i = 0; i < totalNbBodies; i++) {
            Object[] constructorParams = new Object[] { new Integer(i), new Planet(universe) };
            try {
                // Create all the Domains used in the simulation 
                domainArray[i] = PAActiveObject.newActive(Domain.class, constructorParams, nodes[(i + 1) %
                    nodes.length]);
            } catch (ActiveObjectCreationException e) {
                deployer.abortOnError(e);
            } catch (NodeException e) {
                deployer.abortOnError(e);
            }
        }

        // Add the reference on the Domain to the deployer
        deployer.addAoReferences(domainArray);

        logger.info("[NBODY] " + totalNbBodies + " Planets are deployed");

        // Create a maestro, which will orchestrate the whole simulation, synchronizing the computations of the Domains
        Maestro maestro = null;
        try {
            maestro = PAActiveObject.newActive(Maestro.class, new Object[] { domainArray,
                    new Integer(maxIter), deployer }, nodes[0]);
        } catch (ActiveObjectCreationException e) {
            deployer.abortOnError(e);
        } catch (NodeException e) {
            deployer.abortOnError(e);
        }

        // Add the reference on the Maestro to the deployer
        deployer.addAoReference(maestro);

        // init workers
        for (int i = 0; i < totalNbBodies; i++)
            domainArray[i].init(domainArray, displayer, maestro);
    }
}
