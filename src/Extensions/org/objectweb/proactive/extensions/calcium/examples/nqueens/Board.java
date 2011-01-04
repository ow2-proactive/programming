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
package org.objectweb.proactive.extensions.calcium.examples.nqueens;

public class Board implements java.io.Serializable {
    //board dimension
    public int n;
    public int solvableSize;

    //solutions vector
    //public long solutions[];

    // the board
    public int[] board;
    public int bound1;
    public int topbit;
    public int mask;
    public int row; // fila de la ultima reina fijada
    public int column; // columna de la ultima reina fijada
    public int left; // vector de diagonales hacia la izquierda
    public int down; // vector de columnas
    public int right; // vector de diagonales hacia la derecha

    public Board(int n, int solvableSize) {
        this.n = n;
        this.solvableSize = solvableSize;

        this.board = new int[n];
    }

    public Board(int n, int solvableSize, int row, int left, int down, int right, int bound1) {
        this(n, solvableSize);
        this.row = row;
        this.left = left;
        this.right = right;
        this.down = down;
        this.bound1 = bound1;
    }

    public boolean isRootBoard() {
        return true;
    }

    public boolean isBT1() {
        return false;
    }

    /*
    public Vector<Board> divide(){

            Vector<Board> v = new Vector<Board>();
            v.addAll(initDivideBT1());
            v.addAll(initDivideBT2());
            return v;
    }
     */
}
