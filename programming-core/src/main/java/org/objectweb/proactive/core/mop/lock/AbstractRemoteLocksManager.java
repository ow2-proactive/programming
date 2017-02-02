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
