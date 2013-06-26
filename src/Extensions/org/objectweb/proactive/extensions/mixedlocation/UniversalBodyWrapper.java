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
package org.objectweb.proactive.extensions.mixedlocation;

import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;


public class UniversalBodyWrapper implements UniversalBody, Runnable {

    /**
     *
     */
    protected UniversalBody wrappedBody;
    protected long time;
    protected UniqueID id;
    protected String name;
    protected boolean stop;
    protected long creationTime;

    //protected  Thread t ;

    /**
     * Create a time-limited wrapper around a UniversalBody
     * @param body the wrapped UniversalBody
     * @param time the life expectancy of this wrapper in milliseconds
     */
    public UniversalBodyWrapper(UniversalBody body, long time) {
        this.wrappedBody = body;
        this.time = time;
        this.creationTime = System.currentTimeMillis();
        this.id = this.wrappedBody.getID();
        this.name = this.wrappedBody.getName();
    }

    public void receiveRequest(Request request) throws IOException {
        //       System.out.println("UniversalBodyWrapper.receiveRequest");
        if (this.wrappedBody == null) {
            throw new IOException();
        }

        //the forwarder should be dead
        if (System.currentTimeMillis() > (this.creationTime + this.time)) {
            //   this.updateServer();
            //   this.wrappedBody = null;
            //	t.start();
            //   System.gc();
            throw new IOException();
        } else {
            try {
                this.wrappedBody.receiveRequest(request);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        //      this.stop();
    }

    public void receiveReply(Reply r) throws IOException {
        this.wrappedBody.receiveReply(r);
    }

    public String getNodeURL() {
        return this.wrappedBody.getNodeURL();
    }

    public UniqueID getID() {
        return this.id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {
        this.wrappedBody.updateLocation(id, body);
    }

    public UniversalBody getRemoteAdapter() {
        return this.wrappedBody.getRemoteAdapter();
    }

    public String getReifiedClassName() {
        return this.wrappedBody.getReifiedClassName();
    }

    public void enableAC() throws java.io.IOException {
        this.wrappedBody.enableAC();
    }

    public void disableAC() throws java.io.IOException {
        this.wrappedBody.disableAC();
    }

    protected void updateServer() {
        //        System.out.println("UniversalBodyWrapper.updateServer");
        //  LocationServer server = LocationServerFactory.getLocationServer();
        //        try {
        //            server.updateLocation(id, this.wrappedBody);
        //        } catch (Exception e) {
        //            System.out.println("XXXX Error XXXX");
        //           // e.printStackTrace();
        //        }
    }

    //protected synchronized void stop() {
    // this.stop=true;
    // this.notifyAll();
    //}
    //
    //protected synchronized void waitForStop(long time) {
    //	if (!this.stop) {
    //	 try {
    //		 wait(time);
    //	} catch (InterruptedException e) {
    //		e.printStackTrace();
    //	}
    //	}
    //
    //}
    public void run() {
        //        System.out.println("UniversalBodyWrapper.run life expectancy " + time);
        try {
            // Thread.currentThread().sleep(time);
            //  this.waitForStop(time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //        System.out.println("UniversalBodyWrapper.run end of life...");
        this.updateServer();
        this.wrappedBody = null;
        //        System.gc();
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#receiveHeartbeat(org.objectweb.proactive.core.body.ft.internalmsg.FTMessage)
     */
    public Object receiveHeartbeat() throws IOException {
        return this.wrappedBody.receiveHeartbeat();
    }

    public void createShortcut(Shortcut shortcut) throws IOException {
        // TODO implement
        throw new ProActiveRuntimeException("create shortcut method not implemented yet");
    }

    public String registerByName(String name, boolean rebind) throws IOException, ProActiveException {
        return this.wrappedBody.registerByName(name, rebind);
    }

    public String registerByName(String name, boolean rebind, String protocol) throws IOException,
            ProActiveException {
        return this.wrappedBody.registerByName(name, rebind, protocol);
    }

    public String getUrl() {
        return this.wrappedBody.getUrl();
    }
}
