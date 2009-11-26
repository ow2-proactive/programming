package org.objectweb.proactive.extensions.webservices.cxf.initialization;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesInitActive;


public class CXFInitActive extends AbstractWebServicesInitActive {

    public void initActivity(Body body) {
        CXFInitializer.init();
    }
}
