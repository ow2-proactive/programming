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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;


public class RuntimeRegistrationEventProducerImpl extends AbstractEventProducer
        implements RuntimeRegistrationEventProducer {
    //
    //-------------------implements RuntimeRegistrationEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.RuntimeRegistrationEventProducer#addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
     */
    public void addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener) {
        addListener(listener);
    }

    /**
     * @see org.objectweb.proactive.core.event.RuntimeRegistrationEventProducer#removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
     */
    public void removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener) {
        removeListener(listener);
    }

    //
    //-------------------inherited methods from AbstractEventProducer------------------
    //

    /**
     * @see org.objectweb.proactive.core.event.AbstractEventProducer#notifyOneListener(ProActiveListener, ProActiveEvent)
     */
    @Override
    protected void notifyOneListener(ProActiveListener proActiveListener, ProActiveEvent event) {
        RuntimeRegistrationEvent runtimeRegistrationEvent = (RuntimeRegistrationEvent) event;
        RuntimeRegistrationEventListener runtimeRegistrationEventListener = (RuntimeRegistrationEventListener) proActiveListener;

        //notify the listener that a registration occurs
        runtimeRegistrationEventListener.runtimeRegistered(runtimeRegistrationEvent);
    }

    //
    //-------------------PROTECTED METHODS------------------
    //
    protected void notifyListeners(ProActiveRuntime proActiveRuntime, int type, ProActiveRuntime registeredRuntime,
            String creatorID, String protocol, String vmName) {
        if (hasListeners()) {
            notifyAllListeners(new RuntimeRegistrationEvent(proActiveRuntime,
                                                            type,
                                                            registeredRuntime,
                                                            creatorID,
                                                            protocol,
                                                            vmName));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("no listener");
            }
        }
    }
}
