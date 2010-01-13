/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.core.remoteobject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.ibis.IbisRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmissh.RmiSshRemoteObjectFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectProtocolFactoryRegistry {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);
    protected static Hashtable<String, Class<? extends RemoteObjectFactory>> remoteObjectFactories;

    static {
        // set the default supported protocols
        remoteObjectFactories = new Hashtable<String, Class<? extends RemoteObjectFactory>>();
        remoteObjectFactories.put(Constants.RMI_PROTOCOL_IDENTIFIER, RmiRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.XMLHTTP_PROTOCOL_IDENTIFIER, HTTPRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.RMISSH_PROTOCOL_IDENTIFIER, RmiSshRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.IBIS_PROTOCOL_IDENTIFIER, IbisRemoteObjectFactory.class);

        Iterator<RemoteObjectFactorySPI> iter = ServiceRegistry.lookupProviders(RemoteObjectFactorySPI.class);
        while (iter.hasNext()) {
            RemoteObjectFactorySPI remoteObjectFactorySPI = iter.next();

            String protoId = remoteObjectFactorySPI.getProtocolId();
            Class<? extends RemoteObjectFactory> cl = remoteObjectFactorySPI.getFactoryClass();

            if (!remoteObjectFactories.contains(protoId)) {
                logger.info("Remote Object Factory provider <" + protoId + ", " + cl + "> found");
                remoteObjectFactories.put(protoId, cl);
            }
        }
    }

    public static void put(String protocol, Class<? extends RemoteObjectFactory> factory) {
        remoteObjectFactories.put(protocol, factory);
    }

    public static void remove(String protocol) {
        remoteObjectFactories.remove(protocol);
    }

    public static Class<? extends RemoteObjectFactory> get(String protocol) {
        return remoteObjectFactories.get(protocol);
    }

    public static Enumeration<String> keys() {
        return remoteObjectFactories.keys();
    }
}
