/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.examples.documentation.faq;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class MyClass {

    //@snippet-start faq_3
    //@snippet-start faq_4
    //@snippet-start faq_5
    //@snippet-start faq_6
    public class A {
        //@snippet-break faq_3
        //@snippet-break faq_4
        //@snippet-break faq_5
        //@snippet-break faq_6

        //@snippet-resume faq_4
        public A getBadRef() {
            return this; // THIS IS WRONG FOR AN ACTIVE OBJECT
        }

        //@snippet-break faq_4

        //@snippet-resume faq_5
        public A getGoodRef() {
            return (A) PAActiveObject.getStubOnThis(); // returns a reference on the stub
        }

        //@snippet-break faq_5

        //@snippet-resume faq_6
        private int i;
        private String s;

        public A() {
        }

        public A(int i, String s) {
            this.i = i;
            this.s = s;
        }

        //@snippet-break faq_6
        public int a1;

        //@snippet-resume faq_6

        //@snippet-resume faq_3
        public void main(String[] args) throws Exception {
            //@snippet-break faq_6
            A a = new A();
            A activeA = (A) PAActiveObject.turnActive(a);
            a.a1 = 2; // set the attribute a1 of the instance pointed by a to 2
            activeA.a1 = 2; // set the attribute a1 of the stub instance to 2
            //@snippet-resume faq_6
            //@snippet-break faq_3

            // instance based creation
            A a1;
            Object[] params = new Object[] { new Integer(26), "astring" };
            try {
                a = (A) PAActiveObject.newActive(A.class.getName(), params);
            } catch (ActiveObjectCreationException e) {
                // creation of ActiveObject failed
                e.printStackTrace();
            }
            // object based creation
            A a2 = new A(26, "astring");
            try {
                a = (A) PAActiveObject.turnActive(a);
            } catch (ActiveObjectCreationException e) {
                // creation of ActiveObject failed
                e.printStackTrace();
            }
            //@snippet-resume faq_3
        }
        //@snippet-break faq_6
        //@snippet-break faq_3

        //@snippet-resume faq_3
        //@snippet-resume faq_4
        //@snippet-resume faq_5
        //@snippet-resume faq_6
    }

    //@snippet-end faq_3
    //@snippet-end faq_4
    //@snippet-end faq_5
    //@snippet-end faq_6

    //@snippet-start faq_7
    public class B implements InitActive, RunActive {

        private String myName;

        public String getName() {
            return myName;
        }

        // -- implements InitActive
        public void initActivity(Body body) {
            myName = body.getName();
        }

        // -- implements RunActive for serving request in a LIFO fashion
        public void runActivity(Body body) {
            Service service = new Service(body);
            while (body.isActive()) {
                service.blockingServeYoungest();
            }
        }

        public void main(String[] args) throws Exception {
            B b = (B) PAActiveObject.newActive(B.class.getName(), null);
            System.out.println("Name = " + b.getName());
        }
    }

    //@snippet-end faq_7

    //@snippet-start faq_8
    public class LIFOActivity implements RunActive {

        // -- implements RunActive for serving request in a LIFO fashion
        public void runActivity(Body body) {
            Service service = new Service(body);
            while (body.isActive()) {
                service.blockingServeYoungest();
            }
        }
    }

    public class C implements InitActive {
        private String myName;

        public String getName() {
            return myName;
        }

        // -- implements InitActive
        public void initActivity(Body body) {
            myName = body.getName();
        }

        public void main(String[] args) throws Exception {
            C c = (C) PAActiveObject.newActive(C.class, null, null, null, new LIFOActivity(), null);
            System.out.println("Name = " + c.getName());
        }
    }

    //@snippet-end faq_8

    public static void main(String[] args) throws ActiveObjectCreationException, NodeException {
        //@snippet-start faq_1
        Node node = NodeFactory.getNode("//X/node1");
        // Creates an active object on the remote node
        MyClass mc1 = (MyClass) PAActiveObject.newActive(MyClass.class, new Object[] {}, node);

        // Turns an existing object into an active object on the remote node
        //@snippet-start faq_2
        MyClass mc2 = new MyClass();
        PAActiveObject.turnActive(mc2, node);
        //@snippet-end faq_2
        //@snippet-end faq_1
    }

    //@snippet-start faq_9
    public MyClass getMyClass(boolean returnNull) {
        if (!returnNull) {
            return new MyClass();
        } else {
            return null; //--> to avoid in ProActive
        }
    }

    //@snippet-end faq_9

    public void callerSide() throws ActiveObjectCreationException, NodeException {
        //@snippet-start faq_10
        MyClass o = (MyClass) PAActiveObject.newActive(MyClass.class, null);
        MyClass result_from_method = o.getMyClass(true);
        if (result_from_method == null) {
            // Do something
        }
        //@snippet-end faq_10
    }

}
