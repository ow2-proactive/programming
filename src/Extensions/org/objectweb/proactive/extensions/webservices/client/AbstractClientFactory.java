/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.extensions.webservices.client;

import java.net.URI;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.client.axis2.Axis2ClientFactory;
import org.objectweb.proactive.extensions.webservices.client.cxf.CXFClientFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public abstract class AbstractClientFactory implements ClientFactory {

    static private Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    final protected static HashMap<String, ClientFactory> activatedClientFactory;
    final protected static HashMap<String, Class<? extends ClientFactory>> clientFactories;
    final protected static HashMap<URI, Client> activatedClients;

    static {
        activatedClientFactory = new HashMap<String, ClientFactory>();
        clientFactories = new HashMap<String, Class<? extends ClientFactory>>();
        activatedClients = new HashMap<URI, Client>();
        clientFactories.put("axis2", Axis2ClientFactory.class);
        clientFactories.put("cxf", CXFClientFactory.class);
    }

    protected AbstractClientFactory() {
    }

    /**
     * Gets a ClientFactory instance corresponding to the framework id
     *
     * @param frameWorkId id of the framework
     * @return a instance of a ClientFactory
     * @throws UnknownFrameWorkException
     */
    public static ClientFactory getClientFactory(String frameWorkId) throws UnknownFrameWorkException {
        if (frameWorkId == null) {
            frameWorkId = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
        }

        try {
            ClientFactory cf = activatedClientFactory.get(frameWorkId);
            if (cf != null) {
                return cf;
            } else {
                Class<?> cfClazz = clientFactories.get(frameWorkId);

                if (cfClazz != null) {
                    ClientFactory o = (ClientFactory) cfClazz.newInstance();

                    activatedClientFactory.put(frameWorkId, o);

                    return o;
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        throw new UnknownFrameWorkException("There is no ClientFactory defined for the framework: " +
            frameWorkId);
    }

    /** Return the default ClientFactory
     *
     * @return return the client factory associated to the default framework
     * @throws UnknownFrameWorkException if the default framework is not known
     */
    public static ClientFactory getDefaultClientFactory() throws UnknownFrameWorkException {
        String frameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
        return getClientFactory(frameWork);
    }

    /**
     * Gets the wsdl address corresponding to the service. This method is in particular used
     * to generate the HashMap key for the client.
     *
     * @param url
     * @param serviceName
     * @return the wsdl address
     * @throws WebServicesException
     */
    public URI getWsdl(String url, String serviceName) throws WebServicesException {
        String wsdl = url + WSConstants.SERVICES_PATH + serviceName + "?wsdl";
        try {
            URI wsdlUri = new URI(wsdl);
            URI builtWsdlUri = URIBuilder.buildURI(wsdlUri.getHost(), wsdlUri.getPath(), wsdlUri.toURL()
                    .getProtocol(), wsdlUri.getPort(), true);
            return builtWsdlUri;
        } catch (Exception e) {
            throw new WebServicesException("An exception occured while trying to read the client url", e);
        }
    }

    /**
     * Creates a new instance of Client.
     *
     * @param url Url of the service
     * @param serviceName Name of the service
     * @param serviceClass Class of the service
     * @return
     * @throws WebServicesException
     */
    abstract protected Client newClient(String url, String serviceName, Class<?> serviceClass)
            throws WebServicesException;

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.client.ClientFactory#getClient(java.lang.String, java.lang.String, java.lang.Class)
     */
    public final Client getClient(String url, String serviceName, Class<?> serviceClass)
            throws WebServicesException {
        URI wsdlUri = getWsdl(url, serviceName);
        Client client = activatedClients.get(wsdlUri);
        if (client != null) {
            logger.debug("Getting the Client instance from the hashmap");
            logger.debug("the new Client instance has been put into the HashMap using the uri key: " +
                wsdlUri.toString());
            return client;
        } else {
            logger.debug("Creating a new Client instance");
            client = newClient(url, serviceName, serviceClass);
            activatedClients.put(wsdlUri, client);
            logger.debug("The new Client instance has been put into the HashMap using the uri key: " +
                wsdlUri.toString());
            return client;
        }
    }

}
