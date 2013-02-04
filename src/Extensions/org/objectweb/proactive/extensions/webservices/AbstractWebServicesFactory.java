/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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

import java.net.URI;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public abstract class AbstractWebServicesFactory implements WebServicesFactory {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    final protected static HashMap<String, WebServicesFactory> activatedWebServicesFactory;
    final protected static HashMap<URI, WebServices> activatedWebServices;

    static {
        activatedWebServicesFactory = new HashMap<String, WebServicesFactory>();
        activatedWebServices = new HashMap<URI, WebServices>();
    }

    protected AbstractWebServicesFactory() {
    }

    /**
     * @param frameWorkId web service framework
     * @return the unique instance of WebServicesFactory corresponding to the given framework
     * @throws UnknownFrameWorkException
     */
    public static WebServicesFactory getWebServicesFactory(String frameWorkId)
            throws UnknownFrameWorkException {

        if (frameWorkId == null) {
            frameWorkId = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
        }

        try {
            WebServicesFactory wsf = activatedWebServicesFactory.get(frameWorkId);
            if (wsf != null) {
                logger.debug("Getting the WebServicesFactory instance from the hashmap");
                return wsf;
            } else {
                logger.debug("Creating a new WebServicesFactory instance");
                Class<?> wsfClazz = WebServicesFrameWorkFactoryRegistry.get(frameWorkId);

                if (wsfClazz != null) {
                    WebServicesFactory o = (WebServicesFactory) wsfClazz.newInstance();

                    activatedWebServicesFactory.put(frameWorkId, o);

                    return o;
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        throw new UnknownFrameWorkException("There is no WebServicesFactory defined for the framework: " +
            frameWorkId);
    }

    /**
     * Return the default WebServicesFactory
     *
     * @return return the web service factory associated to the default framework
     * @throws UnknownFrameWorkException if the default framework is not known
     */
    public static WebServicesFactory getDefaultWebServicesFactory() throws UnknownFrameWorkException {
        String frameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
        return getWebServicesFactory(frameWork);
    }

    /**
     * @return the local Jetty port which is a random (except if proactive.http.port is set)
     */
    public static String getLocalPort() {
        HTTPServer httpServer = HTTPServer.get();
        return CentralPAPropertyRepository.PA_XMLHTTP_PORT.getValueAsString();
    }

    /**
     * @return the local Jetty URL
     */
    public static String getLocalUrl() {
        return "http://" + ProActiveInet.getInstance().getInetAddress().getHostAddress() + ":" +
            getLocalPort() + "/";
    }

    /**
     * Creates a new WebServices instance. Used in case when a WebServices object corresponding
     * to the given URL has not been instantiated yet.
     *
     * @param url
     * @return
     * @throws WebServicesException
     */
    abstract protected WebServices newWebServices(String url) throws WebServicesException;

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.WebServicesFactory#getWebServices(java.lang.String)
     */
    public final WebServices getWebServices(String url) throws WebServicesException {
        URI uriKey = null;
        try {
            URI uri = new URI(url);
            uriKey = URIBuilder.buildURI(uri.getHost(), null, uri.toURL().getProtocol(), uri.getPort(), true);
        } catch (Exception e) {
            throw new WebServicesException("An exception occured while reading the web service url", e);
        }
        WebServices ws = activatedWebServices.get(uriKey);
        if (ws != null) {
            logger.debug("Getting the WebServices instance from the hashmap");
            logger.debug("the new WebServices instance has been put into the HashMap using the uri key: " +
                uriKey.toString());
            return ws;
        } else {
            logger.debug("Creating a new WebServices instance");
            ws = newWebServices(url);
            activatedWebServices.put(uriKey, ws);
            logger.debug("The new WebServices instance has been put into the HashMap using the uri key: " +
                uriKey.toString());
            return ws;
        }
    }
}
