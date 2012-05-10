/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
