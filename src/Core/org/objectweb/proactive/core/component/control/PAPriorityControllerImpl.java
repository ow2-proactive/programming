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
package org.objectweb.proactive.core.component.control;

import java.util.Hashtable;
import java.util.Map;

import org.etsi.uri.gcm.api.control.PriorityController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;


/**
 * @author The ProActive Team
 */
public class PAPriorityControllerImpl extends AbstractPAController implements PriorityController {
    private static final String ANY_PARAMETERS = "any-parameters";
    private Map<String, Object> nf2s;
    private Map<String, Object> nf3s;

    public PAPriorityControllerImpl(Component owner) {
        super(owner);
        nf2s = new Hashtable<String, Object>(2);
        nf2s.put("setPriorityNF2", ANY_PARAMETERS);
        nf3s = new Hashtable<String, Object>(3);
        nf3s.put("setPriorityNF3", ANY_PARAMETERS);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.PRIORITY_CONTROLLER,
                    PriorityController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName(), e);
        }
    }

    ///////////////////////////////////////
    // PriorityController IMPLEMENTATION //
    ///////////////////////////////////////
    // TODO_C for a NF? priority check that the method is in a controller
    public void setGCMPriority(String itfName, String methodName, Class<?>[] parameterTypes,
            RequestPriority priority) {
        switch (priority) {
            case NF1:
                nf2s.remove(methodName);
                nf3s.remove(methodName);
                break;
            case NF2:
                nf3s.remove(methodName);
                nf2s.put(methodName, parameterTypes);
                break;
            case NF3:
                nf2s.remove(methodName);
                nf3s.put(methodName, parameterTypes);
                break;
            default:
                break;
        }
    }

    public RequestPriority getGCMPriority(String itfName, String methodName, Class<?>[] parameterTypes) {
        if (nf2s.get(methodName) != null) {
            return RequestPriority.NF2;
        } else if (nf3s.get(methodName) != null) {
            return RequestPriority.NF3;
        } else if (Utils.isControllerItfName(itfName)) {
            return RequestPriority.NF1;
        } else {
            return RequestPriority.F;
        }
    }
}
