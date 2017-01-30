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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;


/**
 * Interface for wrappers which handle locks. 
 * This Manager allows sending proxies on local locks
 * @see LockProxy
 */
public interface RemoteLocksManager {

    ////////////////////METHODS FROM LOCK ////////////////////

    void lock(int id);

    void lockInterruptibly(int id) throws InterruptedException;

    boolean tryLock(int id);

    boolean tryLock(int id, long time, TimeUnit unit) throws InterruptedException;

    void unlock(int id);

    Condition newCondition(int id);
}
