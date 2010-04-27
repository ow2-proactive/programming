package org.objectweb.proactive.extensions.webservices.cxf.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.extensions.webservices.component.controller.AbstractPAWebServicesControllerImpl;


public class PAWebServicesControllerImpl extends AbstractPAWebServicesControllerImpl {

    public PAWebServicesControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    protected String getFramework() {
        return "cxf";
    }
}
