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

public class Out {
    int SIZEE;
    int[] BOARD;
    int TOPBIT;
    int SIDEMASK;
    int LASTMASK;
    int ENDBIT;
    int BOUND1;
    int BOUND2;
    int BBOUND1;
    int BBOUND2;
    int COUNT8;
    int COUNT4;
    int COUNT2;

    final void Check(int bsize) {
        int size = SIZEE;

        /* 90-degree rotation */
        if (BBOUND2 == 1) {
            int own = 1;
            for (int ptn = 2; own <= size; own++, ptn <<= 1) {
                int bit = 1;
                int bown = BOARD[own];
                for (int you = size; (BOARD[you] != ptn) && (bown >= bit); you--)
                    bit <<= 1;
                if (bown > bit) {
                    return;
                }
                if (bown < bit) {
                    break;
                }
            }
            if (own > size) {
                COUNT2++;
                return;
            }
        }

        /* 180-degree rotation */
        if (bsize == ENDBIT) {
            int own = 1;
            for (int you = size - 1; own <= size; own++, you--) {
                int bit = 1;
                int bown = BOARD[own];
                int byou = BOARD[you];
                for (int ptn = TOPBIT; (ptn != byou) && (bown >= bit); ptn >>= 1)
                    bit <<= 1;
                if (bown > bit) {
                    return;
                }
                if (bown < bit) {
                    break;
                }
            }
            if (own > size) {
                COUNT4++;
                return;
            }
        }

        /* 270-degree rotation */
        if (BBOUND1 == TOPBIT) {
            int own = 1;
            for (int ptn = TOPBIT >> 1; own <= size; own++, ptn >>= 1) {
                int bit = 1;
                int bown = BOARD[own];
                for (int you = 0; (BOARD[you] != ptn) && (bown >= bit); you++) {
                    bit <<= 1;
                }
                if (bown > bit) {
                    return;
                }
                if (bown < bit) {
                    break;
                }
            }
        }
        COUNT8++;
    }

    /**********************************************/
    /* First queen is inside */
    /**********************************************/
    final void Backtrack2(int y, int left, int down, int right) {
        int bitmap = ~(left | down | right);
        if (y == BOUND1) {
            // First diag reached
            while (bitmap != 0) {
                int bit = -bitmap & bitmap;
                BBOUND1 = bit;
                bitmap ^= (BOARD[y] = bit);
                Backtrack3(y + 1, (left | bit) << 1, down | bit, (right | bit) >> 1);
            }
        } else {
            bitmap |= SIDEMASK;
            bitmap ^= SIDEMASK;
            while (bitmap != 0) {
                int bit = -bitmap & bitmap;
                bitmap ^= (BOARD[y] = bit);
                Backtrack2(y + 1, (left | bit) << 1, down | bit, (right | bit) >> 1);
            }
        }
    }

    final void Backtrack3(int y, int left, int down, int right) {
        int bitmap = ~(left | down | right);
        if (y == BOUND2) {
            // Second diag reached
            if ((down & SIDEMASK) == 0) {
                return;
            }
            if ((down & SIDEMASK) != SIDEMASK) {
                bitmap &= SIDEMASK;
            }
            while (bitmap != 0) {
                int bit = -bitmap & bitmap;
                BBOUND2 = bit;
                bitmap ^= (BOARD[y] = bit);
                Backtrack4(y + 1, (left | bit) << 1, down | bit, (right | bit) >> 1);
            }
        } else {
            while (bitmap != 0) {
                int bit = -bitmap & bitmap;
                bitmap ^= (BOARD[y] = bit);
                Backtrack3(y + 1, (left | bit) << 1, down | bit, (right | bit) >> 1);
            }
        }
    }

    final void Backtrack4(int y, int left, int down, int right) {
        int bitmap = ~(left | down | right);
        if (y == SIZEE) {
            if (bitmap != 0) {
                if ((bitmap & LASTMASK) == 0) {
                    BOARD[y] = bitmap;
                    Check(bitmap);
                }
            }
        } else {
            while (bitmap != 0) {
                int bit = -bitmap & bitmap;
                bitmap ^= (BOARD[y] = bit);
                Backtrack4(y + 1, (left | bit) << 1, down | bit, (right | bit) >> 1);
            }
        }
    }

    /**********************************************/
    /* Search of N-Queens */
    /**********************************************/
    final int how() {
        return ((COUNT8 * 8) + (COUNT4 * 4) + (COUNT2 * 2));
    }

    final void init(OutQuery query) {
        int size = query.n;
        BOARD = query.board;
        COUNT8 = COUNT4 = COUNT2 = 0;
        SIZEE = size - 1;
        TOPBIT = 1 << SIZEE;

        /* 0:000001110 */
        SIDEMASK = TOPBIT | 1;
        ENDBIT = query.endbit;
        BOUND1 = query.bound1;
        BOUND2 = query.bound2;
        BBOUND1 = query.bbound1;
        BBOUND2 = query.bbound2;
        LASTMASK = query.lastmask;
    }

    static int run(OutQuery query) {
        Out o = new Out();
        o.init(query);
        int y = query.done;
        if (y <= o.BOUND1) {
            o.Backtrack2(y, query.left, query.down, query.right);
        } else if (y <= o.BOUND2) {
            o.Backtrack3(y, query.left, query.down, query.right);
        } else {
            o.Backtrack4(y, query.left, query.down, query.right);
        }

        /* Total Solutions */
        return (o.how());
    }
}
