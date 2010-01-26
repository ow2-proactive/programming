/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.readers;

public class ReaderDisplay implements org.objectweb.proactive.InitActive {
    private AppletReader applet;
    private ReaderWriter rw;
    private Reader[] readers;
    private Writer[] writers;

    public ReaderDisplay() {
    }

    public ReaderDisplay(AppletReader applet) {
        this.applet = applet;
    }

    public void initActivity(org.objectweb.proactive.Body body) {
        readers = new Reader[3];
        writers = new Writer[3];

        Object[] param = new Object[] { org.objectweb.proactive.api.PAActiveObject.getStubOnThis(),
                new Integer(ReaderWriter.DEFAULT_POLICY) };
        try {
            rw = org.objectweb.proactive.api.PAActiveObject.newActive(ReaderWriter.class, param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        param = new Object[3];
        param[0] = org.objectweb.proactive.api.PAActiveObject.getStubOnThis();
        param[1] = rw;
        for (int i = 0; i < 3; i++) {
            param[2] = new Integer(i);
            try {
                // Readers
                readers[i] = org.objectweb.proactive.api.PAActiveObject.newActive(Reader.class, param);
                // Writers
                writers[i] = org.objectweb.proactive.api.PAActiveObject.newActive(Writer.class, param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setRead(int id, boolean state) {
        applet.readerPanel.setRead(id, state);
    }

    public void setWait(int id, boolean isReader) {
        applet.readerPanel.setWait(id, isReader);
    }

    public void setWrite(int id, boolean state) {
        applet.readerPanel.setWrite(id, state);
    }

    public void setPolicy(int policy) {
        if ((policy < 3) && (policy > -1)) {
            rw.setPolicy(policy);
        }
    }
}
