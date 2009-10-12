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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.webservices.common;

import java.util.LinkedList;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * A simple example to expose an active object as a web service.
 *
 * @author The ProActive Team
 */
@ActiveObject
public class HelloWorld extends HelloWorldSuperClass implements java.io.Serializable {

    private LinkedList<String> textsToSay = new LinkedList<String>();
    private Couple[] couples;

    public HelloWorld() {
    }

    public void putHelloWorld() {
        this.textsToSay.add("Hello world!");
    }

    public void putTextToSay(String textToSay) {
        this.textsToSay.add(textToSay);
    }

    public String sayText() {
        String str;
        if (this.textsToSay.isEmpty()) {
            str = "The list is empty";
        } else {
            str = this.textsToSay.poll();
        }
        return str;
    }

    public boolean contains(String textToCheck) {
        boolean response = new Boolean(this.textsToSay.contains(textToCheck));
        return response;
    }

    public LinkedList<String> getTextsToSay() {
        return this.textsToSay;
    }

    public void setTextsToSay(LinkedList<String> textsToSay) {
        this.textsToSay = textsToSay;
    }

    public void setCouples(Couple[] couples) {
        this.couples = couples;
    }

    public Couple[] getCouples() {
        return couples;
    }
}
