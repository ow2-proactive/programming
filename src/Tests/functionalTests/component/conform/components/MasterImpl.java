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
package functionalTests.component.conform.components;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class MasterImpl implements BindingController, Master {
    public static String ITF_CLIENTE_MULTICAST = "client-multicast";
    private SlaveMulticast slaves = null;

    public MasterImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (ITF_CLIENTE_MULTICAST.equals(clientItfName)) {
            slaves = (SlaveMulticast) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] listFc() {
        return new String[] { ITF_CLIENTE_MULTICAST };
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (ITF_CLIENTE_MULTICAST.equals(clientItfName)) {
            return slaves;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (ITF_CLIENTE_MULTICAST.equals(clientItfName)) {
            slaves = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void computeOneWay(List<String> args, String other) {
        slaves.computeOneWay(args, other);
    }

    public List<StringWrapper> computeAsync(List<String> args, String other) {
        return slaves.computeAsync(args, other);
    }

    public List<StringWrapper> computeRoundRobinBroadcastAsync(List<String> args, List<String> other) {
        return slaves.computeRoundRobinBroadcastAsync(args, other);
    }

    public List<GenericTypeWrapper<String>> computeAsyncGenerics(List<String> args, String other) {
        //            System.err.println("Async calls with " + list.size() + " arguments.");
        //            Object[] sw = ((List<StringWrapper>) slaves.computeAsync(list,
        //                    "Async")).toArray();
        return slaves.computeAsyncGenerics(args, other);
        //            for (Object object : sw) {
        //                System.err.println("Object result: " + object);
        //            }
    }

    public List<String> computeSync(List<String> args, String other) {
        List<GenericTypeWrapper<String>> list = slaves.computeAsyncGenerics(args, other);
        List<String> listResult = new ArrayList<String>(list.size());
        for (GenericTypeWrapper<String> string : list) {
            listResult.add(string.getObject());
        }
        return listResult;
    }

}
