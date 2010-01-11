/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
//@snippet-start Exchange_1
package org.objectweb.proactive.examples.documentation.activeobjectconcepts;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


public class Exchange {

    public static class AO {
        protected double[] myArray;

        public void display() {
            String vect = "[";
            for (double i : myArray) {
                vect += i + ", ";
            }
            int index = vect.lastIndexOf(',');
            if (index != -1)
                vect = vect.substring(0, index);

            vect += "]";
            System.out.println(vect);
        }
    }

    public static class AO1 extends AO {

        public void foo(AO2 ao2) {
            myArray = new double[] { 1, 2, 0, 0 };
            // Before : myArray = [1, 2, 0, 0]
            PAActiveObject.exchange("myExch", ao2, myArray, 0, myArray, 2, 2);
            // After : myArray = [1, 2, 3, 4]
        }
    }

    public static class AO2 extends AO {

        public void bar(AO1 ao1) {
            myArray = new double[] { 0, 0, 3, 4 };
            // Before : myArray = [0, 0, 3, 4]
            PAActiveObject.exchange("myExch", ao1, myArray, 2, myArray, 0, 2);
            // After : myArray = [1, 2, 3, 4]
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            AO1 ao1 = (AO1) PAActiveObject.newActive(AO1.class.getName(), null);
            AO2 ao2 = (AO2) PAActiveObject.newActive(AO2.class.getName(), null);
            ao1.foo(ao2);
            ao2.bar(ao1);
            ao1.display();
            ao2.display();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }
}
//@snippet-end Exchange_1