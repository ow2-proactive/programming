//@tutorial-start
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@snippet-start adl_interfaces_SlaveImpl_skeleton
//@snippet-start adl_interfaces_SlaveImpl
package org.objectweb.proactive.examples.userguide.components.adl.interfaces;

import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;


/**
 * @author The ProActive Team
 */
public class SlaveImpl implements Itf1
//TODO: Add the new interface org.objectweb.proactive.examples.userguide.components.adl.interfaces.Itf2
        //@tutorial-break
        //@snippet-break adl_interfaces_SlaveImpl_skeleton
        , Itf2
//@tutorial-resume
//@snippet-resume adl_interfaces_SlaveImpl_skeleton
{

    public void compute(List<String> arg) {
        String str = "\n" + PAActiveObject.getBodyOnThis().getNodeURL() + "Slave: " + this + "\n";
        str += "arg: ";
        for (int i = 0; i < arg.size(); i++) {
            str += arg.get(i);
            if (i + 1 < arg.size()) {
                str += " - ";
            }
        }
        System.err.println(str);
    }

    public void doNothing() {
        System.err.println("\n" + PAActiveObject.getBodyOnThis().getNodeURL() + "Slave: " + this +
            "\nI do nothing");
    }
}
//@snippet-end adl_interfaces_SlaveImpl_skeleton
//@tutorial-end
//@snippet-end adl_interfaces_SlaveImpl