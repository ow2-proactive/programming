package org.objectweb.proactive.extensions.webservices;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.axis2.initialization.Axis2InitActive;
import org.objectweb.proactive.extensions.webservices.cxf.initialization.CXFInitActive;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;


public class WebServicesInitActiveFactory {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);
    protected static Hashtable<String, AbstractWebServicesInitActive> webServicesInitActiveInstances;
    protected static Hashtable<String, Class<? extends AbstractWebServicesInitActive>> webServicesInitActiveClasses;

    static {
        webServicesInitActiveInstances = new Hashtable<String, AbstractWebServicesInitActive>();
        // set the default supported framework
        webServicesInitActiveClasses = new Hashtable<String, Class<? extends AbstractWebServicesInitActive>>();
        webServicesInitActiveClasses.put("axis2", Axis2InitActive.class);
        webServicesInitActiveClasses.put("cxf", CXFInitActive.class);
    }

    public static AbstractWebServicesInitActive getInitActive(String frameWorkId)
            throws UnknownFrameWorkException {

        if (frameWorkId == null) {
            frameWorkId = PAProperties.PA_WEBSERVICES_FRAMEWORK.getValue();
        }

        try {
            AbstractWebServicesInitActive awsia = webServicesInitActiveInstances.get(frameWorkId);
            if (awsia != null) {
                logger.debug("Getting the AbstractWebServicesInitActive instance from the hashmap");
                return awsia;
            } else {
                logger.debug("Creating a new AbstractWebServicesInitActive instance");
                Class<?> iaClazz = webServicesInitActiveClasses.get(frameWorkId);

                if (iaClazz != null) {
                    AbstractWebServicesInitActive activity = (AbstractWebServicesInitActive) iaClazz
                            .newInstance();

                    webServicesInitActiveInstances.put(frameWorkId, activity);

                    return activity;
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        throw new UnknownFrameWorkException(
            "There is no AbstractWebServicesInitActive defined for the framework: " + frameWorkId);
    }
}
