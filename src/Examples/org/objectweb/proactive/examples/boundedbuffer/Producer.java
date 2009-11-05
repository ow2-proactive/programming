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
package org.objectweb.proactive.examples.boundedbuffer;

/**
 * This class implements a simple producer
 */
public class Producer extends ConsumerProducer {

    /**
     * The no arg constructor
     * There _MUST_ be an empty no arg constructor
     */
    public Producer() {
    }

    /**
     * The effective constructor
     */
    public Producer(ConsumerProducerListener listener, BoundedBuffer buffer) {
        super("Producer", listener, buffer);
    }

    @Override
    protected void doStuff(boolean wasSuspended) {
        if (isSuspended) {
            if (!wasSuspended) {
                listener.producerStopRunning();
            }
        } else {
            if (wasSuspended) {
                listener.producerStartRunning();
            }
            buffer.put(new String("@" + (int) (Math.random() * 100)));
            // The call is synchronous as String is final
        }
    }
}
