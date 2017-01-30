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
package org.objectweb.proactive.core.group.spmd;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.group.MethodCallControlForGroup;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


/**
 * This class represents a call of strong synchronization between the member of a SPMD Group.
 * @author The ProActive Team
 */
public class MethodCallBarrier extends MethodCallControlForGroup {

    /** The unique ID of the barrier */
    private String IDName;

    /**
     * Constructor
     * @param idname - the id name of the barrier
     */
    public MethodCallBarrier(String idname) {
        this.IDName = idname;
    }

    /**
     * Returns the name of the call
     * @return the String "MethodCallBarrier"
     */
    @Override
    public String getName() {
        return "MethodCallBarrier";
    }

    /**
     * Returns the ID name of the barrier
     * @return the ID name of the barrier
     */
    public String getIDName() {
        return this.IDName;
    }

    /**
     * Execution of a barrier call is to block the service of request if the method is sent by the object itself.
     * @param target this object is not used.
     * @return null
     */
    @Override
    public Object execute(Object target) throws InvocationTargetException, MethodCallExecutionFailedException {
        ProActiveSPMDGroupManager spmdManager = ((AbstractBody) PAActiveObject.getBodyOnThis()).getProActiveSPMDGroupManager();
        BarrierState bs = spmdManager.getBarrierStateFor(this.getIDName());

        // bs == null  =>  state not found  =>  first barrier encountered for ID name
        if (bs == null) {
            bs = new BarrierState();
            spmdManager.addToCurrentBarriers(this.getIDName(), bs);
        }
        bs.incrementReceivedCalls();
        // if there is no others waiting calls, release the barrier
        if ((bs.getAwaitedCalls() - bs.getReceivedCalls()) == 0) {
            spmdManager.remove(this.getIDName());
        }
        return null;
    }
}
