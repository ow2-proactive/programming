/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessage;


/**
 *  Inplements a message sender proxy
 */
public class OutboundProxy implements Serializable, InitActive {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    Map<Integer, InboundProxy[]> inboundProxyMap;
    private InboundProxy groupComm;

    //Debug timers
    private long acc_send_to_native_timer = 0;
    private int nb_call = 0;

    public OutboundProxy() {

    }

    public void initActivity(Body body) {

        acc_send_to_native_timer = 0;
        nb_call = 0;
        try {
            groupComm = (InboundProxy) PAGroup.newGroup(InboundProxy.class.getName());
        } catch (ClassNotReifiableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * send message to native process
     * @param m_r native mesage containing data to be sent and recipient process identification
     */
    public void sendToNative(NativeMessage m_r) {
        int rank = m_r.getDestRank();
        int jobId = m_r.getDestJobId();
        long start = System.currentTimeMillis();
        // async call with rendez-vous to the target, we pay network latency here
        getTarget(jobId, rank).receiveFromNative(m_r);
        acc_send_to_native_timer += (System.currentTimeMillis() - start);
        nb_call++;
    }

    /**
     * Get a target wrapper object based on hierarchical identification
     * @param jobId hierarchical id
     * @param rank primary id
     * @return
     */
    private InboundProxy getTarget(int jobId, int rank) {
        if (jobId < inboundProxyMap.size()) {
            InboundProxy[] arrayComm = (InboundProxy[]) inboundProxyMap.get(jobId);
            if ((rank < arrayComm.length) && (arrayComm[rank] != null)) {
                return arrayComm[rank];
            } else {
                throw new IndexOutOfBoundsException(" ActiveProxyComm destinator " + rank +
                    " is unreachable!");
            }
        }

        throw new IndexOutOfBoundsException(" No Native job exists with num " + jobId);
    }

    /**
     * Send a group of messages in parallel by bulding a group communication
     * @param messages
     */
    public void sendToNativeInParallel(NativeMessage[] messages) {
        // cleaning previous invocation
        Group<InboundProxy> g = PAGroup.getGroup(groupComm);
        g.clear();

        // creating parameter group to dispatch
        NativeMessage parameterGroup;

        try {
            parameterGroup = (NativeMessage) PAGroup.newGroup(messages[0].getClass().getName());
            PAGroup.setScatterGroup(parameterGroup);

            Group<NativeMessage> pg = PAGroup.getGroup(parameterGroup);

            // building scattered group and adding parameters to group parameter
            int i = 0;
            while (i < messages.length) {
                InboundProxy target = getTarget(messages[i].getDestJobId(), messages[i].getDestRank());
                pg.add(messages[i]);
                g.add(target);
                i++;
            }

            // invoking
            this.groupComm.receiveFromNative(parameterGroup);

            // waiting for communication to complete
            g.waitAll();

            PAGroup.unsetScatterGroup(parameterGroup);

        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void unregisterProcess(int rank) {
        System.out.println("timer send_to_native through AO milli " + acc_send_to_native_timer + " moy " +
            ((nb_call != 0) ? (acc_send_to_native_timer / nb_call) : 0));
    }

    public void setInboundProxyReferences(Map<Integer, InboundProxy[]> inboundProxyMap2) {
        inboundProxyMap = inboundProxyMap2;
    }

}
