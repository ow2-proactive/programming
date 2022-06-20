/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.ssh.proxycommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;

import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Session;


public class SshProxySession extends Socket {

    private Session session;

    private boolean unused;

    private InputStream is;

    private OutputStream os;

    SshProxySession(Session sess) {
        this.session = sess;
        this.is = sess.getStdout();
        this.os = sess.getStdin();
    }

    public Socket getSocket() throws IOException {
        this.unused = false;
        return this;
    }

    public void close() throws IOException {
        this.flush();
        try {
            this.session.waitForCondition(ChannelCondition.EOF, 1);
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        } finally {
            /* Now its hopefully safe to close the session */
            this.session.close();
            // Socket::close()
            this.unused = true;
        }
    }

    public boolean isUnused() {
        return this.unused;
    }

    // Socket methods
    @Override
    public InputStream getInputStream() throws IOException {
        return this.is;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.os;
    }

    public void flush() throws IOException {
        this.os.flush();
    }

    @Override
    public String toString() {
        return "Socket[ProxyCommand fake socket connected]";
    }
}
