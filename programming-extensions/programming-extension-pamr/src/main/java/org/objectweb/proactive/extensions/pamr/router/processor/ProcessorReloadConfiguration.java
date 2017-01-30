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
