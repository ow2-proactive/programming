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
//@snippet-start component_userguide_14
package org.objectweb.proactive.examples.documentation.components;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.interception.InputInterceptor;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.mop.MethodCall;


public class MyInputInterceptor extends AbstractProActiveController implements InputInterceptor,
        ControllerItf {

    public MyInputInterceptor(Component owner) {
        super(owner);
    }

    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType("mycontroller",
                    ControllerItf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller" + this.getClass().getName());
        }
    }

    // foo is defined in the MyController interface
    public void foo() {
        // foo implementation
    }

    public void afterInputMethodInvocation(MethodCall methodCall) {
        System.out.println("post processing an intercepted an incoming functional invocation");
        // interception code
    }

    public void beforeInputMethodInvocation(MethodCall methodCall) {
        System.out.println("pre processing an intercepted an incoming functional invocation");
        // interception code
    }
}
//@snippet-end component_userguide_14

