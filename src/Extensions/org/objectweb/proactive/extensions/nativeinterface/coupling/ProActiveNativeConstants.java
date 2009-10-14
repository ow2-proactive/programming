/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.nativeinterface.coupling;

public interface ProActiveNativeConstants {
    public static final int COMM_MSG_SEND = 2;
    public static final int COMM_MSG_INIT = 6;
    public static final int COMM_MSG_ALLSEND = 8;
    public static final int COMM_MSG_FINALIZE = 10;
    public static final int COMM_MSG_SEND_PROACTIVE = 12;
    public static final int COMM_MSG_SCATTER = 14;
    public static final int COMM_MSG_BCAST = 16;
    public static final int COMM_MSG_NF = 20;

}
