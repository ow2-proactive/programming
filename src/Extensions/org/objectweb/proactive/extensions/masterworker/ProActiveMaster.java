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
package org.objectweb.proactive.extensions.masterworker;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.masterworker.core.AOMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;
import org.objectweb.proactive.extensions.masterworker.interfaces.MemoryFactory;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * Entry point of the Master/Worker API.<br/>
 * Here is how the Master/Worker API is used :
 * <ol>
 * <li>Create a ProActiveMaster object through the different constructors</li>
 * <li>Submit tasks through the use of the <b><i>solve</i></b> methods</li>
 * <li>Collect results through the <b><i>wait</i></b> methods</li>
 * </ol>
 * <br/>
 * The <b><i>WorkerMemory</i></b> concept is meant to allow user to store information directly inside the workers where tasks are executed. <br/>
 * The WorkerMemory has the same structure as a Dictionary with &lt;key, value&gt; pairs where keys are string and values are any Java object. <br/>
 * <br/>
 * A user can specify, when creating the master, the initial memory that every worker will have by providing a Map of &lt;String,Object&gt; pairs to the ProActiveMaster constructors.<br/>
 * <br/>
 * When tasks will later on be executed on the workers, the tasks will be able to access this memory through the worker memory parameter of the <b><i>run</i></b> method.
 * <br/>
 * The results can be received using two different reception order modes: <br/>
 * <ul>
 * <li>In the <b><i>CompletionOrder mode</i></b>, which is the default, results are received in the same order as they are completed by the workers (i.e. order is unspecified).</li>
 * <li>In the <b><i>SubmissionOrder mode</i></b>, results are received in the same order as they are submitted to the master.</li>
 * </ul>
 * <br/>
 *
 * @author The ProActive Team
 * @param <T> Task of result R
 * @param <R> Result Object
 * @see org.objectweb.proactive.extensions.masterworker.interfaces.Task
 * @see org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory
 * @see org.objectweb.proactive.extensions.masterworker.interfaces.Master
 */
@PublicAPI
public class ProActiveMaster<T extends Task<R>, R extends Serializable> implements Master<T, R>, Serializable {

    private static final long serialVersionUID = 52;

    protected ProActiveMaster<T, R> activeThis = null;

    protected AOMaster aomaster = null;

    /** Creates a local master (you can add resources afterwards) */
    public ProActiveMaster()
    // try comment
    {
        this(new ConstantMemoryFactory());
    }

    //@snippet-start masterworker_constructor
    /**
     * Creates a remote master that will be created on top of the given Node <br>
     * Resources can be added to the master afterwards
     *
     * @param remoteNodeToUse this Node will be used to create the remote master
     */
    public ProActiveMaster(Node remoteNodeToUse)
    //@snippet-end masterworker_constructor
    {
        this(remoteNodeToUse, new ConstantMemoryFactory());
    }

