/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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


public class FirstDiagQuery extends Query {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    int scale;

    public FirstDiagQuery(int n, int s) {
        super(n, 1, 2, 1, 0);
        down |= ~((1 << n) - 1);
        scale = s;
    }

    @Override
    public long run() {
        Vector v = split(new Vector());
        int n = v.size();
        long r = 0;
        for (int i = 0; i < n; i++) {
            r += ((Query) v.get(i)).run();
        }
        return (r);
    }

    private DiagQuery next(int q, int sym) {
        int l = (left | q) << 1;
        int d = (down | q);
        int r = (right | q) >> 1;
        return (new DiagQuery(n, 2, sym, scale, l, d, r));
    }

    @Override
    public Vector split(Vector v) {
        int nq1 = n - 1;
        for (int i = 2; i < nq1; i++)
            v.add(next(1 << i, nq1 - i));
        return (v);
    }
}
