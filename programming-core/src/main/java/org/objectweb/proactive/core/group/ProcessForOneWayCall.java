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
package org.objectweb.proactive.core.group;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides multithreading for the oneway methodcall on a group.
 *
 * @author The ProActive Team
 */
public class ProcessForOneWayCall extends AbstractProcessForGroup {
    //    private int index;
    private MethodCall mc;

    private Body body;

    private ExceptionListException exceptionList;

    CountDownLatch doneSignal;

    public ProcessForOneWayCall(ProxyForGroup proxyGroup, Vector memberList, int index, MethodCall mc, Body body,
            ExceptionListException exceptionList, CountDownLatch doneSignal) {
        this.proxyGroup = proxyGroup;
        this.memberList = memberList;
        this.groupIndex = index;
        this.mc = mc;
        this.body = body;
        this.exceptionList = exceptionList;
        this.doneSignal = doneSignal;
        this.dynamicallyDispatchable = false;
    }

    public void run() {
        // push an initial context for this thread
        LocalBodyStore.getInstance().pushContext(new Context(body, null));
        Object object = null;

        object = this.memberList.get(this.groupIndex);

        /* only do the communication (reify) if the object is not an error nor an exception */
        if (!(object instanceof Throwable)) {
            try {
                executeMC(this.mc, object);
            } catch (Throwable e) {
                this.exceptionList.add(new ExceptionInGroup(object, this.groupIndex, e.fillInStackTrace()));
            }
            doneSignal.countDown();
        }
        // delete contexts for this thread
        LocalBodyStore.getInstance().clearAllContexts();
    }

    public void executeMC(MethodCall mc, Object target) throws Throwable {
        boolean objectIsLocal = false;
        Proxy lastProxy = AbstractProcessForGroup.findLastProxy(target);
        if (lastProxy instanceof UniversalBodyProxy) {
            objectIsLocal = ((UniversalBodyProxy) lastProxy).isLocal();
        }
        if (lastProxy == null) {
            // means we are dealing with a non-reified object (a standard Java Object)
            mc.execute(target);
        } else if (objectIsLocal) {
            if (!(mc instanceof MethodCallControlForGroup)) {
                ((StubObject) target).getProxy().reify(mc.getShallowCopy());
            } else {
                ((StubObject) target).getProxy().reify(mc);
            }
        } else {
            ((StubObject) target).getProxy().reify(mc);
        }
    }

}
