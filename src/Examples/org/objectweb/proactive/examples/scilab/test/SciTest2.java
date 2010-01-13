/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.examples.scilab.test;

import java.util.List;

import org.objectweb.proactive.examples.scilab.AbstractData;
import org.objectweb.proactive.examples.scilab.AbstractGeneralTask;
import org.objectweb.proactive.examples.scilab.GeneralResult;
import org.objectweb.proactive.examples.scilab.SciTask;
import org.objectweb.proactive.examples.scilab.monitor.GenTaskInfo;
import org.objectweb.proactive.examples.scilab.monitor.MSEvent;
import org.objectweb.proactive.examples.scilab.monitor.MSEventListener;
import org.objectweb.proactive.examples.scilab.monitor.MSService;


public class SciTest2 {
    private MSService scilab;

    public void displayResult(GenTaskInfo scitaskInfo) {
        GeneralResult sciResult = scitaskInfo.getResult();
        List<AbstractData> listResult = sciResult.getList();

        for (AbstractData data : listResult) {
            System.out.println(data);
        }

        scilab.exit();
        System.exit(0);
    }

    public SciTest2(String nameVN, String pathVN) throws Exception {
        scilab = new MSService();
        scilab.deployEngine(nameVN, pathVN, new String[] { "Scilab" });

        scilab.addEventListenerTask(new MSEventListener() {
            public void actionPerformed(MSEvent evt) {
                GenTaskInfo sciTaskInfo = (GenTaskInfo) evt.getSource();

                if (sciTaskInfo.getState() == GenTaskInfo.SUCCEEDED) {
                    displayResult(sciTaskInfo);
                    return;
                }
            }
        });

        AbstractGeneralTask task = new SciTask("id");
        task.setJobInit("n = 10;");
        task.addDataOut("n");
        task.setJob("n = n+1;");
        System.out.println("Job : " + task.getJob());
        scilab.sendTask(task);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        new SciTest2(args[0], args[1]);
    }
}
