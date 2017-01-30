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
