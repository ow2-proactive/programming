package org.objectweb.proactive.extensions.webservices.component;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;


public class Utils extends org.objectweb.proactive.core.component.Utils {

    /**
     * Returns the {@link PAWebServicesController} interface of the given component.
     *
     * @param component Reference on a component.
     * @return {@link PAWebServicesController} interface of the given component.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public static PAWebServicesController getPAWebServicesController(final Component component)
            throws NoSuchInterfaceException {
        return (PAWebServicesController) component
                .getFcInterface(PAWebServicesController.WEBSERVICES_CONTROLLER);
    }
}
