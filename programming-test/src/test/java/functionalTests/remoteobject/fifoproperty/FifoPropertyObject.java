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
package functionalTests.remoteobject.fifoproperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * An object that accumulates a set of integer into a list and that verifies
 * that all that each value as the following property : L(x) = L(x-1) + 1
 * 
 */
public class FifoPropertyObject implements Serializable {

    protected List<Integer> accumulator;

    protected int range;

    public FifoPropertyObject() {
    }

    public FifoPropertyObject(int range) {
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
                throw new RuntimeException("invalid order at pos " + it + ", prev=" + prev + ", current= " + t);
            }
            prev = t;
            it++;
        }

        return true;
    }

}
