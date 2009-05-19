/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.mop;

import java.util.Hashtable;


public class ObjectReferenceReplacer implements ObjectReplacer {

    private Object initialReference;
    private Object from;
    private Object to;
    private RestoreManager restoreManager;
    private Hashtable<Integer, Object> visitedObjects;

    public ObjectReferenceReplacer(Object from, Object to) {
        this.from = from;
        this.to = to;
        this.restoreManager = new RestoreManager();
        this.visitedObjects = new Hashtable<Integer, Object>();
    }

    public Object replaceObject(Object objectToAnalyse) {
        this.initialReference = objectToAnalyse;
        return MOP.replaceObject(objectToAnalyse, from, to, restoreManager, visitedObjects);
    }

    public Object restoreObject() throws IllegalArgumentException, IllegalAccessException {
        return restoreManager.restore(this.initialReference);
    }

}
