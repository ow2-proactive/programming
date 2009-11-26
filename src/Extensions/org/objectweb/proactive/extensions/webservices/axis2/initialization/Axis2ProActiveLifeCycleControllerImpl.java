package org.objectweb.proactive.extensions.webservices.axis2.initialization;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.controller.ProActiveLifeCycleControllerImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public class Axis2ProActiveLifeCycleControllerImpl extends ProActiveLifeCycleControllerImpl {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    public Axis2ProActiveLifeCycleControllerImpl(Component owner) {
        super(owner);
    }

    public void startFc() throws IllegalLifeCycleException {
        try {
            Axis2Initializer.init();
        } catch (WebServicesException e) {
            logger.error("An exception occured while initializing Axis2", e);
        }
        super.startFc();
    }

}
