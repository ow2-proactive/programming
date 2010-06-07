/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
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
