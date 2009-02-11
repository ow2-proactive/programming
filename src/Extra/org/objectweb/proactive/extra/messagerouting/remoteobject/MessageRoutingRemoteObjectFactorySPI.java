package org.objectweb.proactive.extra.messagerouting.remoteobject;

import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactorySPI;


/** 
 *
 * @since ProActive 4.1.0
 */
public class MessageRoutingRemoteObjectFactorySPI implements RemoteObjectFactorySPI {

    public Class<? extends RemoteObjectFactory> getFactoryClass() {
        return MessageRoutingRemoteObjectFactory.class;
    }

    public String getProtocolId() {
        return "pamr";
    }

}
