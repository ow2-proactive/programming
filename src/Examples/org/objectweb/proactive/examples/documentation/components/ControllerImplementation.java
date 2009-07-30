package org.objectweb.proactive.examples.documentation.components;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


public class ControllerImplementation extends AbstractProActiveController implements ControllerItf {

    //@snippet-start component_userguide_11
    public ControllerImplementation(Component owner) {
        super(owner);
    }

    //@snippet-end component_userguide_11

    //@snippet-start component_userguide_12
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType("Name of the controller",
                    ControllerItf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type: " + this.getClass().getName());
        }
    }

    //@snippet-end component_userguide_12

    public void foo() {
        // TODO Auto-generated method stub
    }
}
