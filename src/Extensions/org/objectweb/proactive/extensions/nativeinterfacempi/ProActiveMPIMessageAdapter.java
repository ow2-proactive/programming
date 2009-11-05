/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.nativeinterfacempi;

import org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageAdapter;
import org.objectweb.proactive.extensions.nativeinterface.coupling.ProActiveNativeConstants;
import org.objectweb.proactive.extensions.nativeinterface.utils.ProActiveNativeUtil;


/**
 *  Implementation of @link org.objectweb.proactive.extensions.nativeinterface.application.NativeMessageAdapter
 *
 */
public class ProActiveMPIMessageAdapter implements NativeMessageAdapter {

    // Data types
    public static final int MPI_DATATYPE_NULL = 0;
    public static final int MPI_CHAR = 1;
    public static final int MPI_UNSIGNED_CHAR = 2;
    public static final int MPI_BYTE = 3;
    public static final int MPI_SHORT = 4;
    public static final int MPI_UNSIGNED_SHORT = 5;
    public static final int MPI_INT = 6;
    public static final int MPI_UNSIGNED = 7;
    public static final int MPI_LONG = 8;
    public static final int MPI_UNSIGNED_LONG = 9;
    public static final int MPI_FLOAT = 10;
    public static final int MPI_DOUBLE = 11;
    public static final int MPI_LONG_DOUBLE = 12;
    public static final int MPI_LONG_LONG_INT = 13;
    public static final int MPI_COMPLEX = 14;
    public static final int MPI_DOUBLE_COMPLEX = 15;

    public static final int MPI_ANY_TAG = -1;
    public static final int MPI_ANY_SOURCE = -2;

    private static int MSG_TYPE_OFFSET = 0;
    private static int SRC_IDJOB_OFFSET = MSG_TYPE_OFFSET + (Integer.SIZE / 8);
    private static int DEST_IDJOB_OFFSET = SRC_IDJOB_OFFSET + (Integer.SIZE / 8);
    private static int COUNT_OFFSET = DEST_IDJOB_OFFSET + (Integer.SIZE / 8);
    private static int SRC_RANK_OFFSET = COUNT_OFFSET + (Integer.SIZE / 8);
    private static int DEST_RANK_OFFSET = SRC_RANK_OFFSET + (Integer.SIZE / 8);
    private static int MSG_TAG_OFFSET = DEST_RANK_OFFSET + (Integer.SIZE / 8);
    private static int PA_DATATYPE_OFFSET = MSG_TAG_OFFSET + (Integer.SIZE / 8);
    private static int DATA_PTR_OFFSET = PA_DATATYPE_OFFSET + (Integer.SIZE / 8);
    //TODO 8 sizeof pointer on this platform
    private static int METHOD_OFFSET = DATA_PTR_OFFSET + 8;
    private static int DATA_OFFSET = METHOD_OFFSET + 128;
    private static int MSG_HEADER_SIZE = DATA_OFFSET;

