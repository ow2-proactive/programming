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
package functionalTests.activeobject.request.isuniquethread;

import java.util.Hashtable;
import java.util.Map;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public class AgentForIS {

    // static for all active objects Agent <Owner -> <CallerID, ThreadforIS>
    private static Map<UniqueID, Map<UniqueID, Thread>> threadsForISUnique = new Hashtable<UniqueID, Map<UniqueID, Thread>>();

    private Thread serviceThread;

    private UniqueID myID;

    // IS MT
    @ImmediateService
    public BooleanWrapper foo() {
        return new BooleanWrapper((!Thread.currentThread().equals(this.serviceThread)) &&
                                  !(threadsForISUnique.get(myID).containsValue(Thread.currentThread())));
    }

    // NORMAL
    public BooleanWrapper foo(Integer a) {
        return new BooleanWrapper(Thread.currentThread().equals(this.serviceThread));
    }

    // IS UT
    @ImmediateService(uniqueThread = true)
    public BooleanWrapper foo(Long a, Integer b) {
        UniqueID caller = PAActiveObject.getContext().getCurrentRequest().getSourceBodyID();
        if (!this.threadsForISUnique.get(myID).containsKey(caller)) {
            // first call for this caller
            this.threadsForISUnique.get(myID).put(caller, Thread.currentThread());
        }
        return new BooleanWrapper(Thread.currentThread().equals(this.threadsForISUnique.get(myID).get(caller)));
    }

    @ImmediateService
    public void nothing() {
    }

    public boolean checkAllThreadISAreDown(UniqueID id) {
        for (Thread t : threadsForISUnique.get(id == null ? myID : id).values()) {
            //if (!t.getState().equals(State.TERMINATED)){
            if (t.isAlive()) {
                return false;
            }
        }
        return true;
    }

    public int init() {
        this.serviceThread = Thread.currentThread();

        this.myID = PAActiveObject.getBodyOnThis().getID();

        threadsForISUnique.put(myID, new Hashtable<UniqueID, Thread>());

        return 0;

    }

    public UniqueID getID() {
        return this.myID;
    }
}
