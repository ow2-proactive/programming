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

