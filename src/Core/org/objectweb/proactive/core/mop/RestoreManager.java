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
package org.objectweb.proactive.core.mop;

import java.util.ArrayList;
import java.util.List;


/**
 * A restore manager logs all modifications made to an object
 * through the method  @see MOP#replaceObject() and is able to
 * restore the initial state of the object
 */
public class RestoreManager implements FieldToRestore {

    protected Object objectToRestore;
    protected List<FieldToRestore> list;

    public RestoreManager() {
        this.list = new ArrayList<FieldToRestore>();
    }

    /**
     * add a new modification to be taken into account by this manager
     * @param f
     */
    public void add(FieldToRestore f) {
        list.add(f);
    }

    public Object restore(Object modifiedObject) throws IllegalArgumentException, IllegalAccessException {
        Object result = modifiedObject;

        for (FieldToRestore f : list) {
            Object r = f.restore(modifiedObject);
            if (r != null) {
                result = r;
            }
        }
        return result;
    }

}
