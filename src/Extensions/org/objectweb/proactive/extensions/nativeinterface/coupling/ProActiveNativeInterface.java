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
package org.objectweb.proactive.extensions.nativeinterface.coupling;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.nativeinterface.ProActiveNativeManager;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeApplicationFactory;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessage;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageAdapter;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageHandler;


/**
 * Main implementation of a native interface, containing proxies and message handlers
 */
public class ProActiveNativeInterface {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.NATIVE_CONTROL_COUPLING);
    private String hostname = "NULL";
    private String prefix = "";

    private volatile boolean shouldRun = true;

    private InboundProxy aoMyProxy;

    private boolean notify = true;

    private int nativeRank = -1;
    private int nativeJobID;

    /* job manager */
    private ProActiveNativeManager manager;
    private NativeInterface nativeInterface;
    private NativeMessageAdapter msgAdapter;
    private NativeMessageHandler msgHandler;
    private long timer_send_to_native = 0;
    private int timer_send_to_native_nb_call = 0;
    private long timer_send_debug = 0;
    //	private long msg_acc = 0;
    private OutboundProxy outboundProxy = null;

    /**
     * Send information regarding current job to the native code.
     * @param jobNumber The current jobId
     * @param nbJob The number of processes involved in this job.
     * @return
     */

    ////////////////////////////////
    //// CONSTRUCTOR METHODS    ////
    ////////////////////////////////
    public ProActiveNativeInterface() {
    }

    /**
     * Creates a native interface
     * @param libName native library responsible for handling Java<->native communication,
     * implementing @link {@link org.objectweb.proactive.extensions.nativeinterface.coupling.NativeInterface}
     * @param uniqueID
     * @param factory
     */
    public ProActiveNativeInterface(String libName, int uniqueID, NativeApplicationFactory factory) {
        hostname = ProActiveInet.getInstance().getInetAddress().getHostName();
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] Constructor> : Loading library.");
        }

        if (logger.isInfoEnabled()) {
            logger.info(System.getProperty("java.library.path"));
        }
        System.loadLibrary(libName);
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] Constructor> : Library loaded.");
        }

        prefix = "[PA/Native] [" + ProActiveNativeInterface.this.hostname + "] > ";
        msgHandler = factory.createMsgHandler();
        msgAdapter = factory.createMsgAdapter();
        nativeInterface = new NativeInterfaceImpl();
        nativeInterface.init();
    }

    ////////////////////////////////
    //// INTERNAL METHODS       ////
    ////////////////////////////////
    public void createRecvThread() {
        Runnable r = new NativeMessagePoll();
        Thread t = new Thread(r, "Thread Message Recv");
        t.start();
    }

    public void sendJobNumberAndRegister(int nbJob) {

        byte[] msg = this.msgAdapter.buildInitMessage(nativeRank, nativeJobID, nbJob);
        this.nativeInterface.sendMessage(msg);

        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] sendJobNumber> setting jobId as(" +
                nativeJobID + ")");
        }

        this.manager.register(this.nativeJobID, nativeRank);
    }

    public void wakeUpThread() {
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] activeThread> : activate thread");
        }
        this.notify = true;
    }

    public void asleepThread() {
        this.notify = false;
    }

    ////////////////////////////////
    //// INITIALIZATION METHODS ////
    ////////////////////////////////
    public void setMyProxy(InboundProxy myProxy, ProActiveNativeManager jobManager, int idJob) {
        this.aoMyProxy = myProxy;
        this.nativeJobID = idJob;
        this.manager = jobManager;
        System.out.println("Native " + hostname + " associated with " + this.nativeJobID);
    }

    public void init(int uniqueID) {
        int res = nativeInterface.init();
        if (logger.isInfoEnabled()) {
            logger.info("[REMOTE PROXY] [" + this.hostname + "] init> : init: " + res);
        }
    }

    /****************************************************************/
    /** Communications with native 									*/
    /****************************************************************/

    public void sendToNative(NativeMessage m_r) {
        //    	((ProActiveMPIMessage) m_r).setTimer(System.currentTimeMillis());
        // asynch call to my ao proxy
        outboundProxy.sendToNative(m_r);
    }

    public void sendToNativeInParallel(NativeMessage[] messages) {
        outboundProxy.sendToNativeInParallel(messages);
    }

    public void receiveFromNative(NativeMessage m_r) {
        if (m_r.getSerializedMessage() == null) {
            throw new RuntimeException("[REMOTE PROXY] !!! DATA is null ");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[REMOTE PROXY] [" + this.hostname + "]  receiveFromMpi> received message" + m_r);
        }

        long start = System.currentTimeMillis();
        this.nativeInterface.sendMessage(m_r.getSerializedMessage());
        timer_send_to_native += System.currentTimeMillis() - start;
        timer_send_to_native_nb_call++;

        start = System.currentTimeMillis();
        this.nativeInterface.debug(m_r.getSerializedMessage());
        timer_send_debug += System.currentTimeMillis() - start;
    }

    public int getMyRank() {
        return nativeRank;
    }

    public void setRank(int rank) {
        this.nativeRank = rank;
        aoMyProxy.register(nativeRank);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n Hostname: " + this.hostname);
        sb.append("\n rank: " + this.nativeRank);
        return sb.toString();
    }

    /****************************************************************/
    /** Native Interface message polling							*/
    /****************************************************************/

    protected class NativeMessagePoll implements Runnable {
        private long timer_msg_deserialize = 0;

        public NativeMessagePoll() {
        }

        public void run() {

            aoMyProxy.nativeInterfaceReady();

            byte[] serializedMessage;
            while (shouldRun) {
                if (notify) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(prefix + " waiting for message ");
                    }
                    if ((serializedMessage = nativeInterface.recvMessage()) == null) {
                        throw new RuntimeException(prefix +
                            " !!! ERROR data received from native method is null");
                    } else {
                        long start = System.currentTimeMillis();
                        NativeMessage m_r = msgAdapter.deserialize(serializedMessage);
                        timer_msg_deserialize += (System.currentTimeMillis() - start);

                        if (logger.isDebugEnabled()) {
                            logger.debug(prefix + " received a message  " + m_r.toString(prefix));
                        }
                        shouldRun = msgHandler.handleMessage(ProActiveNativeInterface.this, m_r);
                    }
                }
            }

            System.out
                    .println("timer_send_to_native_ipc milli " +
                        timer_send_to_native +
                        " moy " +
                        ((timer_send_to_native_nb_call != 0) ? ((timer_send_to_native / timer_send_to_native_nb_call))
                                : 0));
            System.out.println("timer_msg_deserialize milli " + timer_msg_deserialize);
            System.out.println("timer_send_to_native_nb_call " + timer_send_to_native_nb_call);
            System.out.println("timer_send_debug milli " + timer_send_debug);

            /* Exiting */
            nativeInterface.terminate();
            aoMyProxy.unregisterProcess(nativeRank);
            outboundProxy.unregisterProcess(nativeRank);

        }
    }

    public void setOutboundProxy(OutboundProxy outboundProxy2) {
        this.outboundProxy = outboundProxy2;
    }
}
