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
package org.objectweb.proactive.core.ssh.proxycommand;

import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.core.config.PAPropertyBoolean;
import org.objectweb.proactive.core.config.PAPropertyString;


public class ProxyCommandConfig implements PAPropertiesLoaderSPI {

    public static PAPropertyString PA_SSH_PROXY_GATEWAY = new PAPropertyString("proactive.communication.ssh.proxy.gateway",
                                                                               false);

    public static PAPropertyString PA_SSH_PROXY_USE_GATEWAY_OUT = new PAPropertyString("proactive.communication.ssh.proxy.out_gateway",
                                                                                       false);

    public static PAPropertyBoolean PA_RMISSH_TRY_PROXY_COMMAND = new PAPropertyBoolean("proactive.communication.ssh.try_proxy_command",
                                                                                        false,
                                                                                        false);

}
