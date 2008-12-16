/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.debug.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;


public class Connection implements Serializable {

    private static final long serialVersionUID = 4143722330891057902L;
    private Socket socket;
    private InputStream reader;
    private OutputStream writer;
    private boolean activated = false;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        reader = socket.getInputStream();
        writer = socket.getOutputStream();
    }

    public Data read() throws IOException {
        Data data = new Data(512);
        data.read(reader);
        return data;
    }

    public boolean isActive() {
        return activated;
    }

    public void write(Data data) throws IOException {
        data.write(writer);
    }

    public void activate() {
        activated = true;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
        }

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }
}