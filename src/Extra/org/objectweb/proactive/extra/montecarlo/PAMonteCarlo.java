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
package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.core.EngineTaskAdapter;
import org.objectweb.proactive.extra.montecarlo.core.MCMemoryFactory;
import org.objectweb.proactive.annotation.PublicAPI;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;


/**
 * PAMonteCarlo
 *
 * Entry point for the Monte-Carlo framework.
 * The framework is based on a master-worker architecture, where the master will handle task scheduling,
 * collection of results, and the workers will compute simulations.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class PAMonteCarlo<T extends Serializable> {

    ProActiveMaster<EngineTaskAdapter<T>, T> master = null;

    /**
     * Initialize the Monte-Carlo toolkit by giving a descriptor and a virtual node name.
     * The workers will be created upon any resource produced by the given virtual node
     *
     * @param descriptorURL url of a descriptor
     * @param workersVNName virtual node name corresponding to workers
     * @throws ProActiveException
     */
    public PAMonteCarlo(URL descriptorURL, String workersVNName) throws ProActiveException {
        this(descriptorURL, workersVNName, null, null);
    }

    /**
     * Initialize the Monte-Carlo toolkit by giving a descriptor and two virtual node names.
     * Workers will be instanciated on resources created by the first one.
     * The second one should create one single resource. The master will be deployed on that resource
     *
     * @param descriptorURL url of a descriptor
     * @param masterVNName virtual node name corresponding to the master
     * @param workersVNName virtual node name corresponding to workers
     * @throws ProActiveException
     */
    public PAMonteCarlo(URL descriptorURL, String workersVNName, String masterVNName)
            throws ProActiveException {
        this(descriptorURL, masterVNName, workersVNName, null);
    }

    /**
     * Initialize the Monte-Carlo toolkit by giving a descriptor and two virtual node names.
     * Workers will be instanciated on resources created by the first one.
     * The second one should create one single resource. The master will be deployed on that resource
     * The last parameter is a Class object holding the definition of the RandomStream to use.
     * By default, the generator in use is the MRG32k3a one.
     *
     * @param descriptorURL url of a descriptor
     * @param masterVNName virtual node name corresponding to the master
     * @param workersVNName virtual node name corresponding to workers
     * @param randomStreamClass Random Number Generator class that workers will be using
     * @throws ProActiveException
     */
    public PAMonteCarlo(URL descriptorURL, String workersVNName, String masterVNName, Class randomStreamClass)
            throws ProActiveException {
        if (masterVNName != null) {
            // Remote master
            master = new ProActiveMaster<EngineTaskAdapter<T>, T>(descriptorURL, masterVNName,
                new MCMemoryFactory(randomStreamClass));
        } else {
            // Local master
            master = new ProActiveMaster<EngineTaskAdapter<T>, T>(new MCMemoryFactory(randomStreamClass));
        }
        master.addResources(descriptorURL, workersVNName);
        master.setResultReceptionOrder(SubMaster.SUBMISSION_ORDER);
    }

    /**
     * Runs the simulation by giving the definition of a top-level task.
     * This task is not directly a Monte Carlo simulation, but should rather contain the code of your general algorithm.
     * The top-level task can submit nested general tasks or nested Monte-Carlo simulations (Experience Set).
     * @param toplevelTask top-level task
     * @return the result of the top-level task
     * @throws TaskException if an exception occured during the execution of the top-level task.
     */
    public T run(EngineTask<T> toplevelTask) throws TaskException {
        ArrayList<EngineTaskAdapter<T>> singletask = new ArrayList<EngineTaskAdapter<T>>(1);
        singletask.add(new EngineTaskAdapter<T>(toplevelTask));
        master.solve(singletask);
        return master.waitOneResult();
    }

    /**
     * Terminates the Monte-Carlo toolkit and free created resources.
     */
    public void terminate() {
        master.terminate(true);
    }

}
