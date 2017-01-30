/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.rmi;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RegistryHelper {
    static Logger logger = ProActiveLogger.getLogger(Loggers.RMI);

    protected final static int DEFAULT_REGISTRY_PORT = 1099;

    /**
     * settings of the registry
     */
    protected int registryPortNumber = DEFAULT_REGISTRY_PORT;

    protected boolean shouldCreateRegistry = true;

    protected boolean registryChecked;

    protected static java.rmi.registry.Registry registry;

    //following boolean is used to know which runtime has created RMI registry
    protected static boolean registryCreator = false;

    //
    // -- Constructors -----------------------------------------------
    //
    public RegistryHelper() {
        String port = CentralPAPropertyRepository.PA_RMI_PORT.getValueAsString();
        if (port != null) {
            setRegistryPortNumber(new Integer(port).intValue());
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public int getRegistryPortNumber() {
        return registryPortNumber;
    }

    public void setRegistryPortNumber(int v) {
        registryPortNumber = v;
        registryChecked = false;
    }

    public boolean shouldCreateRegistry() {
        return shouldCreateRegistry;
    }

    public void setShouldCreateRegistry(boolean v) {
        shouldCreateRegistry = v;
    }

    public synchronized void initializeRegistry() throws java.rmi.RemoteException {
        try {
            if (!shouldCreateRegistry) {
                return; // don't bother
            }
            if (registryChecked) {
                return; // already done for this VM
            }
            getOrCreateRegistry(registryPortNumber);
            registryChecked = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static java.rmi.registry.Registry getRegistry() {
        return registry;
    }

    public static boolean getRegistryCreator() {
        return registryCreator;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    // 
    private static java.rmi.registry.Registry createRegistry(int port) throws java.rmi.RemoteException {
        registry = java.rmi.registry.LocateRegistry.createRegistry(port);
        registryCreator = true;
        return registry;
    }

    private static java.rmi.registry.Registry detectRegistry(int port) {
        // java.rmi.registry.Registry registry = null;
        try {
            // whether an effective registry exists or not we should get a reference
            registry = java.rmi.registry.LocateRegistry.getRegistry(ProActiveInet.getInstance().getHostname(), port);
            if (registry == null) {
                return null;
            }

            // doing a lookup should produce ConnectException if registry doesn't exist
            // and no exception or NotBoundException if the registry does exist.
            registry.lookup("blah!");
            logger.info("Detected an existing RMI Registry on port " + port);
            return registry;
        } catch (java.rmi.NotBoundException e) {
            logger.info("Detected an existing RMI Registry on port " + port);
            return registry;
        } catch (java.rmi.RemoteException e) {
            return null;
        }
    }

    private static java.rmi.registry.Registry getOrCreateRegistry(int port) throws java.rmi.RemoteException {
        registry = detectRegistry(port);
        if (registry != null) {
            return registry;
        }

        // no registry created
        try {
            registry = createRegistry(port);
            logger.info("Created a new registry on port " + port);
            return registry;
        } catch (java.rmi.RemoteException e) {
            // problem to bind the registry : may be somebody created one in the meantime
            // try to find the rmi registry one more time
            registry = detectRegistry(port);
            if (registry != null) {
                return registry;
            }
            logger.error("Cannot detect an existing RMI Registry on port " + port + " nor create one e=" + e);
            throw e;
        }
    }
}
