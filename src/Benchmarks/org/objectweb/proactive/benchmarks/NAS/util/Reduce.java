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
package org.objectweb.proactive.benchmarks.NAS.util;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.benchmarks.NAS.FT.WorkerFT;


public class Reduce implements Serializable {

    private Body body;
    private Complex sum_Complex, result_Complex;
    private int nbReceived;
    private int groupSize;

    public Reduce() {
    }

    public void init(WorkerFT workers) {
        body = PAActiveObject.getBodyOnThis();
        nbReceived = 0;
        this.groupSize = PAGroup.size(workers);
    }

    public Complex sumC(Complex value) {
        nbReceived++;

        if (sum_Complex == null) {
            sum_Complex = value;
        } else {
            sum_Complex.plusMe(value);
        }

        while (nbReceived < groupSize) {
            blockingServe();
            if (sum_Complex == null)
                break;
        }

        if (sum_Complex != null) {
            nbReceived = 0;
            result_Complex = sum_Complex;
            sum_Complex = null;
        }

        return result_Complex;
    }

    private final void blockingServe() {
        body.serve(body.getRequestQueue().blockingRemoveOldest());
    }

    public void msg(String str) {
        System.out.println("\t *R* --------> " + str);
    }
}