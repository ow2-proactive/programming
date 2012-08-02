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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.mop.lock;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public class AbstractRemoteLocksManager implements RemoteLocksManager {

    // exported locks
    // [lock's hashcode => lock] this allows O(1) bidirectional access to the table
    // no collision should occurs as Lock.hashcode() is not overridden 
    private Hashtable<Integer, Lock> locks;

    public void lock(int id) {
        this.locks.get(id).lock();
    }

    public void lockInterruptibly(int id) throws InterruptedException {
        this.locks.get(id).lockInterruptibly();
    }

    public Condition newCondition(int id) {
        return this.locks.get(id).newCondition();
    }

    public boolean tryLock(int id) {
        return this.locks.get(id).tryLock();
    }

    public boolean tryLock(int id, long time, TimeUnit unit) throws InterruptedException {
        return this.locks.get(id).tryLock(time, unit);
    }

    public void unlock(int id) {
        this.locks.get(id).unlock();
    }

}
