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
package org.objectweb.proactive.extensions.timitspmd.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.timitspmd.util.observing.EventObserver;


/**
 * This class is useful to share TimerCounter and EventObserver instances
 * between classes on the same Body (or VM if app is not in a ProActive context)
 *
 * @author The ProActive Team
 */
public class TimItStore {
    private static TimItStore vmInstance;
    private static HashMap<Body, TimItStore> timerStoreMap = new HashMap<Body, TimItStore>();
    private String[] activation;
    public boolean allActivated;
    private Timed timed;
    private ArrayList<TimerCounter> tcList;

    /**
     * Used by getInstance to create an unique instance per Body
     */
    private TimItStore(Timed timed) {
        String prop = CentralPAPropertyRepository.PA_TIMIT_ACTIVATION.getValue();
        if (prop == null) {
            this.activation = new String[0];
        } else if (prop.equals("all")) {
            this.allActivated = true;
            this.activation = new String[0];
        } else {
            this.activation = prop.split(",");
        }
        Arrays.sort(this.activation);
        //        System.err.println("proactive.timit.activation = " + Arrays.toString(this.activation));
        this.tcList = new ArrayList<TimerCounter>();
        this.timed = timed;
    }

    /**
     * Get a TimerStore instance for the current Body, or the VM if we are
     * not on an active object (ProActive context)
     *
     * @return an instance of TimerStore
     */
    synchronized public static TimItStore getInstance(Timed timed) {
        Body body = PAActiveObject.getBodyOnThis();

        if (body == null) {
            if (vmInstance == null) {
                vmInstance = new TimItStore(timed);
            }
            return vmInstance;
        }
        TimItStore ts = TimItStore.timerStoreMap.get(body);
        if (ts == null) {
            ts = new TimItStore(timed);
            TimItStore.timerStoreMap.put(body, ts);
        }
        return ts;
    }

    public TimerCounter addTimerCounter(TimerCounter tc) {
        if (this.allActivated || Arrays.binarySearch(this.activation, tc.getName()) >= 0) {
            this.tcList.add(tc);
        }
        return tc;
    }

    public EventObserver addEventObserver(EventObserver eo) {
        if (this.allActivated || Arrays.binarySearch(this.activation, eo.getName()) > 0) {
            this.timed.getEventObservable().addObserver(eo);
        }
        return eo;
    }

    public void activation() {
        TimerCounter[] tcActivate = this.tcList.toArray(new TimerCounter[0]);
        this.timed.activate(tcActivate);
    }
}
