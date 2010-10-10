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
package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 * @author The ProActive Team
 *
 * Used to explain asynchronous behaviour.
 */
public class Value implements Serializable {
    IntWrapper age;
    String name;

    public Value() {
    }

    public Value(IntWrapper age, String name) {
        this.age = age;
        this.name = name;
    }

    public void bar() {
        this.age = new IntWrapper(0);
        this.name = "Anonymous";
    }

    //@snippet-start Continuation_3
    public Value bar(Value v, int nbYears) {
        v.setAge(new IntWrapper(v.getAge().getIntValue() + nbYears));
        return this;
    }

    //@snippet-end Continuation_3

    public void display() {
        System.out.println(this.name + " is " + this.age);
    }

    /**
     *
     *  GETTERS AND SETTERS
     *
     */

    public IntWrapper getAge() {
        return age;
    }

    public void setAge(IntWrapper age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
