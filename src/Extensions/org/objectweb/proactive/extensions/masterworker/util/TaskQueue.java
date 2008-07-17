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
package org.objectweb.proactive.extensions.masterworker.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Iterator;
import java.util.Collection;


/**
 * Queue of pending tasks, able to tell if some tasks have been submitted by a given worker
 *
 * @author The ProActive Team
 */
public class TaskQueue implements Queue<TaskID> {

    private Stack<TaskID> lifo;
    private LinkedList<TaskID> fifo;

    public TaskQueue() {
        super();
        lifo = new Stack<TaskID>();
        fifo = new LinkedList<TaskID>();
    }

    public int countTasksByOriginator(String originator) {
        int count = 0;
        for (TaskID tid : this) {
            if (tid.getOriginator().equals(originator)) {
                count++;
            }
        }

        return count;
    }

    public boolean hasTasksByOriginator(String originator) {
        for (TaskID tid : this) {
            if (tid.getOriginator().equals(originator)) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return lifo.size() + fifo.size();
    }

    public boolean isEmpty() {
        return lifo.isEmpty() && fifo.isEmpty();
    }

    public boolean contains(Object o) {
        Iterator<TaskID> it = this.iterator();
        while (it.hasNext()) {
            TaskID tid = it.next();
            if (tid.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public Iterator<TaskID> iterator() {
        final Iterator<TaskID> lifoit = lifo.iterator();
        final Iterator<TaskID> fifoit = fifo.iterator();
        return new Iterator<TaskID>() {

            private boolean fifolast = true;

            public boolean hasNext() {
                return fifoit.hasNext() || lifoit.hasNext();
            }

            public TaskID next() {
                if (fifoit.hasNext()) {
                    return fifoit.next();
                }
                fifolast = false;
                return lifoit.next();
            }

            public void remove() {
                if (fifolast) {
                    fifoit.remove();
                } else {
                    lifoit.remove();
                }
            }
        };
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        Iterator<TaskID> it = this.iterator();
        while (it.hasNext()) {
            TaskID tid = it.next();
            if (tid.equals(o)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends TaskID> c) {
        boolean changed = false;
        for (TaskID o : c) {
            changed = changed || this.add(o);
        }
        return changed;
    }

    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed = changed || this.remove(o);
        }

        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        lifo.clear();
        fifo.clear();
    }

    public boolean add(TaskID taskID) {
        if (taskID.isDivisible()) {
            return lifo.add(taskID);
        } else {
            return fifo.add(taskID);
        }
    }

    public boolean offer(TaskID taskID) {
        return add(taskID);
    }

    public TaskID remove() {
        return poll();
    }

    public TaskID poll() {
        if (fifo.isEmpty()) {
            return lifo.pop();
        } else {
            return fifo.poll();
        }
    }

    public TaskID element() {
        return peek();
    }

    public TaskID peek() {
        if (fifo.isEmpty()) {
            return lifo.peek();
        } else {
            return fifo.peek();
        }
    }
}
