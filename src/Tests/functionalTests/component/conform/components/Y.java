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
package functionalTests.component.conform.components;

public class Y implements I {
    // invalid component class for components with dependencies:
    //   does not implement BindingController
    public void m(boolean v) {
    }

    public void m(byte v) {
    }

    public void m(char v) {
    }

    public void m(short v) {
    }

    public void m(int v) {
    }

    public void m(long v) {
    }

    public void m(float v) {
    }

    public void m(double v) {
    }

    public void m(String v) {
    }

    public void m(String[] v) {
    }

    public boolean n(boolean v, String[] w) {
        return v;
    }

    public byte n(byte v, String w) {
        return v;
    }

    public char n(char v, double w) {
        return v;
    }

    public short n(short v, float w) {
        return v;
    }

    public int n(int v, long w) {
        return v;
    }

    public long n(long v, int w) {
        return v;
    }

    public float n(float v, short w) {
        return v;
    }

    public double n(double v, char w) {
        return v;
    }

    public String n(String v, byte w) {
        return v;
    }

    public String[] n(String[] v, boolean w) {
        return v;
    }
}
