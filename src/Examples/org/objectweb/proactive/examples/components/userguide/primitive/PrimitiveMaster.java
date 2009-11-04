/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@snippet-start primitive_master
package org.objectweb.proactive.examples.components.userguide.primitive;

import java.io.Serializable;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class PrimitiveMaster implements Runnable, Serializable, BindingController {
    private static final String COMPUTER_CLIENT_ITF = "compute-itf";
    private ComputeItf computer;

    public PrimitiveMaster() {
    }

    public void run() {
        computer.doNothing();
        int result = computer.compute(5);
        System.out.println(" PrimitiveMaster-->run(): " + "Result of computation whith 5 is: " + result); //display 10
    }

    //BINDING CONTROLLER implementation
    public void bindFc(String myClientItf, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (myClientItf.equals(COMPUTER_CLIENT_ITF)) {
            computer = (ComputeItf) serverItf;
        }
    }

    public String[] listFc() {
        return new String[] { COMPUTER_CLIENT_ITF };
    }

    public Object lookupFc(String itf) throws NoSuchInterfaceException {
        if (itf.equals(COMPUTER_CLIENT_ITF)) {
            return computer;
        }
        return null;
    }

    public void unbindFc(String itf) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (itf.equals(COMPUTER_CLIENT_ITF)) {
            computer = null;
        }
    }
}
//@snippet-end primitive_master
