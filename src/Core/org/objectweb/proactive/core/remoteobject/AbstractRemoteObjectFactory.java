/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject;

import java.util.Hashtable;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.httpserver.ClassServerServlet;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


/**
 *
 * This class provides helper methods for manipulation remote objects.
 *
 */
public abstract class AbstractRemoteObjectFactory implements RemoteObjectFactory {
    final protected static Hashtable<String, RemoteObjectFactory> activatedRemoteObjectFactories;

    static {
        activatedRemoteObjectFactories = new Hashtable<String, RemoteObjectFactory>();

        if ((System.getSecurityManager() == null) && PAProperties.PA_SECURITYMANAGER.isTrue()) {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
        }

        createClassServer();
    }

    /**
     *        create the class server -- mandatory for class file transfer
     */
    protected static synchronized void createClassServer() {
        // If HTTP class server is used add its URL to the codebase
        // Otherwise, the ProActiveRMIClassloader and the ProActiveRuntime use their
        // own ProActive codebase.
        if (PAProperties.PA_CLASSLOADING_USEHTTP.isTrue()) {
            ClassServerServlet classServerServlet = ClassServerServlet.get();
            String servletCodebase = classServerServlet.getCodeBase();

            // Local class server is useful when an object migrate
            // Other class servers  are used only if local class server fail
            String oldCodebase = PAProperties.JAVA_RMI_SERVER_CODEBASE.getValue();

            String newCodebase = null;
            if (oldCodebase != null) {
                // RMI support multiple class server locations
                newCodebase = servletCodebase + " " + oldCodebase;
            } else {
                newCodebase = servletCodebase;
            }
            PAProperties.JAVA_RMI_SERVER_CODEBASE.setValue(newCodebase);
            ProActiveRuntimeImpl.getProActiveRuntime();
        }
    }

    /**
     * @param protocol The protocol schema or null to use the default protocol
     * @return return the remote object factory associated to the given protocol
     * @throws UnknownProtocolException
     */
    public static RemoteObjectFactory getRemoteObjectFactory(String protocol) throws UnknownProtocolException {
        if (protocol == null) {
            protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
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
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        throw new UnknownProtocolException("There is no RemoteObjectFactory defined for the protocol : " +
            protocol);
    }

    /** Return the default RemoteObjectFactory
     * 
     * The default ROF is controlled by the @link{PAProperties.PA_COMMUNICATION_PROTOCOL} property.
     * 
     * @return return the remote object factory associated to the default protocol
     * @throws UnknownProtocolException if the default communication protocol is not known
     */
    public static RemoteObjectFactory getDefaultRemoteObjectFactory() throws UnknownProtocolException {
        String protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        return getRemoteObjectFactory(protocol);
    }
}
