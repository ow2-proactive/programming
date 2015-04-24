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
package org.objectweb.proactive.core.group.spmd;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.group.MethodCallControlForGroup;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


/**
 * @author The ProActive Team
 */
public class MethodCallSetSPMDGroup extends MethodCallControlForGroup {

    private static final long serialVersionUID = 62L;

    /**
     * Builds a method call to set the SPMD group
     * @param smpgGroup - the SPMD group to set
     */
    public MethodCallSetSPMDGroup(Object smpgGroup) {
        Object[] objTab = new Object[1];
        objTab[0] = smpgGroup;
        this.setEffectiveArguments(objTab);
    }

    /**
     * Returns the name of the call
     * @return the String "MethodCallSetSPMDGroup";
     */
    @Override
    public String getName() {
        return "MethodCallSetSPMDGroup";
    }

    /**
     * This call has one parameter : the group to set as SPMD group.
     * @return 1
     * @see org.objectweb.proactive.core.mop.MethodCall#getNumberOfParameter()
     */
    @Override
    public int getNumberOfParameter() {
        return 1;
    }

    /**
     * Executes the call.
     * @see org.objectweb.proactive.core.mop.MethodCall#execute(java.lang.Object)
     */
    @Override
    public Object execute(Object targetObject) throws InvocationTargetException,
            MethodCallExecutionFailedException {
        AbstractBody body = (AbstractBody) PAActiveObject.getBodyOnThis();
        body.setSPMDGroup(this.getParameter(0));
        return null;
    }
}
