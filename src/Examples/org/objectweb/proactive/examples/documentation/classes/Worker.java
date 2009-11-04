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
//@snippet-start class_Worker
//@snippet-start class_Worker_With_Activity
package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

//@snippet-break class_Worker
import org.objectweb.proactive.InitActive;


//@snippet-resume class_Worker

/**
 * @author ProActive Team
 *
 * Class used for documentation example, in particular
 * for Chapter 9. Active Objects: Creation And Advanced Concepts.
 */
public class Worker implements Serializable, InitActive {

    private IntWrapper age = new IntWrapper(0);
    private String name = "Anonymous";

    /**
     * Empty no-arg constructor needed by ProActiv
     */
    public Worker() {
    }

    /**
     * Constructor with arguments
     *
     * @param age
     * @param name
     */
    public Worker(IntWrapper age, String name) {
        this.setAge(age);
        this.setName(name);
    }

    //@snippet-break class_Worker
    //@snippet-break class_Worker_With_Activity
    //@snippet-start AO_Creation_4
    // Passing null as
    // an argument for the constructor
    // leads to the generation of an exception
    public Worker(IntWrapper age) {
        this.setAge(age);
    }

    public Worker(String name) {
        this.setName(name);
    }

    //@snippet-end AO_Creation_4

    //@snippet-start Good_Practice_1
    public Worker getWorker() {
        if (this.name.compareTo("Anonymous") != 0) {
            return this;
        } else {
            return null; //to avoid in ProActive
        }
    }

    //@snippet-end Good_Practice_1

    /**
     * Setter
     *
     * @param age
     */

    public void setAge(IntWrapper age) {
        this.age = age;
    }

    /**
     * Getter
     *
     * @return age
     */
    public IntWrapper getAge() {
        return age;
    }

    /**
     * Setter
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Do nothing
     */
    public void doNothingGracefully() {
    }

    //@snippet-start Continuation_2
    public Value foo() {
        return new Value(this.age, this.name);
    }

    //@snippet-end Continuation_2

    public void display() {
        System.out.println(this.name + " is " + this.age);
    }

    //@snippet-resume class_Worker_With_Activity
    /* (non-Javadoc)
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        System.out.println("===> Started Active object: ");
        System.out.println(body.getMBean().getName() + " on " + body.getMBean().getNodeUrl());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.EndActive#endActivity(org.objectweb.proactive.Body)
     */
    public void endActivity(Body body) {
        System.out.println("===> You have killed the active object");
    }
    //@snippet-resume class_Worker
}
//@snippet-end class_Worker
//@snippet-end class_Worker_With_Activity