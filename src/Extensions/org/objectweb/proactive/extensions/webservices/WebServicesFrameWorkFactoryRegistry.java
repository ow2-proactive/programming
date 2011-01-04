/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.webservices;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.axis2.Axis2WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.cxf.CXFWebServicesFactory;


/**
 * @author The ProActive Team
 *
 */
public class WebServicesFrameWorkFactoryRegistry {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);
    protected static Hashtable<String, Class<? extends WebServicesFactory>> webServicesFactories;

    static {
        // set the default supported framework
        webServicesFactories = new Hashtable<String, Class<? extends WebServicesFactory>>();
        webServicesFactories.put("axis2", Axis2WebServicesFactory.class);
        webServicesFactories.put("cxf", CXFWebServicesFactory.class);

        // add the WebServicesFactory at runtime using the WebServicesFactorySPI class
        Iterator<WebServicesFactorySPI> iter = ServiceRegistry.lookupProviders(WebServicesFactorySPI.class);
        while (iter.hasNext()) {
            WebServicesFactorySPI webServicesFactorySPI = iter.next();

            String frameWorkId = webServicesFactorySPI.getFrameWorkId();
            Class<? extends WebServicesFactory> cl = webServicesFactorySPI.getFactoryClass();

            if (!webServicesFactories.contains(frameWorkId)) {
                logger.debug("Web Service Factory provider <" + frameWorkId + ", " + cl + "> found");
                webServicesFactories.put(frameWorkId, cl);
            }
        }
    }

    public static void put(String framework, Class<? extends WebServicesFactory> factory) {
        webServicesFactories.put(framework, factory);
    }

    public static void remove(String framework) {
        webServicesFactories.remove(framework);
    }

    public static Class<? extends WebServicesFactory> get(String frameWork) {
        return webServicesFactories.get(frameWork);
    }

    public static Enumeration<String> keys() {
        return webServicesFactories.keys();
    }

    public static boolean isValidFrameWork(String frameWork) {
        return webServicesFactories.containsKey(frameWork);
    }
}
