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
package org.objectweb.proactive.extensions.nativeinterfacempi;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessage;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageHandler;
import org.objectweb.proactive.extensions.nativeinterface.coupling.ProActiveNativeConstants;
import org.objectweb.proactive.extensions.nativeinterface.coupling.ProActiveNativeInterface;


/**
 *  MPI implementation of message handler
 *  implements mpi <-> java communication + collective operations
 */
public class ProActiveMPIMessageHandler implements NativeMessageHandler {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.NATIVE_CONTROL_COUPLING);
    private String prefix;
    private long scatter_timer = 0;
    private long bcast_timer = 0;
    private long scatter_extract = 0;

    public ProActiveMPIMessageHandler() {
        this.prefix = "[MPI_MSG_HANDLER]";
    }

    public boolean handleMessage(ProActiveNativeInterface callback, NativeMessage message) {

        ProActiveMPIMessage m_r = (ProActiveMPIMessage) message;
        try {

            // check msg_type
            if (m_r.getMsgType() == ProActiveNativeConstants.COMM_MSG_INIT) {
                callback.setRank(m_r.getSrcRank());

                callback.asleepThread();
            } else if (m_r.getMsgType() == ProActiveNativeConstants.COMM_MSG_SEND) {
                if (logger.isDebugEnabled()) {
                    logger.debug(prefix + " [START] sending message to remote mpi\n" + m_r.toString(prefix));
                }

                callback.sendToNative(m_r);
            } else if (m_r.getMsgType() == ProActiveNativeConstants.COMM_MSG_SCATTER) {
                if (logger.isDebugEnabled()) {
                    logger.debug(prefix + " [START] Scatter message processing\n" + m_r.toString(prefix));
                }

                performScatter(callback, m_r);

                if (logger.isDebugEnabled()) {
                    logger.debug(prefix + " [END]  Scatter message processing\n" + m_r.toString(prefix));
                }

            } else if (m_r.getMsgType() == ProActiveNativeConstants.COMM_MSG_BCAST) {
                if (logger.isDebugEnabled()) {
                    logger.debug(prefix + " [START] Bcast message processing\n" + m_r.toString(prefix));
                }

                performBcast(callback, m_r);

                if (logger.isDebugEnabled()) {
                    logger.debug(prefix + " [END]  Bcast message processing\n" + m_r.toString(prefix));
                }

            } else if (m_r.getMsgType() == ProActiveNativeConstants.COMM_MSG_FINALIZE) {
                System.out.println("scatter_timer" + scatter_timer);
                System.out.println("scatter_extract " + scatter_extract);
                System.out.println("bcast_timer " + bcast_timer);
                return false;
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(prefix + " msg_type UNKNOWN  " + m_r.toString(prefix));
                }
            }
        } catch (Exception e) {
            System.out.println("In Java:\n\t" + e);
            if (logger.isDebugEnabled()) {
                logger.debug(prefix + " exception in Message recv handler " + e);
            }
            e.printStackTrace();
        }
        return true;
    }

    private void performScatter(ProActiveNativeInterface callback, ProActiveMPIMessage m_r) {
        int i = 0;
        byte[] serializedBytes = m_r.getSerializedMessage();
        int nb_send = ProActiveMPIMessageAdapter.extract_scatter_nb_send(serializedBytes);
        ProActiveMPIMessage[] messages = new ProActiveMPIMessage[nb_send];

        long start = System.currentTimeMillis();

        while (i < nb_send) {
            messages[i] = ProActiveMPIMessageAdapter.extract_msg_from_scatter(serializedBytes, i);
            i++;
        }
        scatter_extract += (System.currentTimeMillis() - start);

        start = System.currentTimeMillis();

        callback.sendToNativeInParallel(messages);

        scatter_timer += (System.currentTimeMillis() - start);
    }

    private void performBcast(ProActiveNativeInterface callback, ProActiveMPIMessage m_r) {
        int i = 0;
        byte[] serializedBytes = m_r.getSerializedMessage();
        int nb_send = ProActiveMPIMessageAdapter.extract_bcast_nb_send(serializedBytes);
        ProActiveMPIMessage[] messages = new ProActiveMPIMessage[nb_send];

        long start = System.currentTimeMillis();

        while (i < nb_send) {
            messages[i] = ProActiveMPIMessageAdapter.extract_msg_from_bcast(serializedBytes, i);
            i++;
        }

        callback.sendToNativeInParallel(messages);

        bcast_timer += (System.currentTimeMillis() - start);
    }

}