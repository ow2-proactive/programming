/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pnpssl;

import java.security.KeyStore;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.TrustManager;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.objectweb.proactive.extensions.pnp.PNPExtraHandlers;
import org.objectweb.proactive.extensions.ssl.SecureMode;


/**
 * Add {@link SslHandler} as extra channel to the default PNP pipeline
 *
 * @since ProActive 5.0.0
 */
public class PNPSslExtraHandlers implements PNPExtraHandlers {

    final PNPSslEngineFactory sslEngineFactory;

    public PNPSslExtraHandlers(SecureMode sm, KeyStore ks, TrustManager tm) {
        sslEngineFactory = new PNPSslEngineFactory(sm, ks, tm);
    }

    public List<ChannelHandler> getClientHandlers() {
        final LinkedList<ChannelHandler> handlers = new LinkedList<ChannelHandler>();
        handlers.add(new SslHandler(sslEngineFactory.getClientSSLEngine()));
        return handlers;
    }

    public List<ChannelHandler> getServertHandlers() {
        final LinkedList<ChannelHandler> handlers = new LinkedList<ChannelHandler>();
        handlers.add(new SslHandler(sslEngineFactory.getServerSSLEngine()));
        return handlers;
    }
}
