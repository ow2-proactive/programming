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
package org.objectweb.proactive.extra.montecarlo;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.core.EngineTaskAdapter;
import org.objectweb.proactive.extra.montecarlo.core.MCMemoryFactory;


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

    //@snippet-start montecarlo_constructor
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
    public PAMonteCarlo(URL descriptorURL, String workersVNName, String masterVNName,
            Class<?> randomStreamClass) throws ProActiveException
    //@snippet-end montecarlo_constructor
    {
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

    //@snippet-start montecarlo_run
    /**
     * Runs the simulation by giving the definition of a top-level task.
     * This task is not directly a Monte Carlo simulation, but should rather contain the code of your general algorithm.
     * The top-level task can submit nested general tasks or nested Monte-Carlo simulations (Experience Set).
     * @param toplevelTask top-level task
     * @return the result of the top-level task
     * @throws TaskException if an exception occured during the execution of the top-level task.
     */
    public T run(EngineTask<T> toplevelTask) throws TaskException
    //@snippet-end montecarlo_run
    {
        ArrayList<EngineTaskAdapter<T>> singletask = new ArrayList<EngineTaskAdapter<T>>(1);
        singletask.add(new EngineTaskAdapter<T>(toplevelTask));
        master.solve(singletask);
        return master.waitOneResult();
    }

    //@snippet-start montecarlo_terminate
    /**
     * Terminates the Monte-Carlo toolkit and free created resources.
     */
    public void terminate()
    //@snippet-end montecarlo_terminate
    {
        master.terminate(true);
    }

}
