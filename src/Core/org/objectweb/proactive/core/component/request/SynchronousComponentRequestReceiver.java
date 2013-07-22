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
package org.objectweb.proactive.core.component.request;

import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestReceiverImpl;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.body.ComponentBody;
import org.objectweb.proactive.core.component.control.PAInterceptorControllerImpl;
import org.objectweb.proactive.core.component.interception.Interceptor;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This is an extension of the {@link RequestReceiverImpl} class, which allows the
 * shortcutting of functional requests : when crossing a composite component
 * that has such a request receiver, a shortcut notification is sent to the
 * emitter, and the request is directly transferred to the following linked
 * interface. This means that we stay in the rendez-vous until the request
 * reaches its final destination (a primitive component where the request can be
 * executed, or a component that does not have such a synchronous request
 * receiver).
 *
 * @author The ProActive Team
 */
public class SynchronousComponentRequestReceiver extends RequestReceiverImpl {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUESTS);
    public final static int SHORTCUT = 1;

    public SynchronousComponentRequestReceiver() {
        super();
    }

    @Override
    public int receiveRequest(Request r, Body bodyReceiver) {
        if (r instanceof ComponentRequest) {
            if (!((ComponentRequest) r).isControllerRequest()) {
                if (CentralPAPropertyRepository.PA_COMPONENT_USE_SHORTCUTS.isTrue()) {

                    try {
                        List<Interceptor> interceptors = ((PAInterceptorControllerImpl) Utils
                                .getPAInterceptorController(((ComponentBody) bodyReceiver)
                                        .getPAComponentImpl())).getInterceptors(r.getMethodCall()
                                .getComponentMetadata().getComponentInterfaceName());

                        if (!interceptors.isEmpty()) {
                            if (logger.isDebugEnabled()) {
                                logger
                                        .debug("shortcut is stopped in this component, because functional invocations are intercepted");
                            }

                            // no shortcut if there is an interception
                            return super.receiveRequest(r, bodyReceiver);
                        }
                    } catch (NoSuchInterfaceException e) {
                        // No PAInterceptorController, shorcut can be done
                    }

                    ((ComponentRequest) r).shortcutNotification(r.getSender(), bodyReceiver
                            .getRemoteAdapter());

                    // TODO leave a ref of the shortcut
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("directly executing request " +
                                    r.getMethodCall().getName() +
                                    ((r.getMethodCall().getComponentMetadata().getComponentInterfaceName() != null) ? (" on interface " + r
                                            .getMethodCall().getComponentMetadata()
                                            .getComponentInterfaceName())
                                            : ""));
                    }
                }
                bodyReceiver.serve(r);
                // TODO check with FT
                return SynchronousComponentRequestReceiver.SHORTCUT;
            }
        }

        // normal object invocations and controller requests are not subject to shortcuts
        return super.receiveRequest(r, bodyReceiver);
    }
}
