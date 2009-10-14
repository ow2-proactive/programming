package org.objectweb.proactive.extensions.nativeinterface.coupling;

/**
 *  Native wrapper that hold inbound and outbound proxies
 */
public class ProActiveNativeWrapper {

    private InboundProxy inboundProxy;
    private OutboundProxy outboundProxy;

    public ProActiveNativeWrapper(InboundProxy inboundProxy, OutboundProxy outboundProxy) {
        super();
        this.inboundProxy = inboundProxy;
        this.outboundProxy = outboundProxy;
    }

    public InboundProxy getInboundProxy() {
        return inboundProxy;
    }

    public void setInboundProxy(InboundProxy inboundProxy) {
        this.inboundProxy = inboundProxy;
    }

    public OutboundProxy getOutboundProxy() {
        return outboundProxy;
    }

    public void setOutboundProxy(OutboundProxy outboundProxy) {
        this.outboundProxy = outboundProxy;
    }

}
