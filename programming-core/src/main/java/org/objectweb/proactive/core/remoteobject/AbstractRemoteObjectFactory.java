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
package org.objectweb.proactive.core.remoteobject;

import java.net.URI;
import java.util.Hashtable;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.httpserver.ClassServerServlet;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.URIBuilder;


/**
 *
 * This class provides helper methods for manipulation remote objects.
 *
 */
public abstract class AbstractRemoteObjectFactory implements RemoteObjectFactory {
    final protected static Hashtable<String, RemoteObjectFactory> activatedRemoteObjectFactories;

    static {
        activatedRemoteObjectFactories = new Hashtable<String, RemoteObjectFactory>();

        createClassServer();
    }

    /**
     *        create the class server -- mandatory for class file transfer
     */
    protected static synchronized void createClassServer() {
        // If HTTP class server is used add its URL to the codebase
        // Otherwise, the ProActiveRMIClassloader and the ProActiveRuntime use their
        // own ProActive codebase.
        if (CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.isTrue()) {
            ClassServerServlet classServerServlet = ClassServerServlet.get();
            String servletCodebase = classServerServlet.getCodeBase();

            // the class downloading protocol must be accorded to the
            // selected communication protocol,

            servletCodebase = adaptCodebaseToProtocol(servletCodebase);

            // Local class server is useful when an object migrate
            // Other class servers  are used only if local class server fail
            String oldCodebase = CentralPAPropertyRepository.JAVA_RMI_SERVER_CODEBASE.getValue();

            String newCodebase = null;
            if (oldCodebase != null) {
                // RMI support multiple class server locations
                newCodebase = servletCodebase + " " + oldCodebase;
            } else {
                newCodebase = servletCodebase;
            }
            if (!CentralPAPropertyRepository.JAVA_RMI_SERVER_USECODEBASEONLY.isSet()) {
                CentralPAPropertyRepository.JAVA_RMI_SERVER_USECODEBASEONLY.setValue(false);
            }
            CentralPAPropertyRepository.JAVA_RMI_SERVER_CODEBASE.setValue(newCodebase);

            // next line is commented out for PROACTIVE-928
            // we should *not* have to initialize a runtime when creating
            // the class server. Tests have shown that it is not
            // needed to far.
            //ProActiveRuntimeImpl.getProActiveRuntime();
        }
    }

    public static String adaptCodebaseToProtocol(String servletCodebase) {

        if (CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue().equals("rmissh")) {
            URI httpsshservletURI = URI.create(servletCodebase);
            httpsshservletURI = URIBuilder.setProtocol(httpsshservletURI, "httpssh");
            return httpsshservletURI.toString();
        }
        return servletCodebase;
    }

    /**
     * @param protocol The protocol schema or null to use the default protocol
     * @return return the remote object factory associated to the given protocol
     * @throws UnknownProtocolException
     */
    public static synchronized RemoteObjectFactory getRemoteObjectFactory(String protocol)
            throws UnknownProtocolException {
        if (protocol == null) {
            protocol = CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue();
        }

        try {
            RemoteObjectFactory rof = activatedRemoteObjectFactories.get(protocol);
            if (rof != null) {
                return rof;
            } else {
                Class<?> rofClazz = RemoteObjectProtocolFactoryRegistry.get(protocol);

                if (rofClazz != null) {
                    RemoteObjectFactory o = (RemoteObjectFactory) rofClazz.newInstance();

                    activatedRemoteObjectFactories.put(protocol, o);

                    return o;
                }
            }
        } catch (Throwable e) {
            throw new UnknownProtocolException("Failed to instanciate remote object factory for " + protocol,
                e);
        }

        throw new UnknownProtocolException("There is no RemoteObjectFactory defined for the protocol : " +
            protocol);
    }

    /** Return the default RemoteObjectFactory
     * 
     * The default ROF is controlled by the @link{CentralProperties.PA_COMMUNICATION_PROTOCOL} property.
     * 
     * @return return the remote object factory associated to the default protocol
     * @throws UnknownProtocolException if the default communication protocol is not known
     */
    public static RemoteObjectFactory getDefaultRemoteObjectFactory() throws UnknownProtocolException {
        String protocol = CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue();
        return getRemoteObjectFactory(protocol);
    }
}
