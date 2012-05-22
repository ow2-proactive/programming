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
package org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1;

import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.examples.nqueens.Board;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


public class DivideBT1 implements Divide<Board, Board> {

    private static final long serialVersionUID = 52;

    public Board[] divide(Board board, SkeletonSystem system) {
        if (board.isRootBoard()) {
            return initDivideBT1(board).toArray(new Board[0]);
        }

        return divideBT1(board).toArray(new Board[0]);
    }

    private Vector<Board> initDivideBT1(Board board) {
        Vector<Board> v = new Vector<Board>();

        //We set row 0 and 1 for backtrack1
        for (int i = board.n - 2; i >= 2; i--) {
            int bit = 1 << i;
            v.add(new BoardBT1(board.n, board.solvableSize, 2, (2 | bit) << 1, 1 | bit, bit >> 1, i, null));
        }

        return v;
    }

    protected Vector<Board> divideBT1(Board param) {
        int mask = (1 << param.n) - 1;

        Vector<Board> v = new Vector<Board>();

        int bitmap = mask & ~(param.left | param.down | param.right);
        int bit;

        if (param.row < param.bound1) {
            bitmap &= 0xFFFFFFFD; // 1111...01
        }

        //expand this row
        while (bitmap != 0) {
            bitmap ^= (param.board[param.row] = bit = -bitmap & bitmap);

            v.add(new BoardBT1(param.n, param.solvableSize, param.row + 1, (param.left | bit) << 1,
                param.down | bit, (param.right | bit) >> 1, param.bound1, param.board));
        }

        return v;
    }
}
