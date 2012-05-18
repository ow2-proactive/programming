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
package org.objectweb.proactive.examples.masterworker.nqueens.query;

import java.util.Vector;


public class DiagQuery extends Query {

    private static final long serialVersionUID = 52;
    public int sym;
    public int scale;

    public DiagQuery(int n, int done, int sym, int s, int l, int d, int r) {
        super(n, done, l, d, r);
        this.sym = sym;
        this.scale = s;
    }

    @Override
    public long run() {
        return Diag.run(this) * scale;
    }

    private DiagQuery next(int q) {
        int l = (left | q) << 1;
        int d = (down | q);
        int r = (right | q) >> 1;
        return (new DiagQuery(n, done + 1, sym, scale, l, d, r));
    }

    @Override
    public Vector split(Vector v) {
        int y = n - done - 1;
        int todo = ~(left | down | right | ((y >= sym) ? 2 : 0));
        while (todo != 0) {
            int q = -todo & todo;
            todo ^= q;
            v.add(next(q));
        }
        return (v);
    }
}
