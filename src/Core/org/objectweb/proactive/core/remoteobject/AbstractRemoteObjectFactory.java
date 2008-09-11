/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
public abstract class AbstractRemoteObjectFactory {
    final protected static Hashtable<String, RemoteObjectFactory> activatedRemoteObjectFactories;

    static {
        activatedRemoteObjectFactories = new Hashtable<String, RemoteObjectFactory>();
        createClassServer();
    }

    /**
     * insert a new location within the codebase property
     * @param newLocationURL the new location to add
     * @return the new codebase
     */
    protected static synchronized String addCodebase(String newLocationURL) {
        // Local class server is useful when an object migrate
        // Other class servers  are used only if local class server fail
        String oldCodebase = PAProperties.JAVA_RMI_SERVER_CODEBASE.getValue();
        ProActiveRuntimeImpl.getProActiveRuntime();
        String newCodebase = null;
        if (oldCodebase != null) {
            // RMI support multiple class server locations
            newCodebase = newLocationURL + " " + oldCodebase;
        } else {
            newCodebase = newLocationURL;
        }

        PAProperties.JAVA_RMI_SERVER_CODEBASE.setValue(newCodebase);
        return newCodebase;
    }

    /**
     *        create the class server -- mandatory for class file transfer
     */
    protected static synchronized void createClassServer() {
        ClassServerServlet classServerServlet = ClassServerServlet.get();
        String codebase = classServerServlet.getCodeBase();
        addCodebase(codebase);
    }

    /**
     * @param protocol
     * @return return the remote object factory associated to the given protocol
     * @throws UnknownProtocolException
     */
    public static RemoteObjectFactory getRemoteObjectFactory(String protocol) throws UnknownProtocolException {
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

        throw new UnknownProtocolException("there is no RemoteObjectFactory defined for the protocol : " +
            protocol);
    }
}
