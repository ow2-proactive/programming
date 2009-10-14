package org.objectweb.proactive.extensions.nativeinterface.application;

import org.objectweb.proactive.extensions.nativeinterface.coupling.ProActiveNativeInterface;


/**
 * The NativeMessageHandler is responsible for handling messages
 *  (in general, deliver the message to native applications
 *   and send the reply by callback)
 *
 */
public interface NativeMessageHandler {

    public boolean handleMessage(ProActiveNativeInterface callback, NativeMessage message);

}
