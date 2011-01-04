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
package org.objectweb.proactive.core.component.representative;

import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


/**
 * A factory for component representatives.
 *
 * @author The ProActive Team
 */
public class PAComponentRepresentativeFactory {
    private static PAComponentRepresentativeFactory INSTANCE = null;

    private PAComponentRepresentativeFactory() {
    }

    /**
     * returns the unique instance in the jvm
     * @return the unique instance in the jvm
     */
    public static PAComponentRepresentativeFactory instance() {
        if (INSTANCE == null) {
            return (INSTANCE = new PAComponentRepresentativeFactory());
        } else {
            return INSTANCE;
        }
    }

    /**
     * Creates a component representative according to the type of the component
     * (it also generates the required functional interfaces), and connects the representative to
     * the given proxy. It also takes into account a controller config file for generating references to
     * the implementations of the controllers of this component.
     * @param componentType the type of the component
     * @param proxy the proxy to the active object
     * @param controllerConfigFileLocation location of a file that contains the description of the controllers for this component. null will load the default configuration
     * @return a corresponding component representative
     */
    public PAComponentRepresentative createComponentRepresentative(ComponentType componentType,
            String hierarchicalType, Proxy proxy, String controllerConfigFileLocation) {
        PAComponentRepresentative representative = new PAComponentRepresentativeImpl(componentType,
            hierarchicalType, controllerConfigFileLocation);
        representative.setProxy(proxy);
        return representative;
    }

    public PAComponentRepresentative createComponentRepresentative(ComponentParameters params, Proxy proxy) {
        PAComponentRepresentative representative = new PAComponentRepresentativeImpl(params);
        representative.setProxy(proxy);
        return representative;
    }

    /**
     * Creates a component representative according to the type of the non-functional component
     * (it also generates the required functional interfaces), and connects the representative to
     * the given proxy. It also takes into account a controller config file for generating references to
     * the implementations of the controllers of this component.
     * @param componentType the type of the component
     * @param proxy the proxy to the active object
     * @param controllerConfigFileLocation location of a file that contains the description of the controllers for this component. null will load the default configuration
     * @return a corresponding component representative
     */
    public PAComponentRepresentative createNFComponentRepresentative(ComponentType componentType,
            String hierarchicalType, Proxy proxy, String controllerConfigFileLocation) {
        PAComponentRepresentative representative = new PANFComponentRepresentativeImpl(componentType,
            hierarchicalType, controllerConfigFileLocation);
        representative.setProxy(proxy);
        return representative;
    }

    /**
     * The creation of a component representative from a proxy object implies a remote invocation (immediate service) for
     * getting the parameters of the component, necessary for the construction of the representative
     * @param proxy a reference on a proxy pointing to a component
     * @return a component representative for the pointed component
     * @throws Throwable an exception
     */
    public PAComponentRepresentative createComponentRepresentative(Proxy proxy) throws Throwable {
        // set immediate service for getComponentParameters
        System.out.println("PAComponentRepresentativeFactory.createComponentRepresentative()");
        proxy.reify(MethodCall.getComponentMethodCall(PAComponent.class.getDeclaredMethod(
                "setImmediateServices", new Class[] {}), new Object[] {}, null, Constants.COMPONENT, null,
                ComponentRequest.STRICT_FIFO_PRIORITY));

        ComponentParameters componentParameters = (ComponentParameters) proxy.reify(MethodCall
                .getComponentMethodCall(PAComponent.class.getDeclaredMethod("getComponentParameters",
                        new Class[] {}), new Object[] {}, null, Constants.COMPONENT, null,
                        ComponentRequest.STRICT_FIFO_PRIORITY));

        return PAComponentRepresentativeFactory.instance().createComponentRepresentative(componentParameters,
                proxy);
    }

    /**
     * The creation of a component representative (for a non-functional component) from a proxy object implies a remote invocation (immediate service) for
     * getting the parameters of the component, necessary for the construction of the representative
     * @param proxy a reference on a proxy pointing to a component
     * @return a component representative for the pointed component
     * @throws Throwable an exception
     */
    public PAComponentRepresentative createNFComponentRepresentative(Proxy proxy) throws Throwable {
        // set immediate service for getComponentParameters
        proxy.reify(MethodCall.getComponentMethodCall(PAComponent.class.getDeclaredMethod(
                "setImmediateServices", new Class[] {}), new Object[] {}, null, Constants.COMPONENT, null,
                ComponentRequest.STRICT_FIFO_PRIORITY));
        ComponentParameters componentParameters = (ComponentParameters) proxy.reify(MethodCall
                .getComponentMethodCall(PAComponent.class.getDeclaredMethod("getComponentParameters",
                        new Class[] {}), new Object[] {}, null, Constants.COMPONENT, null,
                        ComponentRequest.STRICT_FIFO_PRIORITY));

        return PAComponentRepresentativeFactory.instance().createNFComponentRepresentative(
                componentParameters.getComponentType(), componentParameters.getHierarchicalType(), proxy,
                componentParameters.getControllerDescription().getControllersConfigFileLocation());
    }
}
