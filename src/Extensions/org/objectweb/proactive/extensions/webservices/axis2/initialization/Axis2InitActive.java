package org.objectweb.proactive.extensions.webservices.axis2.initialization;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesInitActive;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public class Axis2InitActive extends AbstractWebServicesInitActive {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    public void initActivity(Body body) {
        try {
            Axis2Initializer.init();
        } catch (WebServicesException e) {
            logger.error("An exception occured while initializing Axis2", e);
        }
    }
}
