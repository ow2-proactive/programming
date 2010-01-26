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
package org.objectweb.proactive.examples.doctor;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class Receptionnist implements org.objectweb.proactive.RunActive {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    public final static int NO_ONE = -1;
    private int pat_id;
    private int doc_id;
    private Office off;

    public Receptionnist() {
    }

    public Receptionnist(Office _off) {
        off = _off;
        doc_id = pat_id = NO_ONE;
    }

    public synchronized void addPatient(int pat) {
        if (pat_id != NO_ONE) {
            logger.error("ERROR: addPatient(" + pat + ") with pat_id=" + pat_id);
            System.exit(0);
        }
        pat_id = pat;
        if (doc_id != NO_ONE) {
            off.doctorWithPatient(doc_id, pat_id);
            doc_id = pat_id = NO_ONE;
        }
    }

    public synchronized void addDoctor(int doc) {
        if (doc_id != NO_ONE) {
            logger.error("ERROR: addDoctor(" + doc + ") with doc_id=" + doc_id);
            System.exit(0);
        }
        doc_id = doc;
        if (pat_id != NO_ONE) {
            off.doctorWithPatient(doc_id, pat_id);
            doc_id = pat_id = NO_ONE;
        }
    }

    public synchronized boolean doctorWaiting() {
        return ((doc_id != NO_ONE) && (pat_id == NO_ONE));
    }

    public synchronized boolean patientWaiting() {
        return ((pat_id != NO_ONE) && (doc_id == NO_ONE));
    }

    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            if (doctorWaiting()) {
                service.blockingServeOldest("addPatient");
            }
            if (patientWaiting()) {
                service.blockingServeOldest("addDoctor");
            }
            if ((!doctorWaiting()) && (!patientWaiting())) {
                service.blockingServeOldest();
            }
        }
    }
}
