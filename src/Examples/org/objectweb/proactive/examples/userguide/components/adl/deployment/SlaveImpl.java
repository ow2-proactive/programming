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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@tutorial-start
package org.objectweb.proactive.examples.userguide.components.adl.deployment;

import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;


/**
 * @author The ProActive Team
 */
public class SlaveImpl implements Itf1, Itf2 {

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
//@tutorial-end
