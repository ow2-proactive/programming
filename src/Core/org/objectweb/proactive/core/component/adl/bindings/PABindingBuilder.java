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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.adl.bindings;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.bindings.FractalBindingBuilder;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;


/**
 * An extension of the {@link FractalBindingBuilder} class for web service bindings.
 *
 * @author The ProActive Team
 */
public class PABindingBuilder extends FractalBindingBuilder {
    public static final int WEBSERVICE_BINDING = 3;

    public void bindComponent(int type, Object client, String clientItf, Object server, String serverItf,
            Object context) throws Exception {
        if (type != WEBSERVICE_BINDING) {
            super.bindComponent(type, client, clientItf, server, serverItf, context);
        } else {
            BindingController bc = GCM.getBindingController((Component) client);
            bc.bindFc(clientItf, serverItf);
        }
    }
}
