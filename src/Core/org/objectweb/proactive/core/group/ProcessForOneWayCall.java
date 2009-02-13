/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
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

    public ProcessForOneWayCall(ProxyForGroup proxyGroup, Vector memberList, int index, MethodCall mc,
            Body body, ExceptionListException exceptionList, CountDownLatch doneSignal) {
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
