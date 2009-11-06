/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 *                         Nice-Sophia Antipolis/ActiveEon
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
package functionalTests.component.collectiveitf.gathercast;

import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class GatherClientServer implements GatherDummyItf, BindingController {
    private DummyItf client;

    public void foo(List<IntMutableWrapper> l) {
    }

    public List<B> bar(List<A> l) {
        return null;
    }

    public List<B> timeout() {
        return null;
    }

    public void executeAlone(List<String> msg) {
    }

    public List<StringWrapper> executeAlone2() {
        return null;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("client".equals(clientItfName)) {
            client = (DummyItf) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { "client" };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("client".equals(clientItfName)) {
            return client;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if ("client".equals(clientItfName)) {
            client = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
