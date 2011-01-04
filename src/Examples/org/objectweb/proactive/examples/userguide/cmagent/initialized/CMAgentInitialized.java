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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
//@snippet-start init_CMA_skeleton
//@tutorial-start
package org.objectweb.proactive.examples.userguide.cmagent.initialized;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class CMAgentInitialized extends CMAgent implements InitActive, RunActive, EndActive {
    private long lastRequestDuration;
    private long startTime;
    private long requestsServed = 0;

    //@snippet-start cma_init_full
    public void initActivity(Body body) {
        //TODO 1. Print start information
        //@snippet-break init_CMA_skeleton
        //@tutorial-break
        System.out.println("### Started Active object " + body.getMBean().getName() + " on " +
            body.getMBean().getNodeUrl());
        //@snippet-resume init_CMA_skeleton
        //@tutorial-resume

        //TODO 2. Record start time
        //@snippet-break init_CMA_skeleton
        //@tutorial-break
        startTime = System.currentTimeMillis();
        //@snippet-resume init_CMA_skeleton
        //@tutorial-resume
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        long currentRequestDuration = 0;
        while (body.isActive()) {
            //TODO 3. wait for a request
            //@snippet-break init_CMA_skeleton
            //@tutorial-break
            service.waitForRequest(); // block until a request is received
            //@snippet-resume init_CMA_skeleton
            //@tutorial-resume

            //TODO 4. Record time
            //@snippet-break init_CMA_skeleton
            //@tutorial-break
            currentRequestDuration = System.currentTimeMillis();
            //@snippet-resume init_CMA_skeleton
            //@tutorial-resume

            //TODO 5. Serve request
            //@snippet-break init_CMA_skeleton
            //@tutorial-break
            service.serveOldest(); //server the requests in a FIFO manner
            //@snippet-resume init_CMA_skeleton
            //@tutorial-resume

            //TODO 6. Calculate request duration
            //@snippet-break init_CMA_skeleton
            //@tutorial-break
            currentRequestDuration = System.currentTimeMillis() - currentRequestDuration;

            // an intermediary variable is used so
            // when calling getLastRequestServeTime()
            // we get the first value before the last request
            // i.e when calling getLastRequestServeTime
            // the lastRequestDuration is update with the
            // value of the getLastRequestServeTime call
            // AFTER the previous calculated value has been returned
            lastRequestDuration = currentRequestDuration;
            //@snippet-resume init_CMA_skeleton
            //@tutorial-resume

            //TODO 7. Increment the number of requests served
            //@snippet-break init_CMA_skeleton
            //@tutorial-break
            requestsServed++;
            //@snippet-resume init_CMA_skeleton
            //@tutorial-resume
        }
    }

    public void endActivity(Body body) {
        //TODO 8. Calculate the running time of the active object using the start time recorded in initActivity()
        //@snippet-break init_CMA_skeleton
        //@tutorial-break
        long runningTime = System.currentTimeMillis() - startTime;
        //@snippet-resume init_CMA_skeleton
        //@tutorial-resume

        //TODO 9. Print various stop information
        //@snippet-break init_CMA_skeleton
        //@tutorial-break
        System.out.println("### You have killed the active object. The final" + " resting place is on " +
            body.getNodeURL() + "\n### It has faithfully served " + requestsServed + " requests " +
            "and has been an upstanding active object for " + runningTime + " ms ");
        //@snippet-resume init_CMA_skeleton
        //@tutorial-resume
    }

    public LongWrapper getLastRequestServeTime() {
        //TODO 10. Use wrappers for primitive types so the calls are asynchronous
        //@snippet-break init_CMA_skeleton
        //@tutorial-break
        return new LongWrapper(lastRequestDuration);
        //@snippet-resume init_CMA_skeleton
        //@tutorial-resume
    }
    //@snippet-end cma_init_full
}
//@snippet-end init_CMA_skeleton
//@tutorial-end
