/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package functionalTests.component.interceptor;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class A implements FooItf, Foo2Itf, BindingController {
    private FooItf b;
    private Foo2Itf b2;

    public A() {
    }

    @Override
    public void foo() {
        this.b.foo();
    }

    @Override
    public void foo2() {
        this.b2.foo2();
    }

    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals(FooItf.CLIENT_ITF_NAME)) {
            this.b = (FooItf) serverItf;
        } else if (clientItfName.equals(Foo2Itf.CLIENT_ITF_NAME)) {
            this.b2 = (Foo2Itf) serverItf;
        } else {
            throw new IllegalBindingException(
                "no such binding is possible : client interface name does not match");
        }
    }

    @Override
    public String[] listFc() {
        return null;
    }

    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (FooItf.CLIENT_ITF_NAME.equals(clientItfName)) {
            return this.b;
        } else if (Foo2Itf.CLIENT_ITF_NAME.equals(clientItfName)) {
            return this.b2;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
    }
}
