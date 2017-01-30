/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.pamr.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.converter.remote.ProActiveMarshaller;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.exceptions.PAMRException;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extensions.pamr.remoteobject.message.PAMRMessage;
import org.objectweb.proactive.utils.NamedThreadFactory;


/** Executes a ProActive {@link Request} received and send the response.
 *
 * @since ProActive 4.1.0
 */
public class ProActiveMessageHandler implements MessageHandler {

    public static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_CLIENT);

    /** {@link Request} are handled by a threadpool */
    final private ExecutorService tpe;

    /** Local agent */
    private Agent agent;

    public ProActiveMessageHandler(Agent agent) {
        this.agent = agent;

        /*
         * DO NOT USE A FIXED THREAD POOL
         * 
         * Each time a message arrives, it is handled by a task submitted to
         * this executor service. Each task can a perform remote calls. If
         * the number of workers is fixed it can lead to deadlock.
         * 
         * Reentrant calls is the most obvious case of deadlock. But the same
         * issue can occur with remote calls.
         */
        ThreadFactory tf = new NamedThreadFactory("ProActive PAMR message handler");
        tpe = Executors.newCachedThreadPool(tf);

    }

    public void pushMessage(DataRequestMessage message) {
        if (logger.isTraceEnabled()) {
            logger.trace("pushing message " + message + " into the executor queue");
        }

        ProActiveMessageProcessor pmp = new ProActiveMessageProcessor(message, agent);
        tpe.submit(pmp);
    }

    /** Process one ProActive {@link Request} */
    private class ProActiveMessageProcessor implements Runnable {
        /** the request*/
        private final DataRequestMessage _toProcess;

        /** the local agent*/
        private final Agent agent;

        /** serialization*/
        private final ProActiveMarshaller marshaller;

        public ProActiveMessageProcessor(DataRequestMessage msg, Agent agent) {
            this._toProcess = msg;
            this.agent = agent;
            // get the runtime URL
            // if the local Agent has received a DataRequestMessage,
            // means that a ProActiveRuntime exists on this machine
            String runtimeUrl = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
            this.marshaller = new ProActiveMarshaller(runtimeUrl);
        }

        public void run() {
            ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            try {

                // Handle the message
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                PAMRMessage message;
                try {
                    message = (PAMRMessage) this.marshaller.unmarshallObject(_toProcess.getData());
                } catch (Throwable t) {
                    PAMRException e = new PAMRException("Failed to unmarshall incoming message on " +
                                                        this.agent.getAgentID() + "for " + this._toProcess, t);
                    SynchronousReplyImpl sr = new SynchronousReplyImpl(new MethodCallResult(null, e));
                    agent.sendReply(_toProcess, this.marshaller.marshallObject(sr));
                    return;
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("Processing message: " + message);
                }
                Object result = message.processMessage(); // Cannot throw an exception

                byte[] resultBytes;
                try {
                    resultBytes = this.marshaller.marshallObject(result);
                } catch (Throwable t) {
                    PAMRException e = new PAMRException("Failed to marshall the result bytes on " +
                                                        this.agent.getAgentID() + " for " + _toProcess, t);
                    SynchronousReplyImpl sr = new SynchronousReplyImpl(new MethodCallResult(null, e));
                    agent.sendReply(_toProcess, this.marshaller.marshallObject(sr));
                    return;
                }

                try {
                    agent.sendReply(_toProcess, resultBytes);
                } catch (Throwable t) {
                    logger.info("Failed to send the PAMR reply to " + this._toProcess +
                                ". The router should discover the disconnection and unlock the caller", t);
                    return;
                }
            } catch (PAMRException e) {
                logger.info("Failed to send the PAMR error reply to " + this._toProcess +
                            ". The router should discover the disconnection and unlock the caller", e);
                agent.closeTunnel(e);
            } catch (IOException e) {
                logger.info("Failed to send the PAMR error reply to " + this._toProcess +
                            ". The router should discover the disconnection and unlock the caller", e);
                agent.closeTunnel(new PAMRException(e));
            } catch (Throwable t) {
                PAMRException e = new PAMRException("Fatal error occured while serving acall", t);
                agent.closeTunnel(e);
            } finally {
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }
}
