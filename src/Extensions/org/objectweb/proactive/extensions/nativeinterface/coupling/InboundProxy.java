/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.nativeinterface.coupling;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.nativeinterface.ProActiveNativeManager;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeApplicationFactory;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessage;


/**
 *  Inplements a message receiver proxy
 */
public class InboundProxy implements Serializable, InitActive {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;

    /** global Manager*/
    private ProActiveNativeManager manager;

    /** Comm object it refers */
    private ProActiveNativeInterface target;

    // job # managed by the Job Manager
    private int jobID;
    private static Map<Integer, InboundProxy[]> proxyMap;

    ////////////////////////////////
    //// CONSTRUCTOR METHODS    ////
    ////////////////////////////////
    public InboundProxy() {
    }

    /**
     * Create a message receiver
     * @param libName name of the library that implements the native side
     * @param manager central point that holds wrappers references
     * @param jobNum hierarchical identifier that distinguish the different group of processes
     * @param pa_rank primary id of the receiver
     * @param factory NativeApplicationFactory that will provide the implementation of message handler and adaper
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public InboundProxy(String libName, ProActiveNativeManager manager, Integer jobNum, Integer pa_rank,
            NativeApplicationFactory factory) throws ActiveObjectCreationException, NodeException,
            ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.manager = manager;
        this.jobID = jobNum.intValue();
        target = new ProActiveNativeInterface(libName, PAActiveObject.getBodyOnThis().getID().hashCode(),
            factory);
    }

    public void initActivity(Body body) {
        this.target.setMyProxy((InboundProxy) PAActiveObject.getStubOnThis(), this.manager, this.jobID);
    }

    ///////////////////////////////
    ////  PROXY OUTING METHODS ////
    ///////////////////////////////
    public void register(int rank) {
        this.manager.register(this.jobID, rank, (InboundProxy) PAActiveObject.getStubOnThis());
    }

    public void nativeInterfaceReady() {
        this.manager.notifyNativeInterfaceIsReady(this.jobID);
    }

    public void unregisterProcess(int rank) {
        this.manager.unregister(this.jobID, rank);
    }

    public void receiveFromNative(NativeMessage m_r) {
        this.target.receiveFromNative(m_r);
    }

    /////////////////////////////////
    ////  PROXY ENTERING METHODS ////
    /////////////////////////////////
    //    public Ack initEnvironment() {
    //        return new Ack();
    //    }

    public void createRecvThread() {
        this.target.createRecvThread();
    }

    public void notifyProxy(Map<Integer, InboundProxy[]> jobList, Map<Integer, InboundProxy> groupList,
            OutboundProxy outboundProxy) {
        proxyMap = jobList;
        this.target.sendJobNumberAndRegister(proxyMap.size());
        this.target.setOutboundProxy(outboundProxy);
    }

    public void wakeUpThread() {
        this.target.wakeUpThread();
    }

    ///////////////////////////
    ////  GETTER METHODS   ////
    ///////////////////////////
    public Node getNode() throws NodeException {
        return NodeFactory.getNode(PAActiveObject.getBodyOnThis().getNodeURL());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(target.toString()).append("\n NativeJobNum: " + this.jobID);
        return sb.toString();
    }
}
