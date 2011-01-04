/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionalTests.component.collectiveitf.multicast;

import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class ClientServerImpl implements ServerTestItf, BindingController {
    private MulticastTestItf client;

    public WrappedInteger testAllStdModes_Param(List<WrappedInteger> defaultDispatch,
            List<WrappedInteger> broadcastDispatch, WrappedInteger oneToOneDispatch,
            WrappedInteger roundRobinDispatch, WrappedInteger singleElement) {
        return null;
    }

    public WrappedInteger testBroadcast_Method(List<WrappedInteger> listOfMyObject) {
        return null;
    }

    public WrappedInteger testBroadcast_Param(List<WrappedInteger> listOfMyObject) {
        return null;
    }

    public WrappedInteger testCustom_Method(WrappedInteger a) {
        return null;
    }

    public WrappedInteger testCustom_Param(WrappedInteger a) {
        return null;
    }

    public WrappedInteger testOneToOne_Method(WrappedInteger a) {
        return null;
    }

    public WrappedInteger testOneToOne_Param(WrappedInteger a) {
        return null;
    }

    public WrappedInteger testRoundRobin_Method(WrappedInteger a) {
        return null;
    }

    public WrappedInteger testRoundRobin_Param(WrappedInteger a) {
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("client".equals(clientItfName)) {
            client = (MulticastTestItf) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return new String[] { "client" };
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("client".equals(clientItfName)) {
            return client;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if ("client".equals(clientItfName)) {
            client = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
