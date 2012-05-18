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
package org.objectweb.proactive.extensions.nativeinterface;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.nativeinterface.coupling.InboundProxy;
import org.objectweb.proactive.extensions.nativeinterface.coupling.OutboundProxy;
import org.objectweb.proactive.extensions.nativeinterface.spmd.NativeSpmd;


public class ProActiveNativeManager implements Serializable {

    private static final long serialVersionUID = 52;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.NATIVE_CONTROL_MANAGER);
    public final static String LIBRARY_NAME = "ProActiveNativeComm";
    public final static String FULL_LIBRARY_NAME = "lib" + LIBRARY_NAME + ".so";

    /** number of jobs */
    private static int currentNumberOfJob = 0;

    /** list of NativeSpmd object */
    private List<NativeSpmd> spmdList;

    /*  Map<jobID, ProActiveCoupling []> */
    private Map<Integer, InboundProxy[]> inboundProxyArrayMap;

    /*  Map<jobID, PASPMD ProActiveNativeCoupling> */
    private Map<Integer, InboundProxy> inboundProxyMap;

    private Map<Integer, OutboundProxy> outboundProxyMap;

    /*  ackToStart[jobID] = number of proxy registered */
    private int[] ackToStart;

    /*  ackToRecvlist[jobID] = number of proxy ready to begin activities */
    private int[] ackToRecv;
    private boolean debugWaitForInit = false;
    private int jobReady = 0;
    private boolean finished = false;

    public ProActiveNativeManager() {
    }

    /**
     * Test whether the deployment is finished or not
     * @return true if the deployment is finished without errors
     */
    public boolean deploymentFinished() {
        return jobReady == currentNumberOfJob;
    }

    /**
     * Deploy and wrapp a list of SPMD GCMA
     * @param spmdList
     */
    public void deploy(List<NativeSpmd> spmdList) {
        this.spmdList = spmdList;
        this.inboundProxyArrayMap = new Hashtable<Integer, InboundProxy[]>();
        this.outboundProxyMap = new Hashtable<Integer, OutboundProxy>();
        this.inboundProxyMap = new Hashtable<Integer, InboundProxy>();
        this.ackToStart = new int[spmdList.size()];
        this.ackToRecv = new int[spmdList.size()];

        // loop on the NativeSpmd object list
        try {
            for (int i = 0; i < spmdList.size(); i++) {
                NativeSpmd spmd = spmdList.get(currentNumberOfJob);
                List<Node> nodes = spmd.getNodes();

                Node[] allNodes = new Node[nodes.size()];
                int k = 0;
                for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                    Node node = (Node) iterator.next();
                    allNodes[k] = node;
                    k++;
                }

                ClassLoader cl = this.getClass().getClassLoader();

                java.net.URL u = cl.getResource(this.getClass().getPackage().toString().replace('.', '/') +
                    FULL_LIBRARY_NAME);

                //else
                ackToStart[i] = allNodes.length - 1;
                ackToRecv[i] = allNodes.length - 1;
                Object[][] params = new Object[allNodes.length][];
                Object[][] params2 = new Object[allNodes.length][];

                // create parameters
                // "Comm" is the name of the JNI Library
                for (int j = 0; j < params.length; j++) {
                    params[j] = new Object[] { LIBRARY_NAME,
                            (ProActiveNativeManager) PAActiveObject.getStubOnThis(), currentNumberOfJob, j,
                            spmd.getFactory() };

                    params2[j] = new Object[] {};
                }

                if (logger.isInfoEnabled()) {
                    logger.info("[MANAGER] Create SPMD Proxy for jobID: " + currentNumberOfJob);
                }

                /***************************** INBOUND PROXIES CREATION ********************************/

                InboundProxy inboundProxyGroup = (InboundProxy) PASPMD.newSPMDGroup(InboundProxy.class
                        .getName(), params, allNodes);

                // create PASPMD proxy
                this.inboundProxyMap.put(currentNumberOfJob, inboundProxyGroup);

                /***************************** OUTBOUND PROXIES CREATION *******************************/

                OutboundProxy outboundProxyGroup = (OutboundProxy) PASPMD.newSPMDGroup(OutboundProxy.class
                        .getName(), params2, allNodes);

                this.outboundProxyMap.put(currentNumberOfJob, outboundProxyGroup);

                if (logger.isInfoEnabled()) {
                    logger.info("[MANAGER] Initialize remote environments");
                }

                // initialize queues & semaphores and start thread
                // Ack ack = spmdCouplingProxy.initEnvironment();
                // PAFuture.waitFor(ack);

                if (logger.isInfoEnabled()) {
                    logger.info("[MANAGER] Activate remote thread for communication");
                }

                // once environment is ready, start thread to get mpi process rank
                inboundProxyGroup.createRecvThread();
                // initialise joblist & and userProxyList table
                this.inboundProxyArrayMap.put(currentNumberOfJob, new InboundProxy[allNodes.length]);

                currentNumberOfJob++;
                //TODO Why don't we return a reference on the ProActiveMpiCoupling in order to avoid manipulate hazardous jobId ?
            }
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void register(int jobID, int rank) {
        // ack of corresponding job is null means that the
        // job is ready to recv message from another job
        if (logger.isInfoEnabled()) {
            logger.info("[MANAGER] JobID #" + jobID + " rank " + rank +
                "has notified its mpi interface is ready (" +
                (this.inboundProxyArrayMap.get(jobID).length - ackToRecv[jobID]) + "/" +
                this.inboundProxyArrayMap.get(jobID).length + ")");
        }

        // Mpi process of that rank has been initialised
        if (ackToRecv[jobID] == 0) {
            for (int i = 0; i < currentNumberOfJob; i++) {
                // we wait for all jobs to finish Mpi initialisation
                if (ackToRecv[i] != 0) {
                    return;
                }
            }
            for (int i = 0; i < currentNumberOfJob; i++) {
                ((InboundProxy) inboundProxyMap.get(i)).wakeUpThread();
            }
        } else {
            // we decrease the number of remaining ack to receive
            ackToRecv[jobID]--;
        }
    }

    // insert Comm Active Object at the correct location
    public void register(int jobID, int rank, InboundProxy activeProxyComm) {
        if (jobID < currentNumberOfJob) {
            InboundProxy[] mpiCouplingArray = ((InboundProxy[]) this.inboundProxyArrayMap.get(jobID));

            mpiCouplingArray[rank] = activeProxyComm;

            // test if this job is totally registered
            boolean deployUserSpmdObject = true;
            for (int i = 0; i < mpiCouplingArray.length; i++) {
                if (mpiCouplingArray[i] == null) {
                    // not totally registered
                    deployUserSpmdObject = false;
                }
            }

            /* If all jobs have finished */
            for (int i = 0; i < currentNumberOfJob; i++) {
                int jobListLength = ((InboundProxy[]) this.inboundProxyArrayMap.get(i)).length;
                for (int j = 0; j < jobListLength; j++) {
                    if (((InboundProxy[]) this.inboundProxyArrayMap.get(i))[j] == null) {
                        return;
                    }
                }
            }

            for (int i = 0; i < currentNumberOfJob; i++) {
                // send the table of User ProSpmd object to all the Proxy
                try {
                    InboundProxy[] tab = this.inboundProxyArrayMap.get(i);
                    OutboundProxy outboundProxyGroup = this.outboundProxyMap.get(i);
                    outboundProxyGroup.setInboundProxyReferences(this.inboundProxyArrayMap);
                    //TODO change to a group scatter parameter dispatch
                    for (int j = 0; j < tab.length; j++) {
                        OutboundProxy outboundProxyStub = (OutboundProxy) PAGroup.get(outboundProxyGroup, j);
                        tab[j]
                                .notifyProxy(this.inboundProxyArrayMap, this.inboundProxyMap,
                                        outboundProxyStub);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.debugWaitForInit = true;
        } else {
            throw new IndexOutOfBoundsException(" No Native job exists with num " + jobID);
        }
    }

    public boolean waitForInit() {
        return !this.debugWaitForInit;
    }

    public void notifyNativeInterfaceIsReady(int jobID) {
        // ack of job is null means we can start Native application
        if (logger.isInfoEnabled()) {
            logger.info("[MANAGER] JobID #" + jobID + " has notified its native interface is ready (" +
                (this.inboundProxyArrayMap.get(jobID).length - ackToStart[jobID]) + "/" +
                this.inboundProxyArrayMap.get(jobID).length + ")");
        }
        if (ackToStart[jobID] == 0) {
            NativeSpmd mpiSpmd = (NativeSpmd) spmdList.get(jobID);
            //            @SuppressWarnings("unused")
            //            NativeResult res = mpiSpmd.startNative();
            this.jobReady++;
            if (logger.isInfoEnabled()) {
                logger.info("[MANAGER] Start Native has been sent for JobID #" + jobID);
            }
        } else {
            ackToStart[jobID]--;
        }
    }

    public void unregister(int jobID, int rank) {
        if (jobID < currentNumberOfJob) {
            ((InboundProxy[]) this.inboundProxyArrayMap.get(jobID))[rank] = null;
            if (logger.isInfoEnabled()) {
                logger.info("[MANAGER] JobID #" + jobID + " unregister mpi process #" + rank);
            }
            for (int i = 0; i < currentNumberOfJob; i++) {
                int jobListLength = ((InboundProxy[]) this.inboundProxyArrayMap.get(i)).length;
                for (int j = 0; j < jobListLength; j++) {
                    if (((InboundProxy[]) this.inboundProxyArrayMap.get(i))[j] != null) {
                        return;
                    }
                }
            }

            finished = true;
        } else {
            throw new IndexOutOfBoundsException(" No Native job exists with num " + jobID);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void killVirtualNodes() {
        for (NativeSpmd spmd : this.spmdList) {
            try {
                spmd.getNodes().get(0).getProActiveRuntime().getVirtualNode(spmd.getName()).killAll(false);
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
