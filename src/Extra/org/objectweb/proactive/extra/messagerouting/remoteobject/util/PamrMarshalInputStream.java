/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.messagerouting.remoteobject.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;


/**
 * An input stream which defines the deserialization behaviour
 *   while using the ProActive Message Routing protocol
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class PamrMarshalInputStream extends ObjectInputStream {

    private final PamrClassLoader pamrLoader;

    public PamrMarshalInputStream(InputStream in) throws IOException {
        super(in);
        pamrLoader = new PamrClassLoader();
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            // first, try to resolve the class locally
            return super.resolveClass(desc);
        } catch (ClassNotFoundException e) {
            // try to load the class using the pamr class loader
            String clazzName = desc.getName();
            try {
                String runtimeURL = readRuntimeURL();
                return this.pamrLoader.loadClass(clazzName, runtimeURL);
            } catch (ClassCastException e1) {
                throw new ClassNotFoundException("Cannot load the class " + clazzName +
                    " - violation of the pamr serialization protocol.");
            }
        }
    }

    private String readRuntimeURL() throws IOException, ClassNotFoundException {
        // the protocol guarantees that the URL is the next object in the stream
        return (String) readObject();
    }

}
