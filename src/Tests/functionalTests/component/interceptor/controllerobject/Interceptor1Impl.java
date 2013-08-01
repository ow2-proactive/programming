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
package functionalTests.component.interceptor.controllerobject;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.control.AbstractPAController;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.mop.MethodCall;

import functionalTests.component.controller.DummyController;


public class Interceptor1Impl extends AbstractPAController implements Interceptor1 {
    public Interceptor1Impl(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Interceptor1.INTERCEPTOR1_NAME,
                    Interceptor1.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    @Override
    public void setDummyValue(String value) {
        try {
            ((DummyController) getFcItfOwner().getFcInterface(DummyController.DUMMY_CONTROLLER_NAME))
                    .setDummyValue(value);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDummyValue() {
        try {
            return ((DummyController) getFcItfOwner().getFcInterface(DummyController.DUMMY_CONTROLLER_NAME))
                    .getDummyValue();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void beforeMethodInvocation(String interfaceName, MethodCall methodCall) {
        setDummyValue(getDummyValue() + Interceptor1.BEFORE_INTERCEPTION + interfaceName + "-" +
            methodCall.getName() + " - ");
    }

    @Override
    public void afterMethodInvocation(String interfaceName, MethodCall methodCall, Object result) {
        setDummyValue(getDummyValue() + Interceptor1.AFTER_INTERCEPTION + interfaceName + "-" +
            methodCall.getName() + " - ");
    }
}
