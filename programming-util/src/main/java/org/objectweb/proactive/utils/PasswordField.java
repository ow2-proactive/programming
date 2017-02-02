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
package org.objectweb.proactive.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This class prompts the user for a password and attempts to mask input with "*".
 * 
 * @author The ProActive Team
 * @version 4.0, Jun 27, 2007
 * @since ProActive 4.0
 */
public class PasswordField {
    /**
     * Get the password as entered by the user on the given input stream.
     * 
     * @param input The input stream
     * @param prompt The prompt to display to the user.
     * @return The password as entered by the user.
     */
    public static final char[] getPassword(InputStream in, String promt) throws IOException {
        PushbackInputStream pbIn = null;
        if (pbIn instanceof PushbackInputStream) {
            pbIn = (PushbackInputStream) in;
        } else {
            pbIn = new PushbackInputStream(in);
        }

        return getPassword(pbIn, promt);
    }

    /**
     * Get the password as entered by the user on the given input stream.
     * 
     * @param input stream to be used
     * @param prompt The prompt to display to the user.
     * @return The password as entered by the user.
     */
    public static final char[] getPassword(PushbackInputStream in, String prompt) throws IOException {
        MaskingThread maskingthread = new MaskingThread(prompt);
        maskingthread.start();

        char[] buf = new char[64];

        int room = buf.length;
        int offset = 0;
        int c;

        loop: while (true) {
            c = in.read();
            switch (c) {
                case -1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = in.read();
                    if ((c2 != '\n') && (c2 != -1)) {
                        in.unread(c2);
                    } else {
                        break loop;
                    }

                default:
                    if (--room < 0) {
                        char[] tmp = new char[offset + 64];
                        room = tmp.length - offset - 1;
                        System.arraycopy(buf, 0, tmp, 0, offset);
                        Arrays.fill(buf, ' ');
                        buf = tmp;
                    }
                    buf[offset++] = (char) c;
                    break;
            }
        }
        maskingthread.terminate();
        if (offset == 0) {
            return null;
        }
        char[] ret = new char[offset];
        System.arraycopy(buf, 0, ret, 0, offset);
        Arrays.fill(buf, ' ');
        return ret;
    }

    private static class MaskingThread extends Thread {
        private AtomicBoolean cont = new AtomicBoolean(true);

        /**
         * Create new masking thread.
         * 
         *@param prompt The prompt displayed to the user
         */
        public MaskingThread(String prompt) {
            System.out.print(prompt + " ");
        }

        /**
         * Begin masking until asked to stop.
         */
        public void run() {

            int oldPrio = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            try {
                while (this.cont.get()) {
                    System.out.print("\010 ");
                    Thread.yield();
                }
            } finally { // restore the original priority
                Thread.currentThread().setPriority(oldPrio);
            }
        }

        public void terminate() {
            this.cont.set(false);
        }
    }
}
