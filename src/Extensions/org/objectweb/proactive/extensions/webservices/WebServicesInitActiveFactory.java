/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
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
