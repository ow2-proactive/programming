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
package functionalTests.component.conform.components;

public interface I {
    void m(boolean v);

    void m(byte v);

    void m(char v);

    void m(short v);

    void m(int v);

    void m(long v);

    void m(float v);

    void m(double v);

    void m(String v);

    void m(String[] v);

    boolean n(boolean v, String[] w);

    byte n(byte v, String w);

    char n(char v, double w);

    short n(short v, float w);

    int n(int v, long w);

    long n(long v, int w);

    float n(float v, short w);

    double n(double v, char w);

    String n(String v, byte w);

    String[] n(String[] v, boolean w);
}
