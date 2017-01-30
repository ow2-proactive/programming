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
package functionalTests.activeobject.creation.local.newactive.constructors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class B {
    private String choosed = null;

    public B() {
    }

    public B(Object o) {
        choosed = "C1";
    }

    public B(String s) {
        choosed = "C2";
    }

    public B(int i) {
        choosed = "C3";
    }

    public B(long j) {
        choosed = "C4";
    }

    public B(Long j) {
        choosed = "C5";
    }

    public B(String s, Object o) {
        choosed = "C6";
    }

    public B(Object o, String s) {
        choosed = "C7";
    }

    public B(Collection<? extends Object> o) {
        choosed = "C8";
    }

    public B(List<? extends Object> o) {
        choosed = "C9";
    }

    public B(ArrayList<? extends Object> o) {
        choosed = "C10";
    }

    public String getChoosed() {
        return choosed;
    }
}
