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
package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

public class Diag {
    private int SYM;
    private int N = 0;

    private final void countup(int y, int l, int d, int r) {
        if (y > SYM) {
            int todo = ~(l | d | r | 2);
            while (todo != 0) {
                int q = -todo & todo;
                todo ^= q;
                countup(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
            }
        } else {
            int todo = ~(l | d | r);
            while (todo != 0) {
                int q = -todo & todo;
                todo ^= q;
                count(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
            }
        }
    }

    private final void count(int y, int l, int d, int r) {
        int todo = ~(l | d | r);
        if (todo != 0) {
            if (y == 0) {
                N++;
            } else {
                do {
                    int q = -todo & todo;
                    todo ^= q;
                    count(y - 1, (l | q) << 1, d | q, (r | q) >> 1);
                } while (todo != 0);
            }
        }
    }

    public static int run(DiagQuery query) {
        int n = query.n;
        Diag o = new Diag();
        o.SYM = query.sym;
        int y = n - query.done - 1;
        if (y >= query.sym) {
            o.countup(y, query.left, query.down, query.right);
        } else {
            o.count(y, query.left, query.down, query.right);
        }
        return (o.N);
    }
}
