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
package functionalTests.activeobject.generics;

public class Pair<X, Y> {
    private X first;

    private Y second;

    public Pair() {
    }

    public Pair(X a1, Y a2) {
        //		System.out.println("X = " + a1 + " ; Y = " + a2);
        first = a1;
        second = a2;
    }

    public X getFirst() {
        //		System.out.println("[PAIR] getFirst called in " + getClass().getName());
        return first;
    }

    public Y getSecond() {
        //		System.out.println("[PAIR] getSecond called in " + getClass().getName());
        return second;
    }

    public void setFirst(X arg) {
        first = arg;
    }

    public void setSecond(Y arg) {
        second = arg;
    }
}
