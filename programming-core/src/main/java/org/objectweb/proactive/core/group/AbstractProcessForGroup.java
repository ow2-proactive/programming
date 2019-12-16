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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public abstract class AbstractProcessForGroup implements Runnable {

    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.GROUPS);

    protected static Proxy findLastProxy(Object obj) {
        if (!MOP.isReifiedObject(obj)) {
            return null;
        }
        Proxy proxy = ((StubObject) obj).getProxy();
        while (proxy instanceof FutureProxy) {
            if (MOP.isReifiedObject(((FutureProxy) proxy).getResult())) {
                return AbstractProcessForGroup.findLastProxy(((FutureProxy) proxy).getResult());
            } else {
                return proxy;
            }
        }
        return proxy;
    }

    protected Vector<?> memberList;

    protected ProxyForGroup proxyGroup;

    protected int groupIndex; // may change if dispatch is dynamic

    protected int resultIndex; // corresponds to index of results; does not change

    protected Vector<Object> memberListOfResultGroup = null;

    protected boolean dynamicallyDispatchable = false;

    public int getMemberListSize() {
        if (memberList != null) {
            return memberList.size();
        } else {
            return 0;
        }
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int index) {
        this.groupIndex = index;
    }

    public Vector<Object> getResultGroup() {
        return memberListOfResultGroup;
    }

    public boolean isDynamicallyDispatchable() {
        return dynamicallyDispatchable;
    }

    public void setDynamicallyDispatchable(boolean dynamicallyDispatchable) {
        this.dynamicallyDispatchable = dynamicallyDispatchable;
    }
}
