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
package org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2;

import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.examples.nqueens.Board;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


public class DivideBT2 implements Divide<Board, Board> {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;

    public Board[] divide(Board board, SkeletonSystem system) throws RuntimeException {
        if (board.isRootBoard()) {
            return initDivideBT2(board).toArray(new Board[0]);
        }

        return divideBT2((BoardBT2) board).toArray(new Board[0]);
    }

    private Vector<Board> initDivideBT2(Board board) {
        Vector<Board> v = new Vector<Board>();

        int sidemask = (1 << (board.n - 1)) | 1;
        int lastmask = sidemask;
        int topbit = 1 << (board.n - 1);
        int mask = (1 << board.n) - 1;
        int endbit = topbit >> 1;

        for (int i = 1, j = board.n - 2; i < j; i++, j--) {
            //bound1 = i; //bound2 = j;
            int bit = 1 << i;

            v.add(new BoardBT2(board.n, board.solvableSize, 1, bit << 1, bit, bit >> 1, i, j, sidemask,
                lastmask, topbit, mask, endbit, null));

            lastmask |= ((lastmask >> 1) | (lastmask << 1));
            endbit >>= 1;
        }

        return v;
    }

    public Vector<Board> divideBT2(BoardBT2 board) {
        Vector<Board> v = new Vector<Board>();

        int mask = (1 << board.n) - 1;
        int bitmap = mask & ~(board.left | board.down | board.right);
        int bit;

        if (board.row < board.bound1) {
            bitmap |= board.sidemask;
            bitmap ^= board.sidemask;
        } else if (board.row == board.bound2) {
            if ((board.down & board.sidemask) == 0) {
                // "return;" original algorithm is converted into
                v.add(new Board(board.n, board.solvableSize)); //dummy child task
                return v; // no more search is required in this branch
            }
            if ((board.down & board.sidemask) != board.sidemask) {
                bitmap &= board.sidemask;
            }
        }
        while (bitmap != 0) {
            bitmap ^= (board.board[board.row] = bit = -bitmap & bitmap);
            v.add(new BoardBT2(board.n, board.solvableSize, board.row + 1, (board.left | bit) << 1,
                board.down | bit, (board.right | bit) >> 1, board.bound1, board.bound2, board.sidemask,
                board.lastmask, board.topbit, board.mask, board.endbit, board.board));
        } // while-generating

        return v;
    }
}
