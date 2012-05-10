/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
//@snippet-start simple_hello_example
package org.objectweb.proactive.examples.userguide.simplehello;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class HelloWorld {
    // empty constructor is required by Proactive
    public HelloWorld() {
    }

    //@snippet-start wrapper_example_simple_hello 
    // the method returns StringWrapper so the calls can be ansychronous
    public StringWrapper sayHello() {
        String hostname = "Unkown";
        try {
            hostname = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException excep) {
            // hostname will be "Unknown"
            System.err.println(excep.getMessage());
        }
        return new StringWrapper("Distributed Hello! from " + hostname);
    }
    //@snippet-end wrapper_example_simple_hello 
}
//@snippet-end simple_hello_example
