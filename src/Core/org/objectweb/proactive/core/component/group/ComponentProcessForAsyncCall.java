/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.component.group;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.group.ProcessForAsyncCall;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * @author The ProActive Team
 *
 */
public class ComponentProcessForAsyncCall extends ProcessForAsyncCall {

    public ComponentProcessForAsyncCall(ProxyForGroup proxyGroup, Vector memberList,
            Vector memberListOfResultGroup, int groupIndex, MethodCall mc, int resultIndex, Body body,
            CountDownLatch doneSignal) {
        super(proxyGroup, memberList, memberListOfResultGroup, groupIndex, mc, resultIndex, body, doneSignal);
    }

    @Override
    public void executeMC(MethodCall mc, Object object) throws Throwable {
        if (object instanceof PAComponentRepresentative) {
            // delegate to the corresponding interface
            Object target;
            if (mc.getComponentMetadata().getComponentInterfaceName() == null) {
                // a call on the Component interface
                target = object;
            } else {
                target = ((PAComponentRepresentative) object).getFcInterface(mc.getComponentMetadata()
                        .getComponentInterfaceName());
            }
            this.addToListOfResult(mc.execute(target));
        } else if (object instanceof PAInterface) {
            this.addToListOfResult(mc.execute(object));
        }
    }

}
