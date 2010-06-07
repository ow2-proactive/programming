/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.tools;

import java.util.ArrayList;
import java.util.Collection;


public class ArrayListSynchronized<E> extends ArrayList<E> {

    private static final long serialVersionUID = 5663142567515737172L;

    @Override
    public synchronized boolean add(E e) {
        // TODO Auto-generated method stub
        return super.add(e);
    }

    @Override
    public synchronized void add(int index, E element) {
        // TODO Auto-generated method stub
        super.add(index, element);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        // TODO Auto-generated method stub
        return super.addAll(c);
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        // TODO Auto-generated method stub
        return super.addAll(index, c);
    }

    @Override
    public synchronized void clear() {
        // TODO Auto-generated method stub
        super.clear();
    }

    @Override
    public synchronized Object clone() {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public synchronized boolean contains(Object o) {
        // TODO Auto-generated method stub
        return super.contains(o);
    }

    @Override
    public synchronized void ensureCapacity(int minCapacity) {
        // TODO Auto-generated method stub
        super.ensureCapacity(minCapacity);
    }

    @Override
    public synchronized E get(int index) {
        // TODO Auto-generated method stub
        return super.get(index);
    }

    @Override
    public synchronized int indexOf(Object o) {
        // TODO Auto-generated method stub
        return super.indexOf(o);
    }

    @Override
    public synchronized boolean isEmpty() {
        // TODO Auto-generated method stub
        return super.isEmpty();
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        return super.lastIndexOf(o);
    }

    @Override
    public synchronized E remove(int index) {
        // TODO Auto-generated method stub
        return super.remove(index);
    }

    @Override
    public synchronized boolean remove(Object o) {
        // TODO Auto-generated method stub
        return super.remove(o);
    }

    @Override
    protected synchronized void removeRange(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public synchronized E set(int index, E element) {
        // TODO Auto-generated method stub
        return super.set(index, element);
    }

    @Override
    public synchronized int size() {
        // TODO Auto-generated method stub
        return super.size();
    }

    @Override
    public synchronized Object[] toArray() {
        // TODO Auto-generated method stub
        return super.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return super.toArray(a);
    }

    @Override
    public synchronized void trimToSize() {
        // TODO Auto-generated method stub
        super.trimToSize();
    }
}
