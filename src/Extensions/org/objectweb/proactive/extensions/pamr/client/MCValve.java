package org.objectweb.proactive.extensions.pamr.client;

import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


public class MCValve implements Valve {
    ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<Long, Long>();

    public String getInfo() {
        return "Print statistics about calls";
    }

    public Message invokeIncoming(Message message) {
        long messageId = message.getMessageID();
        switch (message.getType()) {
            case DATA_REQUEST:
                break;
            default:
                // We don't care
                break;
        }

        return message;
    }

    public Message invokeOutgoing(Message message) {

        // TODO Auto-generated method stub
        return null;
    }

}
