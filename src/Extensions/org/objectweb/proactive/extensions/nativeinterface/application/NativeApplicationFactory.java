package org.objectweb.proactive.extensions.nativeinterface.application;

import java.io.Serializable;


/**
 * Interface that defines a NativeApplication factory, which contains
 * a message adaptater and a message handler
 * @author emathias
 *
 */
public interface NativeApplicationFactory extends Serializable {
    NativeMessageAdapter createMsgAdapter();

    NativeMessageHandler createMsgHandler();
}
