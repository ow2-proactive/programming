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
package trywithcatch;

import java.io.IOException;


public class Terminal extends Anything {
    private int left;
    private int right;
    private String str;
    private int column;

    public Terminal(int l, int r, int c, String s) {
        left = l;
        right = r;
        column = c;
        str = s;
    }

    @Override
    public String toString() {
        return str + "@" + left + "";
    }

    @Override
    protected void prettyPrint(int indent) {
        super.prettyPrint(indent);
        System.out.println(this);
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public void work(Catcher c) throws IOException {
        c.removeCallAtOffset(left - column);
    }
}
