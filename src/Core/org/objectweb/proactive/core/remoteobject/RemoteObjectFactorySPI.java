package org.objectweb.proactive.core.remoteobject;

public interface RemoteObjectFactorySPI {
    public String getProtocolId();

    public Class<? extends RemoteObjectFactory> getFactoryClass();
}
