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
package org.objectweb.proactive.core.descriptor.services;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents the fault-tolerance configuration for a
 * virtual node, i.e, url of ft servers and ft values such as ttc
 * @version 1.0,  2005/01/20
 * @since   ProActive 2.2
 * @author The ProActive Team
 */
public class FaultToleranceService implements UniversalService {
    public static final String FT_SERVICE_NAME = "Fault-Tolerance Service";
    public static final String DEFAULT_PARAM_LINE = "-Dproactive.ft=false";

    // logger
    final protected static Logger logger = ProActiveLogger.getLogger(Loggers.FAULT_TOLERANCE);

    // protocol Type
    private String protocolType;

    // fault tolerance servers
    private String recoveryProcessURL;
    private String checkpointServerURL;
    private String locationServerURL;
    private String globalServerURL; // priority on the 3 upper values

    // fault tolerance values
    private String ttcValue;

    // attached ressource server
    private String attachedResourceServer; // null if not registred has a ressource node

    public FaultToleranceService() {
        super();
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     * Nothing to do for ft service
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        return null;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return FaultToleranceService.FT_SERVICE_NAME;
    }

    /**
     * Build the java properties sequence corresponding to the set values.
     * @return the java properties sequence corresponding to the set values.
     */
    public List<String> buildParamsLine() {
        final ArrayList<String> line = new ArrayList<String>();
        line.add("-Dproactive.ft=true");
        if (this.getGlobalServerURL() != null) {
            if ((this.getCheckpointServerURL() != null) || (this.getLocationServerURL() != null) ||
                (this.getRecoveryProcessURL() != null)) {
                logger.warn("A global server is set : other servers are ignored !!");
            }
            line.add("-Dproactive.ft.server.global=" + this.getGlobalServerURL());
        } else {
            line.add("-Dproactive.ft.server.checkpoint=" + this.getCheckpointServerURL());
            line.add("-Dproactive.ft.server.recovery=" + this.getRecoveryProcessURL());
            line.add("-Dproactive.ft.server.location=" + this.getLocationServerURL());
        }

        if (this.getTtcValue() != null) {
            line.add("-Dproactive.ft.ttc=" + this.getTtcValue());
        }
        if (this.getAttachedResourceServer() != null) {
            line.add("-Dproactive.ft.server.resource=" + this.getAttachedResourceServer());
        }
        if (this.getProtocolType() != null) {
            line.add("-Dproactive.ft.protocol=" + this.getProtocolType());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("FT config is : " + line);
        }
        return line;
    }

    /**
     * Register all nodes in the RessourceServer. Do nothing if the ressource server is null.
     * @param nodes array of nodes that must be registered in the ressource server
     * @return true if nodes has been registered, false otherwise.
     */
    public boolean registerRessources(Node[] nodes) {
        if (this.getAttachedResourceServer() != null) {
            try {
                ResourceServer rs = (ResourceServer) (Naming.lookup(this.getAttachedResourceServer()));
                for (int i = 0; i < nodes.length; i++) {
                    rs.addFreeNode(nodes[i]);
                }
                return true;
            } catch (MalformedURLException e) {
                logger.error("**ERROR** RessourceServer unreachable : ressource is not registred." + e);
            } catch (RemoteException e) {
                logger.error("**ERROR** RessourceServer unreachable : ressource is not registred" + e);
            } catch (NotBoundException e) {
                logger.error("**ERROR** RessourceServer unreachable : ressource is not registred" + e);
            }
            return false;
        } else {
            return false;
        }
    }

    // Getters and setters
    public String getAttachedResourceServer() {
        return attachedResourceServer;
    }

    public void setAttachedResourceServer(String attachedRessourceServer) {
        this.attachedResourceServer = attachedRessourceServer;
    }

    public String getCheckpointServerURL() {
        return checkpointServerURL;
    }

    public void setCheckpointServerURL(String checkpointServerURL) {
        this.checkpointServerURL = checkpointServerURL;
    }

    public String getGlobalServerURL() {
        return globalServerURL;
    }

    public void setGlobalServerURL(String globalServerURL) {
        this.globalServerURL = globalServerURL;
    }

    public String getLocationServerURL() {
        return locationServerURL;
    }

    public void setLocationServerURL(String locationServerURL) {
        this.locationServerURL = locationServerURL;
    }

    public String getRecoveryProcessURL() {
        return recoveryProcessURL;
    }

    public void setRecoveryProcessURL(String recoveryProcessURL) {
        this.recoveryProcessURL = recoveryProcessURL;
    }

    public String getTtcValue() {
        return ttcValue;
    }

    public void setTtcValue(String ttcValue) {
        this.ttcValue = ttcValue;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }
}
