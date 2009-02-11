package org.objectweb.proactive.extra.messagerouting.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extra.messagerouting.remoteobject.message.MessageRoutingMessage;


/** Executes a ProActive {@link Request} received and send the response.
 *
 * @since ProActive 4.1.0
 */
public class ProActiveMessageHandler implements MessageHandler {

    public static final Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_CLIENT);

    /** {@link Request} are handled by a threadpool */
    final private ExecutorService tpe;

    /** Local agent */
    private Agent agent;

    public ProActiveMessageHandler(Agent agent) {
        this.agent = agent;

        /* DO NOT USE A FIXED THREAD POOL
         * 
         * Each time a message arrives, it is handled by a task submitted to 
         * this executor service. Each task can a perform remote calls. If 
         * the number of workers is fixed it can lead to deadlock.
         * 
         * Reentrant calls is the most obvious case of deadlock. But the same 
         * issue can occur with remote calls. 
         */
        tpe = Executors.newCachedThreadPool();

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

        public ProActiveMessageProcessor(DataRequestMessage msg, Agent agent) {
            this._toProcess = msg;
            this.agent = agent;
        }

        public void run() {
            ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                // Handle the message
                MessageRoutingMessage message = (MessageRoutingMessage) HttpMarshaller
                        .unmarshallObject(_toProcess.getData());
                Object result = null;
                try {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Processing message: " + message);
                    }
                    result = message.processMessage();
                } catch (Exception e) {
                    logger.warn("Exception during execution of message: " + _toProcess, e);
                    // TODO: Send an ERR_ ?
                    return;
                }

                byte[] resultBytes = HttpMarshaller.marshallObject(result);
                agent.sendReply(_toProcess, resultBytes);
            } catch (Exception e) {
                logger.warn("ProActive Message failed to serve a message", e);
                // TODO: Send an ERR_ ?
            } finally {
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }
}
