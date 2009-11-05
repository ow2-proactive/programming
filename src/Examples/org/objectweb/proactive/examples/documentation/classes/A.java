/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.StringWrapper;


/**
 * @author ffonteno
 *
 */
//@snippet-start class_A
public class A implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    protected String str = "Hello";

    /**
     * Empty no-arg constructor
     */
    public A() {
    }

    /**
     * Constructor which initializes str
     *
     * @param str
     */
    public A(String str) {
        this.str = str;
    }

    /**
     * setStrWrapper
     *
     * @param strWrapper StringWrapper to set
     *      StringWrapper is used since this method gives an example
     *      of parameter scatterring. Parameter need to be serializable.
     * @return this
     */
    public A setStrWrapper(StringWrapper strWrapper) {
        this.str = strWrapper.stringValue();
        return this;
    }

    /**
     * getB
     *
     * @return a B object corresponding to the current Object
     */
    public B getB() {
        if (this instanceof B) {
            return (B) this;
        } else {
            return new B(str);
        }
    }

    /**
     * display str on the standard output
     */
    public void display() {
        System.out.println("A display =====> " + str);
    }

    //@snippet-break class_A
    //@snippet-start class_A_exception
    /**
     * Example of a method which can throw an exception
     *
     * @param hasToThrow boolean saying whether the method should
     *                   throw an exception
     * @throws Exception
     */
    public void throwsException(boolean hasToThrow) throws Exception {
        Thread.sleep(5000);
        if (hasToThrow)
            throw new Exception("Class A has thrown an exception");
    }
    //@snippet-resume class_A
    //@snippet-end class_A_exception
}
//@snippet-end class_A