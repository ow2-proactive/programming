/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionalTests.remoteobject.fifoproperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * An object that accumulates a set of integer into a list and that verifies
 * that all that each value as the following property : L(x) = L(x-1) + 1
 * 
 */
public class FifoPropertyTesterObject implements Serializable {

    protected List<Integer> accumulator;
    protected int range;

    public FifoPropertyTesterObject() {
    }

    public FifoPropertyTesterObject(int range) {
        accumulator = new ArrayList<Integer>();
        this.range = range;
    }

    /**
     * add the integer into the internal list.
     * @param value
     */
    public void add(int value) {
        accumulator.add(value);
    }

    /**
     * verifies that all that each value of the list 
     * as the following property : L(x) = L(x-1) + 1
     * 
     * @return true if the property is verified
     */
    public boolean check() {

        int prev = -1;
        int it = 0;
        while (it < range) {
            int t = accumulator.get(it);

            if (t != (prev + 1)) {
                throw new RuntimeException("invalid order at pos " + it + ", prev=" + prev + ", current= " +
                    t);
            }
            prev = t;
            it++;
        }

        return true;
    }

}
