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
package org.objectweb.proactive.core.gc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Level;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.ThreadPools;


public class Referenced implements Comparable<Referenced> {

    /**
     * The threaded broadcaster
     */
    // Initial code expected to create a dynamic thread pool but a wrong usage of the ThreadPoolExecutor 
    // API resulted of only 2 threads being created.
    // Since this code is working (or broken) since years we stick to this behavior even if it was a bug
    private static final ThreadPoolExecutor executor = ThreadPools.newFixedThreadPool(2,
            new NamedThreadFactory("ProActive GC Broadcasting Thread "));

    /**
     * The body we use to communicate with the proxy
     */
    private final UniversalBody body;

    /**
     * The tag to keep track of all the proxy instances targetting this AO
     */
    private WeakReference<GCTag> weakTag;

    /**
     * The last GC response we got, can be null if not applicable
     */
    private GCSimpleResponse lastResponse;

    /**
     * Is there currently a thread sending a message to this referencer?
     */
    private boolean isSendingMessage;

    /**
     * Detect missed deadlines
     */
    private long lastResponseTimestamp;

    /**
     * Can be useful to know who we belong to
     */
    private final GarbageCollector gc;

    Referenced(UniversalBodyProxy proxy, GarbageCollector gc) {
        this.body = proxy.getBody();
        GCTag tag = new GCTag();
        proxy.setGCTag(tag);
        this.weakTag = new WeakReference<GCTag>(tag);
        this.gc = gc;
        this.isSendingMessage = false;
        this.lastResponse = null;
        this.lastResponseTimestamp = System.currentTimeMillis();
    }

    private void setDead() {
        this.weakTag.clear();
    }

    /**
     * Called by the thread
     */
    private void doSendTheMessage(GCMessage msg) {
        GCResponse resp = null;
        try {
            resp = this.body.receiveGCMessage(msg);
        } catch (IOException e) {

            /* Ignore this proxy */
        } catch (Throwable e) {
            e.printStackTrace();
        }

        for (int i = 0; i < msg.size(); i++) {
            GCSimpleMessage m = msg.get(i);
            Referenced ref = m.getReferenced();
            synchronized (ref.gc) {
                if (resp == null) {
                    ref.setDead();
                } else {
                    ref.setLastResponse(resp.get(i));
                }
            }
        }
    }

    void sendMessage(final GCMessage msg) {
        synchronized (this.gc) {
            if (this.isSendingMessage) {
                this.gc.log(Level.WARN, "Sending thread for " + this + " still running");
                return;
            }
            this.isSendingMessage = true;
            executor.execute(new Runnable() {
                public void run() {
                    Referenced.this.doSendTheMessage(msg);
                    synchronized (Referenced.this.gc) {
                        Referenced.this.isSendingMessage = false;
                    }
                }
            });
        }
    }

    void setLastResponse(GCSimpleResponse response) {
        long now = System.currentTimeMillis();
        long acceptableDelay = (GarbageCollector.TTB + GarbageCollector.TTA) / 2;
        long delay;

        synchronized (this.gc) {
            delay = now - this.lastResponseTimestamp;
            this.lastResponse = response;
            this.lastResponseTimestamp = now;
            this.gc.newResponse(this);
        }

        if (delay > acceptableDelay) {
            this.gc.log(Level.WARN, "Delay " + delay + " too long talking to " + this);
        }
    }

    GCSimpleResponse getLastResponse() {
        return this.lastResponse;
    }

    @Override
    public String toString() {
        return this.getBodyID().shortString();
    }

    boolean isReferenced() {
        return this.weakTag.get() != null;
    }

    void add(UniversalBodyProxy ubp) {
        GCTag tag = this.weakTag.get();
        if (tag == null) {
            tag = new GCTag();
            this.weakTag = new WeakReference<GCTag>(tag);
        }
        ubp.setGCTag(tag);
    }

    boolean hasTerminated() {
        return (this.lastResponse != null) && this.lastResponse.isTerminationResponse();
    }

    public int compareTo(Referenced o) {
        return this.getBodyID().compareTo(o.getBodyID());
    }

    UniqueID getBodyID() {
        return this.body.getID();
    }
}
