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
package org.objectweb.proactive.extensions.dataspaces.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.AlreadyConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesProperties;


/**
 * TechnicalService that configures ProActive node to support Data Spaces.
 * <p>
 * Configuration is read from two sources:
 * <ul>
 * <li>Node level configuration of scratch space is read from local runtime ProActive's properties (
 * {@link CentralPAPropertyRepository#PA_DATASPACES_SCRATCH_URLS} and
 * {@link CentralPAPropertyRepository#PA_DATASPACES_SCRATCH_PATH})</li>
 * <li>Application level configuration is read from technical service properties (
 * {@link #PROPERTY_APPLICATION_ID} abd {@link #PROPERTY_NAMING_SERVICE_URL}).</li>
 * </ul>
 * <p>
 * This implementation sets up Data Spaces in {@link #apply(Node)} and cleans up after Data Spaces
 * objects when Node is being destroyed. After Data Spaces are configured, user can safely use
 * {@link PADataSpaces} API.
 */
public class DataSpacesTechnicalService implements TechnicalService {

    private static final long serialVersionUID = 61L;

    public static final String PROPERTY_APPLICATION_ID = "proactive.dataspaces.application_id";

    public static final String PROPERTY_NAMING_SERVICE_URL = "proactive.dataspaces.naming_service_url";

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES_CONFIGURATOR);

    private String namingServiceURL;

    private Long appId;

    /**
     * Create technical service properties for given configuration that should initialize properly
     * this class.
     *
     * @param appId
     *            identifier of application
     * @param namingServiceURL
     *            URL of Naming Service
     * @return technical service properties for given configuration
     */
    public static TechnicalServicesProperties createTechnicalServiceProperties(final long appId,
            final String namingServiceURL) {
        final HashMap<String, String> dataSpacesProperties = new HashMap<String, String>();
        dataSpacesProperties.put(DataSpacesTechnicalService.PROPERTY_APPLICATION_ID, Long.toString(appId));
        dataSpacesProperties.put(DataSpacesTechnicalService.PROPERTY_NAMING_SERVICE_URL, namingServiceURL);

        final HashMap<String, HashMap<String, String>> techServicesMap = new HashMap<String, HashMap<String, String>>();
        techServicesMap.put(DataSpacesTechnicalService.class.getName(), dataSpacesProperties);
        return new TechnicalServicesProperties(techServicesMap);
    }

    // TODO idea: about polices determining whether to leave or remove data and directories in scratch after unexpected end of application.
    private static void closeNodeConfigIgnoreException(final Node node) {
        try {
            DataSpacesNodes.closeNodeConfig(node);
        } catch (NotConfiguredException x) {
            ProActiveLogger.logImpossibleException(logger, x);
        }
    }

    /**
     * Configures Data Spaces for an application on a node with configuration specified properties
     * and local ProActive's properties.
     * <p>
     * FIXME: Data Spaces cannot be already configured nor for a node nor for an application, which
     * is related to GCM deployment specification and lack of acquisition specification yet. After
     * it is implemented in GCM deployment, reconfiguration should be allowed.
     **/
    public void apply(final Node node) {
        if (!isProperlyInitialized())
            return;

        final BaseScratchSpaceConfiguration baseScratchConfiguration = readScratchConfiguration();
        try {
            DataSpacesNodes.configureNode(node, baseScratchConfiguration);
        } catch (IllegalArgumentException e) {
            ProActiveLogger.logImpossibleException(logger, e);
            return;
        } catch (AlreadyConfiguredException e) {
            ProActiveLogger.logImpossibleException(logger, e);
            // FIXME: it may happen when node acquisition will be implemented - and we have to handle that
            // in slightly better way ;) (see comment in javadoc)
            // ...or during more than one GCMA deployment on one machine if this TS is applied on local node
            return;
        } catch (ConfigurationException e) {
            logger.error("Could not configure Data Spaces. Possible configuration problem.", e);
            return;
        } catch (FileSystemException e) {
            logger.error("Could not initialize scratch space for a node - I/O error.", e);
            return;
        }

        try {
            NamingService stub = NamingService.createNamingServiceStub(namingServiceURL);
            DataSpacesNodes.configureApplication(node, appId, stub);
        } catch (NotConfiguredException e) {
            // it should not happen as we configure it above
            ProActiveLogger.logImpossibleException(logger, e);
            closeNodeConfigIgnoreException(node);
            return;
        } catch (URISyntaxException e) {
            // it should not happen as we check that on deployer
            ProActiveLogger.logImpossibleException(logger, e);
            closeNodeConfigIgnoreException(node);
            return;
        } catch (FileSystemException e) {
            logger.error("Could not initialize scratch space for an application on a node - I/O error.", e);
            closeNodeConfigIgnoreException(node);
            return;
        } catch (ProActiveException e) {
            logger.error("Could not contact Naming Service specified by an application.", e);
            closeNodeConfigIgnoreException(node);
            return;
        }

        registerNotificationListener(node);
    }

    /**
     * Requires application id and NamingService URL properties: {@link #PROPERTY_APPLICATION_ID}
     * and {@link #PROPERTY_NAMING_SERVICE_URL}.
     **/
    public void init(Map<String, String> argValues) {
        final String appIdString = argValues.get(PROPERTY_APPLICATION_ID);
        if (appIdString == null) {
            logger
                    .error("Initialization error - provided TS properties are incomplete, application id is not specified.");
        } else {
            try {
                appId = Long.parseLong(appIdString);
            } catch (NumberFormatException x) {
                ProActiveLogger.logImpossibleException(logger, x);
            }
        }

        namingServiceURL = argValues.get(PROPERTY_NAMING_SERVICE_URL);
        if (namingServiceURL == null) {
            logger
                    .error("Initialization error - provided TS properties are incomplete, NamingService URL is not specified.");
        }
    }

    private boolean isProperlyInitialized() {
        return namingServiceURL != null && appId != null;
    }

    private void registerNotificationListener(final Node node) {
        final String runtimeURL = node.getProActiveRuntime().getURL();
        final ObjectName mBeanObjectName = FactoryName.createRuntimeObjectName(runtimeURL);
        final NotificationListener notificationListener = new NotificationListener() {

            public void handleNotification(Notification notification, Object handback) {
                final String type = notification.getType();
                final Object userData = notification.getUserData();

                if (type.equals(NotificationType.nodeDestroyed) &&
                    userData.equals(node.getNodeInformation().getURL())) {
                    // FIXME: it seems that subscribe/unsubscribe is buggy: depends on PROACTIVE-687
                    JMXNotificationManager.getInstance().unsubscribe(mBeanObjectName, this);
                    closeNodeConfigIgnoreException(node);
                }
            }

        };
        JMXNotificationManager.getInstance().subscribe(mBeanObjectName, notificationListener);
    }

    private BaseScratchSpaceConfiguration readScratchConfiguration() {
        final String scratchPath = CentralPAPropertyRepository.PA_DATASPACES_SCRATCH_PATH.getValue();
        final List<String> scratchURLlist = CentralPAPropertyRepository.PA_DATASPACES_SCRATCH_URLS.getValue();

        if (scratchURLlist == null && scratchPath == null) {
            logger.warn("No scratch space configuration specified for this node.");
            return null;
        }
        String[] scratchURLs = null;
        if (scratchURLlist != null) {
            scratchURLs = scratchURLlist.toArray(new String[0]);
        }

        try {
            return new BaseScratchSpaceConfiguration(scratchURLs, scratchPath);
        } catch (ConfigurationException e) {
            // it should not happen as we check it above
            ProActiveLogger.logImpossibleException(logger, e);
            return null;
        }
    }
}
