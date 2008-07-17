/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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


public class NonReduction1Impl implements NonReduction1, Identifiable {
    private int id;

    public IntWrapper doIt() {
        System.out.println(" Server received call on doIt");
        return new IntWrapper(id);
    }

    public IntWrapper doItInt(IntWrapper val) {
        System.out.println(" Server received " + val.intValue());
        return new IntWrapper(id + val.intValue());
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
