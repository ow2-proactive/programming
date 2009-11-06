//@tutorial-start
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
//@snippet-start api_composite_MasterImpl
package org.objectweb.proactive.examples.userguide.components.api.composite;

import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.examples.userguide.components.api.composite.Itf1;
import org.objectweb.proactive.examples.userguide.components.api.composite.Runner;


/**
 * @author The ProActive Team
 */
public class MasterImpl implements Runner, BindingController {
    public static String ITF_CLIENT = "i1";
    private Itf1 i1;

    public void run(List<String> arg) {
        i1.compute(arg);
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (ITF_CLIENT.equals(clientItfName)) {
            i1 = (Itf1) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { ITF_CLIENT };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (ITF_CLIENT.equals(clientItfName)) {
            return i1;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (ITF_CLIENT.equals(clientItfName)) {
            i1 = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
//@snippet-end api_composite_MasterImpl
//@tutorial-end
