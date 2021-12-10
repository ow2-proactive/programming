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
package org.objectweb.proactive.core.remoteobject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
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

        Iterator<RemoteObjectFactorySPI> iter = ServiceLoader.load(RemoteObjectFactorySPI.class).iterator();
        while (iter.hasNext()) {
            try {
                RemoteObjectFactorySPI remoteObjectFactorySPI = iter.next();

                String protoId = remoteObjectFactorySPI.getProtocolId();
                Class<? extends RemoteObjectFactory> cl = remoteObjectFactorySPI.getFactoryClass();

                if (!remoteObjectFactories.contains(protoId)) {
                    logger.debug("Remote Object Factory provider <" + protoId + ", " + cl + "> found");
                    remoteObjectFactories.put(protoId, cl);
                }
            } catch (Throwable err) {
                logger.error("Failed to load remote object factory: " + err);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[RemoteObjectProtocolFactory] Protocols registered : " + remoteObjectFactories.keySet());
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
