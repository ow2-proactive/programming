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
package org.objectweb.proactive.core.body;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;


/**
 * Define an execution context for a thread. A context is associated to a thread.
 * A context contains the body associated to the thread, and the currently served request (or null if any).
 * @see org.objectweb.proactive.core.body.LocalBodyStore
 * @author The ProActive Team
 * @since 3.2.1
 */
@PublicAPI
public class Context implements Serializable {

    /** Body associated to this context */
    private final Body body;

    /** The currently served request */
    private final Request currentRequest;

    /**
     * Create a new context.
     * @param owner the body associated to this context.
     * @param currentRequest the currently served request, null if any.
     */
    public Context(Body owner, Request currentRequest) {
        this.body = owner;
        this.currentRequest = currentRequest;
    }

    /**
     * @return the body associated to this context.
     */
    public Body getBody() {
        return body;
    }

    /**
     * @return the currently served request, null if any.
     */
    public Request getCurrentRequest() {
        return currentRequest;
    }

    /**
     * Returns a stub on the active object that sent the currently served request.
     * @return a stub on the active object that sent the currently served request.
     */
    public Object getStubOnCaller() {
        if (this.currentRequest != null) {
            try {
                UniversalBody caller = currentRequest.getSender();
                return MOP.createStubObject(caller.getReifiedClassName(), caller);
            } catch (MOPException e) {
                throw new ProActiveRuntimeException("Cannot create stub on caller : " + e);
            }
        } else {
            throw new ProActiveRuntimeException("No request is currently served by " + this.body);
        }
    }

    /**
     * Pretty printing.
     */
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("Execution context for body ");
        res.append(this.body.getID()).append(" : ");
        if (this.currentRequest == null) {
            res.append("no current service.");
        } else {
            res.append("service of ")
               .append(this.currentRequest.getMethodName())
               .append(" from ")
               .append(this.currentRequest.getSourceBodyID());
        }
        return res.toString();
    }
}