    //@snippet-start masterworker_constructor_remote
    /**
     * Creates an empty remote master that will be created on top of the given Node with an initial worker memory
     *
     * @param remoteNodeToUse this Node will be used to create the remote master
     * @param memoryFactory factory which will create memory for each new workers
     */
    public ProActiveMaster(Node remoteNodeToUse, MemoryFactory memoryFactory)
    //@snippet-end masterworker_constructor_remote
    {
        try {
            aomaster = (AOMaster) PAActiveObject.newActive(AOMaster.class.getName(), new Object[] {
                    memoryFactory, null, null, null }, remoteNodeToUse);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates an empty local master with an initial worker memory
     *
     * @param memoryFactory factory which will create memory for each new workers
     */
    public ProActiveMaster(MemoryFactory memoryFactory) {
        try {
            aomaster = (AOMaster) PAActiveObject.newActive(AOMaster.class.getName(), new Object[] {
                    memoryFactory, null, null, null });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates an empty remote master with the URL of a descriptor and the name of a virtual node
     * The master will be created on top of a single resource deployed by this virtual node
     *
     * @param descriptorURL url of the ProActive descriptor
     * @param masterVNName  name of the virtual node to deploy inside the ProActive descriptor
     */
    public ProActiveMaster(URL descriptorURL, String masterVNName) {
        this(descriptorURL, masterVNName, new ConstantMemoryFactory());
    }

    /**
     * Creates an empty remote master with the URL of a descriptor and the name of a virtual node
     * The master will be created on top of a single resource deployed by this virtual node
     *
     * @param descriptorURL url of the ProActive descriptor
     * @param vContract {@link VariableContract} to use when parsing the descriptor
     * @param masterVNName  name of the virtual node to deploy inside the ProActive descriptor
     */
    public ProActiveMaster(URL descriptorURL, VariableContractImpl vContract, String masterVNName) {
        this(descriptorURL, vContract, masterVNName, new ConstantMemoryFactory());
    }

    /**
     * Creates an empty remote master with the URL of a descriptor and the name of a virtual node
     * The master will be created on top of a single resource deployed by this virtual node
     *
     * @param descriptorURL url of the ProActive descriptor
     * @param masterVNName  name of the virtual node to deploy inside the ProActive descriptor
     * @param memoryFactory factory which will create memory for each new workers
     */
    public ProActiveMaster(URL descriptorURL, String masterVNName, MemoryFactory memoryFactory) {
        this(descriptorURL, null, masterVNName, memoryFactory);
    }

    /**
     * Creates an empty remote master with the URL of a descriptor and the name of a virtual node
     * The master will be created on top of a single resource deployed by this virtual node
     *
     * @param descriptorURL url of the ProActive descriptor
     * @param vContract {@link VariableContract} to use when parsing the descriptor
     * @param masterVNName  name of the virtual node to deploy inside the ProActive descriptor
     * @param memoryFactory factory which will create memory for each new workers
     */
    public ProActiveMaster(URL descriptorURL, VariableContractImpl vContract, String masterVNName,
            MemoryFactory memoryFactory) {
        try {
            GCMApplication pad = PAGCMDeployment.loadApplicationDescriptor(descriptorURL, vContract);

            GCMVirtualNode masterVN = pad.getVirtualNode(masterVNName);
            pad.startDeployment();
            masterVN.waitReady();

            Node masterNode = masterVN.getANode();

            aomaster = (AOMaster) PAActiveObject.newActive(AOMaster.class.getName(), new Object[] {
                    memoryFactory, descriptorURL, pad, masterVNName }, masterNode);

        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    public void addResources(Collection<Node> nodes) {
        aomaster.addResources(nodes);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ProActiveException
     */
    public void addResources(URL descriptorURL) throws ProActiveException {
        aomaster.addResources(descriptorURL);
    }

    /** {@inheritDoc} */
    public void addResources(URL descriptorURL, VariableContract contract) throws ProActiveException {
        aomaster.addResources(descriptorURL, contract);
    }

    /** {@inheritDoc} */
    public void addResources(URL descriptorURL, VariableContract contract, String virtualNodeName)
            throws ProActiveException {
        aomaster.addResources(descriptorURL, contract, virtualNodeName);
    }

    /** {@inheritDoc} */
    public void addResources(String schedulerURL, String user, String password, String[] classpath)
            throws ProActiveException {
        aomaster.addResources(schedulerURL, user, password, classpath);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ProActiveException
     */
    public void addResources(URL descriptorURL, String virtualNodeName) throws ProActiveException {
        aomaster.addResources(descriptorURL, virtualNodeName);
    }

    /** {@inheritDoc} */
    public int countAvailableResults() {
        return aomaster.countAvailableResults(null);
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return aomaster.isEmpty(null);
    }

    /** {@inheritDoc} */
    public void setPingPeriod(long periodMillis) {
        aomaster.setPingPeriod(periodMillis);
    }

    /** {@inheritDoc} */
    public void setResultReceptionOrder(
            org.objectweb.proactive.extensions.masterworker.interfaces.Master.OrderingMode mode) {
        aomaster.setResultReceptionOrder(null, mode);
    }

    /** {@inheritDoc} */
    public void setInitialTaskFlooding(int number_of_tasks) {
        aomaster.setInitialTaskFlooding(number_of_tasks);
    }

    /** {@inheritDoc} */
    public int workerpoolSize() {
        return aomaster.workerpoolSize();
    }

    /** {@inheritDoc} */

    public void solve(List<T> tasks) {
        if (tasks.size() == 0) {
            throw new IllegalArgumentException("empty list");
        }
        aomaster.solveIntern(null, tasks);
    }

    /** {@inheritDoc} */
    public void terminate(boolean freeResources) {
        // we use here the synchronous version
        aomaster.terminateIntern(freeResources);
        aomaster.awaitsTermination();
        aomaster = null;

    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<R> waitAllResults() throws TaskException {
        if (aomaster.isEmpty(null)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }
        List<R> results = null;
        try {
            results = (List<R>) PAFuture.getFutureValue(aomaster.waitAllResults(null));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return results;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<R> waitKResults(int k) throws IllegalStateException, IllegalArgumentException, TaskException {
        if (aomaster.countPending(null) < k) {
            throw new IllegalStateException("Number of tasks submitted previously is strictly less than " +
                k + ": call to this method will wait forever");
        }

        List<R> results = null;
        try {
            results = (List<R>) PAFuture.getFutureValue(aomaster.waitKResults(null, k));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return results;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public R waitOneResult() throws TaskException {
        if (aomaster.isEmpty(null)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }
        R result = null;
        try {
            result = (R) PAFuture.getFutureValue(aomaster.waitOneResult(null));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public List<R> waitSomeResults() throws TaskException {
        if (aomaster.isEmpty(null)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }

        List<R> results = null;
        try {
            results = (List<R>) PAFuture.getFutureValue(aomaster.waitSomeResults(null));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return results;
    }

    public void clear() {
        aomaster.clear();
    }

    private Throwable findTaskException(Throwable e) {
        if (e instanceof TaskException) {
            return e;
        }
        if (e.getCause() != null) {
            return findTaskException(e.getCause());
        } else {
            return null;
        }
    }

}
