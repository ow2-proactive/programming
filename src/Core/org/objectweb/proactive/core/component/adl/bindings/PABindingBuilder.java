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
package org.objectweb.proactive.core.component.adl.bindings;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.bindings.BindingBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.representative.PANFComponentRepresentative;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * ProActive based implementation of the {@link BindingBuilder} interface.
 * 
 * Uses the GCM API to bind functional interfaces (F bindings),
 * and also handles WebService Bindings, and non functional interfaces (NF bindings).
 *
 * @author The ProActive Team
 */
public class PABindingBuilder implements PABindingBuilderItf {
    public static final int WEBSERVICE_BINDING = 3;
    public static final int MEMBRANE_BINDING = 4;

    protected static final Logger loggerADL = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    public void bindComponent(int type, Object client, String clientItf, Object server, String serverItf,
            Object context) throws Exception {
        // default: isFunctional = true
        bindComponent(type, client, clientItf, server, serverItf, null, true, context);
    }

    @Override
    public void bindComponent(int type, Object client, String clientItf, Object server, String serverItf,
            Object membraneOwner, boolean isFunctional, Object context) throws Exception {

        String clientName = "", serverName = "";
        if (client instanceof PAComponentRepresentative) {
            clientName = ((PAComponentRepresentative) client).getComponentParameters().getName();
        }
        if (server instanceof PAComponentRepresentative) {
            serverName = ((PAComponentRepresentative) server).getComponentParameters().getName();
        }
        loggerADL.debug("[PABindingBuilder] Binding (" + type + ") " + (isFunctional ? "F" : "NF") + " " +
            clientName + "." + clientItf + " --> " + serverName + "." + serverItf);

        // F binding
        if (isFunctional) {
            BindingController bc = GCM.getBindingController((Component) client);
            // regular functional binding
            if (type != WEBSERVICE_BINDING) {
                Object destinationItf;
                if (type == IMPORT_BINDING) {
                    destinationItf = GCM.getContentController((Component) server).getFcInternalInterface(
                            serverItf);
                } else {
                    destinationItf = ((Component) server).getFcInterface(serverItf);
                }
                bc.bindFc(clientItf, destinationItf);
            }
            // web-service binding
            else {
                bc.bindFc(clientItf, serverItf);
            }
        }
        // NF binding
        else {
            PAMembraneController pamcClient;
            try {
                pamcClient = Utils.getPAMembraneController((Component) client);
            } catch (NoSuchInterfaceException e) {
                pamcClient = null;
            }
            PAMembraneController pamcServer;
            try {
                pamcServer = Utils.getPAMembraneController((Component) server);
            } catch (NoSuchInterfaceException e) {
                pamcServer = null;
            }

            // the current definition of bindings inside the membrane require to specify the server interface as "component.interface"
            NameController ncServer = GCM.getNameController((Component) server);
            NameController ncClient = GCM.getNameController((Component) client);
            NameController ncMembraneOwner = null;
            String membraneOwnerName = "---";
            if (membraneOwner != null) {
                ncMembraneOwner = GCM.getNameController((Component) membraneOwner);
                membraneOwnerName = ncMembraneOwner.getFcName();
            }

            loggerADL.debug("[PABindingBuilder] MembraneOwner: " + membraneOwnerName);
            if (pamcClient == null) {
                loggerADL.debug("[PABindingBuilder] Client " + ncClient.getFcName() +
                    " does not have MEMBRANE-controller");
            }
            if (pamcServer == null) {
                loggerADL.debug("[PABindingBuilder] Server " + ncServer.getFcName() +
                    " does not have MEMBRANE-controller");
            }

            PAMembraneController pamc;
            try {
                pamc = Utils.getPAMembraneController((Component) membraneOwner);
            } catch (NoSuchInterfaceException e) {
                pamc = null;
            }

            // Check each type of binding
            if (type == NORMAL_BINDING) {
                clientItf = ncClient.getFcName() + "." + clientItf;
                serverItf = ncServer.getFcName() + "." + serverItf;
            }
            if (type == EXPORT_BINDING) {
                serverItf = ncServer.getFcName() + "." + serverItf;
            }
            if (type == IMPORT_BINDING) {
                clientItf = ncClient.getFcName() + "." + clientItf;
            }
            if (type == MEMBRANE_BINDING) {
                // nothing to change in the interfaces names
            }

            if (!(client instanceof PANFComponentRepresentative) &&
                !(server instanceof PANFComponentRepresentative) && (client != membraneOwner) &&
                (server != membraneOwner)) {
                // special case: the bindings between NF interfaces of two F subcomponents, must be carried on by the MembraneController
                // of the client component, and not by their owner
                if (pamcClient != null) {
                    Object destInterface = null;
                    int index = serverItf.indexOf(".");
                    if (index != -1) {
                        destInterface = ((PAComponent) server).getFcInterface(serverItf.substring(index + 1));
                    } else {
                        // should not enter in this case, because this situation should be classified as NORMAL_BINDING
                        // unless, a new type of binding have been added (to consider)
                        destInterface = ((PAComponent) server).getFcInterface(serverItf);
                    }
                    index = clientItf.indexOf(".");
                    if (index != -1) {
                        // calling nfBindFc using interface of the server component
                        loggerADL.debug("[PABindingBuilder] Calling nfBindFc(" + clientItf + ", ITF" +
                            serverItf + ")");
                        pamcClient.nfBindFc(clientItf.substring(index + 1), destInterface);
                    } else {
                        // same as above ... shouldn't enter here, but we should consider adding a new type of binding to clean this part
                        // calling nfBindFc using interface of the server component
                        loggerADL.debug("[PABindingBuilder] Calling nfBindFc(" + clientItf + ", ITF" +
                            serverItf + ")");
                        pamcClient.nfBindFc(clientItf, destInterface);
                    }
                } else {
                    throw new NoSuchInterfaceException("[PABindingBuilder] The client component " +
                        ncClient.getFcName() + " has no MembraneController.");
                }
            } else {
                // Call nfBinFc with the strings indicating component and interface
                // TODO: not handling webservice NF bindings
                loggerADL.debug("[PABindingBuilder] Calling nfBindFc(" + clientItf + ", " + serverItf + ")");
                pamc.nfBindFc(clientItf, serverItf);

            }

        }
    }
}