    private static int extractMsgType(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, MSG_TYPE_OFFSET);
    }

    private static int extractSrcIdJob(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, SRC_IDJOB_OFFSET);
    }

    private static int extractDestIdJob(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, DEST_IDJOB_OFFSET);
    }

    private static int extractCount(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, COUNT_OFFSET);
    }

    private static int extractSrcRank(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, SRC_RANK_OFFSET);
    }

    private static int extractDestRank(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, DEST_RANK_OFFSET);
    }

    private static int extractMsgTag(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, MSG_TAG_OFFSET);
    }

    private static int extractPaDatatype(byte[] serializedMsg) {
        return ProActiveNativeUtil.bytesToInt(serializedMsg, PA_DATATYPE_OFFSET);
    }

    public ProActiveMPIMessage deserialize(byte[] serializedMsg) {
        return deserializeInternal(serializedMsg);
    }

    public byte[] buildInitMessage(int nativeRank, int nativeJobID, int nbJob) {
        return ProActiveMPIMessageAdapter.buildInitMessageInternal(nativeRank, nativeJobID, nbJob);
    }

    private static ProActiveMPIMessage deserializeInternal(byte[] serializedMsg) {
        ProActiveMPIMessage msg = new ProActiveMPIMessage();
        msg.setMsgType(extractMsgType(serializedMsg));
        msg.setCount(extractCount(serializedMsg));
        msg.setSrcJobID(extractSrcIdJob(serializedMsg));
        msg.setDestJobID(extractDestIdJob(serializedMsg));
        msg.setSrcRank(extractSrcRank(serializedMsg));
        msg.setDestRank(extractDestRank(serializedMsg));
        msg.setTag(extractMsgTag(serializedMsg));
        msg.setDatatype(extractPaDatatype(serializedMsg));
        msg.setSerializedMsg(serializedMsg);
        return msg;
    }

    private static byte[] buildInitMessageInternal(int myrank, int myJobID, int nbJob) {
        /*((Integer.SIZE / 8) * 8) + 8 + 128*/
        byte[] msg = new byte[MSG_HEADER_SIZE];

        ProActiveNativeUtil.intToBytes(ProActiveNativeConstants.COMM_MSG_INIT, msg, MSG_TYPE_OFFSET);
        ProActiveNativeUtil.intToBytes(myJobID, msg, SRC_IDJOB_OFFSET);
        ProActiveNativeUtil.intToBytes(myJobID, msg, DEST_IDJOB_OFFSET);
        ProActiveNativeUtil.intToBytes(0, msg, COUNT_OFFSET);
        ProActiveNativeUtil.intToBytes(nbJob, msg, SRC_RANK_OFFSET);
        ProActiveNativeUtil.intToBytes(myrank, msg, DEST_RANK_OFFSET);
        ProActiveNativeUtil.intToBytes(0, msg, MSG_TAG_OFFSET);
        ProActiveNativeUtil.intToBytes(MPI_DATATYPE_NULL, msg, PA_DATATYPE_OFFSET);

        return msg;
    }

    /*************************** SCATTER MESSAGE UTILS ***********************************/
    /**
     * pa_rank_array
     * count_array
     * concatenated data buffer
     */
    public static int extract_scatter_nb_send(byte[] serializedBytes) {
        return extractCount(serializedBytes);
    }

    public static int extract_scatter_msg_size(byte[] serializedBytes, int i) {
        int offset = DATA_OFFSET + (i * (Integer.SIZE / 8));
        return ProActiveNativeUtil.bytesToInt(serializedBytes, offset);
    }

    public static ProActiveMPIMessage extract_msg_from_scatter(byte[] serializedBytes, int i) {
        int nb_send = extract_scatter_nb_send(serializedBytes);
        int offset = DATA_OFFSET + (nb_send * (Integer.SIZE / 8));
        //calculating offset
        int j = 0;
        while (j < i) {
            int msg_size = extract_scatter_msg_size(serializedBytes, j);
            offset += msg_size;
            j++;
        }
        int msg_size = extract_scatter_msg_size(serializedBytes, i);
        byte[] extractedMsg = new byte[msg_size];

        // extract raw send message from scatter message src / dest
        System.arraycopy(serializedBytes, offset, extractedMsg, 0, msg_size);

        return ProActiveMPIMessageAdapter.deserializeInternal(extractedMsg);
    }

    /*************************** BCAST MESSAGE UTILS ***********************************/

    public static int extract_bcast_nb_send(byte[] serializedBytes) {
        return extractCount(serializedBytes);
    }

    public static int extract_bcast_msg_size(byte[] serializedBytes) {
        int offset = DATA_OFFSET;
        return ProActiveNativeUtil.bytesToInt(serializedBytes, offset);
    }

    public static ProActiveMPIMessage extract_msg_from_bcast(byte[] serializedBytes, int i) {
        int msg_size = extract_bcast_msg_size(serializedBytes);
        int offset = DATA_OFFSET + (Integer.SIZE / 8) + (i * msg_size);
        //calculating offset

        byte[] extractedMsg = new byte[msg_size];

        // extract raw send message from scatter message src / dest
        System.arraycopy(serializedBytes, offset, extractedMsg, 0, msg_size);

        return ProActiveMPIMessageAdapter.deserializeInternal(extractedMsg);
    }
}
