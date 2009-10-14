package org.objectweb.proactive.extensions.nativeinterfacempi;

import org.objectweb.proactive.extensions.nativeinterface.application.NativeApplicationFactory;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageAdapter;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageHandler;


public class MpiApplicationFactory implements NativeApplicationFactory {

    public NativeMessageAdapter createMsgAdapter() {
        return new ProActiveMPIMessageAdapter();
    }

    public NativeMessageHandler createMsgHandler() {
        return new ProActiveMPIMessageHandler();
    }

}
