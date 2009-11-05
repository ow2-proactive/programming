/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.components;

import org.objectweb.proactive.examples.components.helloworld.Service;


//@snippet-start component_examples_13
//@snippet-start component_examples_2
public class AImpl implements A {

    Service service;

    public AImpl() {
    }

    //@snippet-break component_examples_13
    public String bar() {
        return "bar";
    }

    //@snippet-resume component_examples_13

    //@snippet-break component_examples_2
    // implementation of the A interface
    public void foo() {
        service.print("Hello World"); // for example
    }

    // implementation of BindingController
    public Object lookupFc(final String cItf) {
        if (cItf.equals("requiredService")) {
            return service;
        }
        return null;
    }

    // implementation of BindingController
    public void bindFc(final String cItf, final Object sItf) {
        if (cItf.equals("requiredService")) {
            service = (Service) sItf;
        }
    }

    // implementation of BindingController
    public void unbindFc(final String cItf) {
        if (cItf.equals("requiredService")) {
            service = null;
        }
    }
    //@snippet-resume component_examples_2
}
//@snippet-end component_examples_2
//@snippet-end component_examples_13
