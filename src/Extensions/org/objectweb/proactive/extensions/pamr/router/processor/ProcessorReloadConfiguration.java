package org.objectweb.proactive.extensions.pamr.router.processor;

import java.nio.ByteBuffer;

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.ReloadConfigurationMessage;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;


public class ProcessorReloadConfiguration extends Processor {

    public ProcessorReloadConfiguration(ByteBuffer messageAsByteBuffer, RouterImpl router) {
        super(messageAsByteBuffer, router);
    }

    @Override
    public void process() throws MalformedMessageException {
        ReloadConfigurationMessage rcm = new ReloadConfigurationMessage(this.rawMessage.array(), 0);

        MagicCookie admCookie = this.router.getAdminMagicCookie();
        if (admCookie == null) {
            admin_logger.info("router configuration NOT reloaded. configuration magic cookie is not set.");
            return;
        }

        if (!admCookie.equals(rcm.getMagicCookie())) {
            admin_logger.info("router configuration NOT reloaded. Invalid configuration magic cookie");
            return;
        }

        try {
            admin_logger.info("reloading router configuration file");
            this.router.reloadConfigurationFile();
        } catch (Exception e) {
            admin_logger.warn("failed to reload the router configuration", e);
        }
    }
}
