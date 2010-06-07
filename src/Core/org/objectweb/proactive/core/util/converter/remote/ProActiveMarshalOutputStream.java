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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.util.converter.remote;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * An output stream which defines the serialization behaviour.
 * 
 * The URL of the local runtime is put as annotation to allows the 
 * {@link ProActiveMarshalInputStream} to remotely download the
 * class if neeeded.
 *
 * @since ProActive 4.3.0
 */
public class ProActiveMarshalOutputStream extends ObjectOutputStream {

    private final String localRuntimeUrl;

    public ProActiveMarshalOutputStream(OutputStream out, String localRuntimeUrl) throws IOException {
        super(out);
        this.localRuntimeUrl = localRuntimeUrl;
    }

    @Override
    protected void annotateClass(Class<?> cl) throws IOException {
        // write the local runtime URL
        writeObject(this.localRuntimeUrl);
    }
}
