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
package functionalTests.component.wsbindings.frascati;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class ClientImpl implements BindingController, Services {
    public static final String SERVICES_NAME = "client-services";

    private Services services;

    public void doNothing() {
        services.doNothing();
    }

    public int incrementInt(int i) {
        return services.incrementInt(i);
    }

    public double[] decrementArrayDouble(double[] array) {
        return services.decrementArrayDouble(array);
    }

    public String modifyString(String name) {
        return services.modifyString(name);
    }

    public String[] splitString(String string) {
        return services.splitString(string);
    }

    public AnObject modifyObject(AnObject object) {
        return services.modifyObject(object);
    }

    public AnObject[] modifyArrayObject(AnObject[] arrayObject) {
        return services.modifyArrayObject(arrayObject);
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (SERVICES_NAME.equals(clientItfName)) {
            services = (Services) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { SERVICES_NAME };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (SERVICES_NAME.equals(clientItfName)) {
            return services;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (SERVICES_NAME.equals(clientItfName)) {
            services = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
