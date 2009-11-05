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
package functionalTests.activeobject.request.forgetonsend;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.annotation.Sterile;


public class B {

    private static String services;

    private String name;

    public B() {
    }

    public B(String name) {
        services = "";
        this.name = name;
    }

    public void a() {
        services += "a";
    }

    public SlowlySerializableObject b(int i) {
        services += "b";
        return new SlowlySerializableObject("res" + i, 0);
    }

    public void c() {
        services += "c";
    }

    public void d() {
        services += "d";
    }

    public void e() {
        services += "e";
    }

    public void f() {
        services += "f";
    }

    @Sterile
    public void g() {
        services += "g";
    }

    public void h(SlowlySerializableObject o) {
        services += "h";
    }

    public void i(SlowlySerializableObject o) {
        services += "i";
    }

    public String takeFast() {
        String result = services;
        services = "";
        return result;
    }

    public void setAsImmediate(String methodName) {
        PAActiveObject.setImmediateService(methodName);
    }
}
