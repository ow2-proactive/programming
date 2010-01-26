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
package functionalTests.component.nonfunctional.creation;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 *
 * @author The ProActive Team
 *
 * Content class of the dummy controller component
 */
public class DummyMaster implements DummyControllerItf, BindingController {
    private DummyControllerItf dummyController;

    public String dummyMethodWithResult() {
        return dummyController.dummyMethodWithResult();
    }

    public void dummyVoidMethod(String message) {
        dummyController.dummyVoidMethod(message);
    }

    public void bindFc(String arg0, Object arg1) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (arg0.equals("dummy-client")) {
            dummyController = (DummyControllerItf) arg1;
        }
    }

    public String[] listFc() {
        return new String[] { "dummy-client" };
    }

    public Object lookupFc(String arg0) throws NoSuchInterfaceException {
        if (arg0.equals("dummy-client")) {
            return dummyController;
        }
        return null;
    }

    public void unbindFc(String arg0) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (arg0.equals("dummy-client")) {
            dummyController = null;
        }
    }

    public IntWrapper result(IntWrapper param) {

        return dummyController.result(param);
    }
}
