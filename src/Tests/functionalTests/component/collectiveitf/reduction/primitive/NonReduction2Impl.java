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
package functionalTests.component.collectiveitf.reduction.primitive;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.component.collectiveitf.multicast.Identifiable;


public class NonReduction2Impl implements NonReduction2, Identifiable {
    private int id;

    public IntWrapper doIt() {
        System.out.println(" Server received call on doIt");
        return new IntWrapper(id);
    }

    public IntWrapper doItInt(IntWrapper val) {
        System.out.println(" Server received " + val.getIntValue());
        return new IntWrapper(id + val.getIntValue());
    }

    public void voidDoIt() {
        System.out.println(" Server received call on voidDoIt");
    }

    public String getID() {
        return String.valueOf(id);
    }

    public void setID(String id) {
        this.id = new Integer(id).intValue();
    }
}
