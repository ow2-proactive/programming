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
package org.objectweb.proactive.examples.doctor;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class Office {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    //Number of patients being created at startup 
    public static final int NB_PAT = 15;

    //Number of doctors being created at startup 
    public static final int NB_DOC = 5;

    //Gaussian time parameters of patients (time being healthy)
    public static final int MEAN_PAT = 10000;
    public static final int SIGM_PAT = 3000;

    //Gaussian time parameters of doctors (cure finding duration)
    public static final int MEAN_DOC = 13000;
    public static final int SIGM_DOC = 4000;

    //Maximal number of doctors and patients (used to size queues)
    public static final int MAX_PAT = 50;
    public static final int MAX_DOC = 20;

    //State constants definition
    public static final int DOC_UNDEF = -1;
    public static final int PAT_WELL = -2;
    public static final int PAT_SICK = -1;
    private Vector patients;
    private Vector doctors;
    private Office me;
    private Receptionnist recept;
    private DisplayPanel display;
    private RandomTime rand;
    private OfficeWindow win;

    public Office() {
    }

    public Office(Integer useLess) {
        // Creating patient and doctor vectors
        patients = new java.util.Vector();
        doctors = new java.util.Vector();

        // Creating the display window
        win = new OfficeWindow();
        win.pack();
        win.setTitle("The Salishan problems (3)");
        win.setVisible(true);

        display = win.getDisplay();
    }

    public void init(Office _me, Receptionnist _recept) {
        me = _me;
        recept = _recept;
        createPeople();
    }

    public synchronized void createPeople() {
        int i;
        try {
            rand = org.objectweb.proactive.api.PAActiveObject.newActive(RandomTime.class, null);

            for (i = 1; i <= NB_DOC; i++)
                addDoctor(i, MEAN_DOC, SIGM_DOC);

            for (i = 1; i <= NB_PAT; i++)
                addPatient(i, MEAN_PAT, SIGM_PAT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDoctor(int id, long meanTime, long sigmaTime) {
        try {
            Object[] params = { Integer.valueOf(id), Long.valueOf(meanTime), Long.valueOf(sigmaTime), me,
                    rand };

            Doctor newDoc = org.objectweb.proactive.api.PAActiveObject.newActive(Doctor.class, params);
            doctors.insertElementAt(newDoc, id - 1);
            recept.addDoctor(id);
            display.addDoctor(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPatient(int id, long meanTime, long sigmaTime) {
        try {
            Object[] params = { new Integer(id), new Long(meanTime), new Long(sigmaTime), me, rand };

            Patient newPat = org.objectweb.proactive.api.PAActiveObject.newActive(Patient.class, params);
            patients.insertElementAt(newPat, id - 1);
            display.addPatient(id);
            Thread.yield();
            newPat.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void doctorCureFound(int doctor, int patient, Cure _cure) {
        display.setPatState(patient, PAT_WELL);
        display.setDocFinished(doctor, patient);

        Patient pat = (Patient) patients.elementAt(patient - 1);
        pat.receiveCure(_cure);
        recept.addDoctor(doctor);
    }

    public synchronized void patientSick(int patient) {
        display.setPatState(patient, PAT_SICK);
        recept.addPatient(patient);
    }

    public synchronized void doctorWithPatient(int doctor, int patient) {
        display.setPatState(patient, doctor);

        Patient pat = (Patient) patients.elementAt(patient - 1);
        Doctor doc = (Doctor) doctors.elementAt(doctor - 1);

        pat.hasDoctor(doctor);
        doc.curePatient(patient);
    }

    public static void main(String[] argv) {
        logger.info("The Salishan problems : Problem 3 - The Doctor's Office");
        try {
            Office off = org.objectweb.proactive.api.PAActiveObject.newActive(Office.class,
                    new Object[] { new Integer(0) });
            Receptionnist recept = org.objectweb.proactive.api.PAActiveObject.newActive(Receptionnist.class,
                    new Object[] { off });
            off.init(off, recept);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
