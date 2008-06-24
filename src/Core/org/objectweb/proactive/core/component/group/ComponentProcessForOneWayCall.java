/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.group;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.ProcessForOneWayCall;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * @author The ProActive Team
 *
 */
public class ComponentProcessForOneWayCall extends ProcessForOneWayCall {

    public ComponentProcessForOneWayCall(ProxyForGroup proxyGroup, Vector memberList, int index,
            MethodCall mc, Body body, ExceptionListException exceptionList, CountDownLatch doneSignal) {
        super(proxyGroup, memberList, index, mc, body, exceptionList, doneSignal);
    }

    @Override
    public void executeMC(MethodCall mc, Object object) throws Throwable {
        if (object instanceof ProActiveComponentRepresentative) {
            // delegate to the corresponding interface
            Object target;
            if (mc.getComponentMetadata().getComponentInterfaceName() == null) {
                // a call on the Component interface
                target = object;
            } else {
                target = ((ProActiveComponentRepresentative) object).getFcInterface(mc.getComponentMetadata()
                        .getComponentInterfaceName());
            }
            mc.execute(target);
        } else if (object instanceof ProActiveInterface) {
            mc.execute(object);
        } else
            throw new RuntimeException("Should not be here for component");
    }
}
