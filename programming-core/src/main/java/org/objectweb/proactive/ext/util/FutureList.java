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
package org.objectweb.proactive.ext.util;

/**
 * <p>
 * <code>FutureList</code> is an object used to monitor a subset of all the futures waited by an
 * active object. A user can simply add or remove <code>Future</code> objects from this list and
 * then call methods to test for their availability.
 * </p>
 * <p>
 * Future Objects to be watched after are added and removed to this list by the user. This class is
 * not thread safe
 * </p>
 * 
 * @author The ProActive Team
 * @version 1.0, 2002/09/25
 * @since ProActive 0.9
 * 
 */
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAFuture;


@PublicAPI
public class FutureList {
    private java.util.Vector<Object> futureList;

    public FutureList() {
        futureList = new java.util.Vector<Object>();
    }

    /**
     * Add the future to the futureList This method does not test if the future is already in the
     * list.
     */
    public boolean add(Object o) {
        //	System.out.println("Adding future " + o);
        return this.futureList.add(o);
    }

    /**
     * Remove the object from the FutureList Return true if successfull
     */
    public boolean remove(Object o) {
        //	System.out.println("Trying to remove " + o);
        return this.futureList.remove(o);
    }

    /**
     * Return the number of future in the List
     */
    public int size() {
        return this.futureList.size();
    }

    /**
     * Return the element at the specified position in this List
     */
    public Object get(int index) {
        return this.futureList.elementAt(index);
    }

    /**
     * Return true if all the futures in the current list are awaited
     */
    public boolean allAwaited() {
        boolean value = true;
        for (int i = 0; i < futureList.size(); i++) {
            value = value && PAFuture.isAwaited(futureList.elementAt(i));
        }
        return value;
    }

    /**
     * Return true if none of the futures in the current list are awaited
     */
    public boolean noneAwaited() {
        for (int i = 0; i < futureList.size(); i++) {
            if (PAFuture.isAwaited(futureList.elementAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the number of currently awaited futures in this list
     */
    public int countAwaited() {
        int count = 0;
        for (int i = 0; i < futureList.size(); i++) {
            if (PAFuture.isAwaited(futureList.elementAt(i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a future available in this list. Returns null if none is available.
     */
    public Object getOne() {
        if (this.countAwaited() == this.size()) {
            //System.out.println("still waiting " + this.countAwaited()+ " futures");
            //futurePool.waitForReply();
            return null;
        } else {
            Object temp;
            for (int i = 0; i < futureList.size(); i++) {
                temp = futureList.elementAt(i);
                if (!PAFuture.isAwaited(temp)) {
                    return temp;
                }
            }
            return null;
        }
    }

    /**
     * Removes and returns a future available this list. Returns null if none is available.
     */
    public Object removeOne() {
        Object tmp;
        tmp = this.getOne();
        if (tmp != null) {
            //	System.out.println("Removing future "  + tmp);
            //System.out.println("Result is " + this.remove(tmp));
            this.remove(tmp);
        }
        return tmp;
    }

    public Object waitAndGetOne() {
        this.waitOne();
        return this.getOne();
    }

    public Object waitAndRemoveOne() {
        this.waitOne();
        return this.removeOne();
    }

    public void waitAll() {
        PAFuture.waitForAll(futureList);
    }

    public void waitOne() {
        PAFuture.waitForAny(futureList);
    }

    public void waitN(int n) {
        java.util.Vector<Object> temp = new java.util.Vector<Object>(futureList);
        for (int i = 0; i < n; i++) {
            int index = PAFuture.waitForAny(temp);
            temp.remove(index);
        }
    }

    public void waitTheNth(int n) {
        PAFuture.waitFor(futureList.get(n));
    }

    public Object waitAndGetTheNth(int n) {
        waitTheNth(n);
        return this.futureList.elementAt(n);
    }
}
