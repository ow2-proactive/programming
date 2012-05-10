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
package org.objectweb.proactive.extensions.nativeinterface.coupling;

/**
 *  Basic implamemtation which contains native methods capable of
 *  communicating through IPC or socket
 *
 */
public class NativeInterfaceImpl implements NativeInterface {

    public int init() {
        return init_native();
    }

    public int terminate() {
        return terminate_native();
    }

    public void sendMessage(byte[] data) {
        send_message_native(data);
    }

    public byte[] recvMessage() {
        return recv_message_native();
    }

    public void debug(byte[] data) {
        debug_native(data);
    }

    public native int init_native();

    public native int terminate_native();

    public native void send_message_native(byte[] data);

    public native byte[] recv_message_native();

    public native void debug_native(byte[] data);

}
